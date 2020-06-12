package com.akinobank.app.config;

//import com.akinobank.app.filters.XSSFilter;

import com.akinobank.app.enumerations.Role;
import com.akinobank.app.filters.JwtFilter;
import com.akinobank.app.utilities.JsonHTMLEscape;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

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

    @Bean
    public HttpMessageConverter<?> jsonConverter() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(String.class, new JsonHTMLEscape());
        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.registerModule(module);
        return new MappingJackson2HttpMessageConverter(objectMapper);
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.formLogin().loginPage("/admin/login").defaultSuccessUrl("/admin", true)
            .and().logout().logoutUrl("/admin/logout");

        httpSecurity.cors().disable().csrf()
//            .disable()
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            .and()
//      Allow certain routes
            .authorizeRequests().antMatchers(
                "/",
            "/test/**", "/session",
            "/admin/login", "/verify","/2fa_setup",
            "/api/auth",
            "/api/auth/",
            "/api/auth/code",
            "/api/auth/refresh",
            "/api/auth/agent",
            "/api/forgot_password",
            "/compte_details",
            "/confirm", "/set_password", "/js/**", "/css/**").permitAll().
            and().authorizeRequests().antMatchers("/admin/**").hasRole(Role.ADMIN.name()). // just for now
            and().authorizeRequests().antMatchers("/agent/**").hasRole(Role.AGENT.name()).
            and().authorizeRequests().antMatchers("/client/**").hasRole(Role.CLIENT.name()).
            anyRequest().authenticated()
            .and().exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint)
            .and().sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);

        httpSecurity.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(jsonConverter());
    }

//    @Override
//    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//        auth.inMemoryAuthentication()
//            .withUser(User.builder().email("oussa@g.c").password("pass").role(Role.ADMIN).build());
//    }

//    @Bean
//    public FilterRegistrationBean<JwtFilter> jwtFilter(){
//        FilterRegistrationBean<JwtFilter> registrationBean
//            = new FilterRegistrationBean<>();
//
//        registrationBean.setFilter(new JwtFilter());
//        registrationBean.addUrlPatterns("/client/*");
//        registrationBean.addUrlPatterns("/agent/*");
//
//        return registrationBean;
//    }
}
