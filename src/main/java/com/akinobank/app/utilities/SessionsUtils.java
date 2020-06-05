package com.akinobank.app.utilities;

import com.akinobank.app.config.GeoIpConfig;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import eu.bitwalker.useragentutils.UserAgent;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Log4j2
@Component
public class SessionsUtils {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private GeoIpConfig geoIpConfig;

    public String getIpAdress() {
        return request.getRemoteAddr();
    }

    public UserAgent getUserAgent() {
        UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
        log.info("User agent infos : {}", userAgent.toString());
        return userAgent;
    }

    public CityResponse getCityResponse() {
        File citiesDatabase = new File(geoIpConfig.getLocation());
        try {
            DatabaseReader dbReader = new DatabaseReader.Builder(citiesDatabase).build();
            return dbReader.city(InetAddress.getByName(getIpAdress()));
        } catch (GeoIp2Exception | UnknownHostException e) {
            log.info("Could not find city : {}", e.getMessage());
            return null;
        } catch (IOException e) {
            log.info("Could not find database file : {}", e.getMessage());
            return null;
        }
    }
}
