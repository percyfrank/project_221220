package com.likelion.project.configuration;

import com.likelion.project.jwt.CustomAuthenticationEntryPoint;
import com.likelion.project.jwt.JwtTokenExceptionFilter;
import com.likelion.project.jwt.JwtTokenFilter;
import com.likelion.project.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(securedEnabled = true,prePostEnabled = true)
public class SecurityConfig {

    @Value("${jwt.token.secret}")
    private String secretKey;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .httpBasic().disable() //rest api 이므로 기본설정 사용안함. 기본설정은 비인증시 로그인폼 화면으로 리다이렉트
                .csrf().disable()      //rest api 이므로 csrf 보안이 필요없음
                .cors()
                .and()
                    .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS) //jwt token으로 인증시 세션 필요없으므로 생성안함
                .and()
                    .authorizeRequests()
                    .antMatchers("/api/v1/users/login","/api/v1/users/join", "/swagger-ui").permitAll() // join, login 은 언제나 가능
                    .antMatchers(HttpMethod.GET,"/api/v1/**").permitAll()   // 모든 get 요청 허용
                    .antMatchers(HttpMethod.POST,"/api/v1/**").authenticated()  // 순서대로 적용이 되기 때문에 join, login 다음에 써주기
                    .antMatchers(HttpMethod.PUT, "/api/v1/**").authenticated()
                    .antMatchers(HttpMethod.DELETE, "/api/v1/**").authenticated()
                .and()
                    .exceptionHandling().authenticationEntryPoint(new CustomAuthenticationEntryPoint())// 토큰 없을 시 에러 처리
                .and()
                    .addFilterBefore(new JwtTokenFilter(secretKey), UsernamePasswordAuthenticationFilter.class) // UserNamePasswordAuthenticationFilter 적용하기 전에 JwtTokenFilter 적용한다는 의미
                    .addFilterBefore(new JwtTokenExceptionFilter(),JwtTokenFilter.class)    // 인증
                .build();
    }
}
