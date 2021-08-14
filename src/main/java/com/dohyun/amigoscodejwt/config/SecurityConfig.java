package com.dohyun.amigoscodejwt.config;

import com.dohyun.amigoscodejwt.filter.CustomAuthenticationFilter;
import com.dohyun.amigoscodejwt.filter.CustomAuthorizationFilter;
import com.dohyun.amigoscodejwt.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final RedisUtil redisUtil;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        CustomAuthenticationFilter customAuthenticationFilter = new CustomAuthenticationFilter(authenticationManagerBean(), redisUtil);
        customAuthenticationFilter.setFilterProcessesUrl("/api/login");

        http
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/api/login/**", "/api/token/refresh/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/user/**").hasAnyAuthority("ROLE_USER")
                .antMatchers(HttpMethod.POST, "/api/user/save/**").hasAnyAuthority("ROLE_ADMIN")
                .anyRequest().authenticated()
                .and()
                .addFilter(customAuthenticationFilter)
                .addFilterBefore(new CustomAuthorizationFilter(redisUtil), UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
