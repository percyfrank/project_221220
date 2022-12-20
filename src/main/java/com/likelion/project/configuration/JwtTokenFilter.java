package com.likelion.project.configuration;

import com.likelion.project.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class JwtTokenFilter extends OncePerRequestFilter {

    private final UserService userService;
    private final String secretKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        final String authentication = request.getHeader(HttpHeaders.AUTHORIZATION);
        log.info("authentication : {}", authentication);

        // 토근 없거나 이상한 토근 확인 작업
        if(authentication == null || !authentication.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 분리
        String token;
        try {
            token = authentication.split(" ")[1];
        } catch (Exception e) {
            log.error("토큰 추출 실패");
            filterChain.doFilter(request, response);
            return;
        }

        // 만료 확인 작업
        if (JwtTokenUtil.isExpired(token,secretKey)) {
            log.error("토큰 만료");
            filterChain.doFilter(request,response);
            return;
        }

        String userName = JwtTokenUtil.getUserName(token, secretKey);
        log.info("userName : {}", userName);


        // 권한 주거나 안주기
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userName, null,null);

        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        filterChain.doFilter(request, response);


    }
}
