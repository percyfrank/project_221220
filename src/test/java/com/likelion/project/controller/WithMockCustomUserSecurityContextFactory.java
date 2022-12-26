package com.likelion.project.controller;

import com.likelion.project.domain.dto.user.UserJoinRequest;
import com.likelion.project.service.UserService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.List;

public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {

    private final UserService userService;

    public WithMockCustomUserSecurityContextFactory(UserService userService) {
        this.userService = userService;
    }

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser customUser) {

        String username = customUser.username();
        String password = "password";

        UserJoinRequest userJoinRequest = UserJoinRequest.builder().userName(username).password(password).build();
        userService.join(userJoinRequest);

        final UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userJoinRequest.getUserName(), userJoinRequest.getPassword(), null);

        final SecurityContext securityContext = SecurityContextHolder.createEmptyContext();

        securityContext.setAuthentication(authenticationToken);

        return securityContext;
    }
}