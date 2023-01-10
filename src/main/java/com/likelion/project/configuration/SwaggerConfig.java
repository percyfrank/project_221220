package com.likelion.project.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.GroupedOpenApi;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "MutsaSNS",
                description = "회원가입,로그인, 게시글 CRUD, 좋아요, 댓글, 알람 기능",
                version = "v1",
                contact = @Contact(name = "Ohsuk", email = "percykwon@naver.com")
        ),
        tags = {
                @Tag(name = "1. 회원", description = "회원 가입/로그인"),
                @Tag(name = "2. 게시글", description = "게시글 등록, 조회, 수정, 삭제"),
                @Tag(name = "3. 댓글", description = "댓글 등록, 조회, 수정, 삭제"),
                @Tag(name = "4. 좋아요", description = "댓글, 게시글에 좋아요 기능"),
                @Tag(name = "5. 마이피드", description = "특정 게시글 조회"),
                @Tag(name = "6. 알람", description = "포스트에 달린 댓글, 좋아요 알림")
        }

)

@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi SecurityGroupOpenApi() {
        String[] paths = {"/api/v1/**"};

        return GroupedOpenApi
                .builder()
                .group("MutsaSNS API v1")
                .pathsToMatch(paths)
                .addOpenApiCustomiser(buildSecurityOpenApi())
                .build();
    }

    public OpenApiCustomiser buildSecurityOpenApi() {
        // jwt token 을 한번 설정하면 header 에 값을 넣어주는 코드
        return OpenApi -> OpenApi.addSecurityItem(new SecurityRequirement().addList("jwt token"))
                .getComponents().addSecuritySchemes("jwt token", new SecurityScheme()
                        .name("Authorization")
                        .type(SecurityScheme.Type.APIKEY) //받아올때 어느타입으로 받아올것인지
                        .in(SecurityScheme.In.HEADER) //jwt값이 Header에 담기므로 HEADER로 지정한다.
                        .bearerFormat("JWT")
                        .scheme("Bearer"));
    }

//    @Bean
//    public Docket api() {
//        return new Docket(DocumentationType.OAS_30)
//                .securityContexts(Arrays.asList(securityContext()))
//                .securitySchemes(Arrays.asList(apiKey()))
//                .select()
//                .apis(RequestHandlerSelectors.basePackage("com.likelion.project.controller"))
//                .paths(PathSelectors.any())
//                .build();
//    }
//
//    // 여기서부터 Swagger에서 token 헤더에 넣는 설정정
//   private SecurityContext securityContext() {
//        return SecurityContext.builder()
//                .securityReferences(defaultAuth())
//                .build();
//    }
//
//    private List<SecurityReference> defaultAuth() {
//        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
//        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
//        authorizationScopes[0] = authorizationScope;
//        return Arrays.asList(new SecurityReference("Authorization", authorizationScopes));
//    }
//
//    private ApiKey apiKey() {
//        return new ApiKey("Authorization", "Authorization", "header");
//    }
}
