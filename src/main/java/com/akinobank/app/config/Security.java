package com.akinobank.app.config;

//import com.akinobank.app.filters.XSSFilter;

import com.akinobank.app.enumerations.Role;
import com.akinobank.app.filters.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class Security extends WebSecurityConfigurerAdapter implements WebMvcConfigurer {

    @Autowired
    private JwtFilter jwtFilter;

    @Autowired
    private JwtAuthEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
//      Disable CSRF
        httpSecurity.cors().disable().csrf().disable()
//      Allow certain routes
                .authorizeRequests().antMatchers("/api/auth", "/api/forgot_password", "/confirm", "/set_password").permitAll().
                and().authorizeRequests().antMatchers("/api/auth").permitAll().
                and().authorizeRequests().antMatchers("/admin/**").permitAll(). // just for now
                and().authorizeRequests().antMatchers("/agent/**").hasRole(Role.AGENT.name()).
                and().authorizeRequests().antMatchers("/client/**").hasRole(Role.CLIENT.name()).
                anyRequest().authenticated().and().
                exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint).and().sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        httpSecurity.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowCredentials(true)
            .allowedHeaders("*")
            .allowedMethods("GET, POST, PATCH, PUT, DELETE, OPTIONS")
            .allowedOrigins("*");
    }

//    @Bean
//    public FilterRegistrationBean xssPreventFilter() {
//        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
//
//        registrationBean.setFilter(new XSSFilter());
//        registrationBean.addUrlPatterns("/*");
//
//        return registrationBean;
//    }

}
