package antifraud.security;

import antifraud.persistence.service.UserServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    private final UserServices userServices;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public SecurityConfig(
        RestAuthenticationEntryPoint restAuthenticationEntryPoint,
        UserServices userServices,
        PasswordEncoder passwordEncoder
    ) {
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
        this.userServices = userServices;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(userServices) // user store 1
                .passwordEncoder(passwordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .exceptionHandling().accessDeniedHandler(accessDeniedHandler())
            .and()
            .httpBasic()
            .authenticationEntryPoint(restAuthenticationEntryPoint) // Handles auth error
            .and()
            .csrf().disable().headers().frameOptions().disable() // for Postman, the H2 console
            .and()
            .authorizeRequests() // manage access
            .mvcMatchers(HttpMethod.POST, "/api/auth/user").permitAll()
            .mvcMatchers(HttpMethod.POST, "/actuator/**").permitAll()
            .mvcMatchers(HttpMethod.DELETE, "/api/auth/user/**").hasRole("ADMINISTRATOR")
            .mvcMatchers(HttpMethod.GET, "/api/auth/list").hasAnyRole("ADMINISTRATOR", "SUPPORT")
            .mvcMatchers(HttpMethod.POST, "/api/antifraud/transaction").hasRole("MERCHANT")
            .mvcMatchers(HttpMethod.PUT, "/api/auth/access").hasRole("ADMINISTRATOR")
            .mvcMatchers(HttpMethod.PUT, "/api/auth/role").hasRole("ADMINISTRATOR")
            .mvcMatchers("/api/antifraud/suspicious-ip").hasRole("SUPPORT")
            .mvcMatchers("/api/antifraud/suspicious-ip/**").hasRole("SUPPORT")
            .mvcMatchers("/api/antifraud/stolencard").hasRole("SUPPORT")
            .mvcMatchers("/api/antifraud/stolencard/**").hasRole("SUPPORT")
            .mvcMatchers("/api/antifraud/history/**").hasRole("SUPPORT")
            .mvcMatchers(HttpMethod.PUT, "/api/antifraud/transaction").hasRole("SUPPORT")
            .anyRequest().authenticated()
            // other matchers
            .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS); // no session
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, ex) -> {
            var responseTemplate =
                "{\n" +
                "  \"timestamp\" : \"<date>\"," +
                "  \"status\" : 403," +
                "  \"error\" : \"Forbidden\"," +
                "  \"message\" : \"Access Denied!\"," +
                "  \"path\" : \"" + request.getRequestURI() + "\"\n" +
                "}";
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            ServletOutputStream out = response.getOutputStream();
            out.println(responseTemplate);
            out.flush();
        };
    }
}
