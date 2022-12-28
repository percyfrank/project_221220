package com.likelion.project.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class JwtTokenFilter extends OncePerRequestFilter {

//    private final UserService userService;
    private final String secretKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 헤더에서 토큰 꺼내기
        final String authentication = request.getHeader(HttpHeaders.AUTHORIZATION);

        // 토근 없거나 이상한 토큰 헤더 형식 확인
        if(authentication == null || !authentication.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰 분리
        final String token;
        try {
            log.info("authentication : {}", authentication);
            token = authentication.split(" ")[1].trim();
        } catch (Exception e) {
            log.error("토큰 추출을 실패했습니다.");
            filterChain.doFilter(request, response);
            return;
        }

        // 만료 확인 작업
        if (JwtTokenUtil.isExpired(token,secretKey)) {
            log.error("토큰이 만료되었습니다.");
            filterChain.doFilter(request,response);
            return;
        }

        // 토큰에서 이름 추출
        String userName = JwtTokenUtil.getUserName(token, secretKey);
        log.info("userName : {}", userName);

        // 권한 부여에 관한 메서드
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userName, null,null);

        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        filterChain.doFilter(request, response);
    }
}
