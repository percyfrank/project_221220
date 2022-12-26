package com.likelion.project.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.project.exception.ErrorCode;
import com.likelion.project.exception.ErrorResponse;
import com.likelion.project.exception.Response;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class JwtTokenExceptionFilter extends OncePerRequestFilter {


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        try {
            log.info("검증 시작");
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {       // 유효기간 만료 토큰
            log.error("만료된 JWT 토큰입니다.");
            setErrorResponse(response, ErrorCode.INVALID_TOKEN);
        } catch (MalformedJwtException  e) {     // 구성이 잘못된 토큰(헤더,내용,서명이 없는 경우)
            log.error("올바르게 구성되지 못한 JWT 토큰입니다.");
            setErrorResponse(response, ErrorCode.INVALID_TOKEN);
        } catch (SignatureException e) {        // 서명을 확인할 수 없는 토큰
            log.error("서명을 확인할 수 없는 토큰입니다.");
            setErrorResponse(response, ErrorCode.INVALID_TOKEN);
        } catch (UnsupportedJwtException e) {   // 형식이 이상한 토큰
            log.error("지원하지 않는 형식의 JWT 토큰입니다.");
            setErrorResponse(response, ErrorCode.INVALID_TOKEN);
        } catch (IllegalArgumentException | JwtException e) {  // 잘못된 토큰
            log.error("잘못된 JWT 토큰입니다.");
            setErrorResponse(response, ErrorCode.INVALID_TOKEN);

        }
    }

    private void setErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {

        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ObjectMapper objectMapper = new ObjectMapper();
//        Gson gson = new Gson();

        ErrorResponse errorResponse = new ErrorResponse(errorCode);
        response.getWriter().write(objectMapper.writeValueAsString(Response.error("ERROR",errorResponse)));
//        response.getWriter().write(gson.toJson(Response.error("ERROR",errorResponse)));

    }


}
