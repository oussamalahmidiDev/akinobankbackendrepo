package com.akinobank.app.config;

//import com.akinobank.app.filters.XSSFilter;

import com.akinobank.app.enumerations.Role;
import com.akinobank.app.filters.JwtFilter;
import com.akinobank.app.utilities.JsonHTMLEscape;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Log4j2
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

        httpSecurity
            .headers()
            .frameOptions()
            .disable();

        httpSecurity
            .formLogin()
            .loginPage("/admin/login")
            .loginProcessingUrl("/admin/auth")
            .usernameParameter("email")
            .passwordParameter("password")
            .successHandler((req, res, auth) -> {
                log.info("Auth success : {}", auth.getName());
                for (GrantedAuthority authority : auth.getAuthorities()) {
                    log.info("AUTHORITY : {}", authority.getAuthority());
                }
                res.sendRedirect("/admin");
            })
            .failureHandler((req, res, exception) -> {
                String message = "";
                if (exception.getClass().isAssignableFrom(BadCredentialsException.class) || exception instanceof UsernameNotFoundException)
                    message = "L'email ou mot de passe est incorrect.";
                else
                    message = "Erreur d'authentification.";
                log.info("Authentication failed : {}", message);
                req.getSession().setAttribute("message", message);
                req.getSession().setAttribute("email", req.getParameter("email"));
                res.sendRedirect("/admin/login");
            })
            .permitAll()
            .and()
            .logout()
            .logoutUrl("/admin/logout")
            .deleteCookies("JSESSIONID");

        CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
//        csrfTokenRepository.setCookieDomain("herokuapp.com");

        httpSecurity.cors().disable()
            .csrf()
//            .disable()
            .csrfTokenRepository(csrfTokenRepository)
            .and()

//      Allow certain routes
            .authorizeRequests().antMatchers(
            "/",
            "/test/**", "/session",
            "/ws/**",
            "/admin/login", "/verify", "/2fa_setup", "/admin/auth", "/recover_account",
            "/api/auth",
            "/test",
            "/api/auth/",
            "/api/auth/code",
            "/api/auth/refresh",
            "/api/auth/agent",
            "/api/forgot_password",
            "/compte_details",
            "/confirm", "/set_password", "/js/**", "/css/**", "/assets/**").permitAll().
            and().authorizeRequests().antMatchers("/admin/**").hasRole(Role.ADMIN.name()). // just for now
            and().authorizeRequests().antMatchers("/agent/**").hasRole(Role.AGENT.name()).
            and().authorizeRequests().antMatchers("/client/**").hasRole(Role.CLIENT.name()).
            anyRequest().authenticated()
            .and().exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint)
            .and().sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);

        httpSecurity.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    }

//    @Bean
//    public FilterRegistrationBean adminAuthFilterRegistration() {
//        FilterRegistrationBean registration = new FilterRegistrationBean();
//        registration.setFilter(adminAuthFilter);
////        registration.setOrder(2);
//        registration.addUrlPatterns("/admin/*");
//        return registration;
//    }

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
//    public FilterRegistrationBean<JwtFilter> jwtFilter() {
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
