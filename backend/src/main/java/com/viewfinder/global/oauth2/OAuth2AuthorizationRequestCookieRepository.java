package com.viewfinder.global.oauth2;

import com.viewfinder.global.config.JwtCookieProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Map;

// OAuth2 인가 요청의 일회용 state를 서버 HttpSession 대신 짧은 수명의 HttpOnly Cookie에 보관
@Component
public class OAuth2AuthorizationRequestCookieRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    static final String COOKIE_NAME = "oauth2_authorization_request";
    private static final long COOKIE_MAX_AGE_SECONDS = 180;

    private final JwtCookieProperties jwtCookieProperties;

    public OAuth2AuthorizationRequestCookieRepository(JwtCookieProperties jwtCookieProperties) {
        this.jwtCookieProperties = jwtCookieProperties;
    }

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return findCookie(request)
                .map(Cookie::getValue)
                .flatMap(this::deserialize)
                .orElse(null);
    }

    @Override
    public void saveAuthorizationRequest(
            OAuth2AuthorizationRequest authorizationRequest,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (authorizationRequest == null) {
            deleteCookie(response);
            return;
        }

        String serializedRequest = serialize(authorizationRequest);
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, serializedRequest)
                .httpOnly(true)
                .secure(jwtCookieProperties.secure())
                .sameSite(jwtCookieProperties.sameSite())
                .path("/")
                .maxAge(COOKIE_MAX_AGE_SECONDS)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        OAuth2AuthorizationRequest authorizationRequest = loadAuthorizationRequest(request);
        deleteCookie(response);
        return authorizationRequest;
    }

    private String serialize(OAuth2AuthorizationRequest authorizationRequest) {
        String registrationId = authorizationRequest.getAttribute(OAuth2ParameterNames.REGISTRATION_ID);
        // 카카오 Authorization Code 흐름에 필요한 고정 정보와 state만 URL-safe 형식으로 Cookie에 보관
        return String.join(".",
                encode(authorizationRequest.getAuthorizationUri()),
                encode(authorizationRequest.getClientId()),
                encode(authorizationRequest.getRedirectUri()),
                encode(String.join(",", authorizationRequest.getScopes())),
                encode(authorizationRequest.getState()),
                encode(registrationId)
        );
    }

    private java.util.Optional<OAuth2AuthorizationRequest> deserialize(String value) {
        try {
            String[] values = value.split("\\.", -1);
            if (values.length != 6) {
                return java.util.Optional.empty();
            }

            return java.util.Optional.of(OAuth2AuthorizationRequest.authorizationCode()
                    .authorizationUri(decode(values[0]))
                    .clientId(decode(values[1]))
                    .redirectUri(decode(values[2]))
                    .scope(decode(values[3]).split(","))
                    .state(decode(values[4]))
                    .attributes(Map.of(OAuth2ParameterNames.REGISTRATION_ID, decode(values[5])))
                    .build());
        } catch (IllegalArgumentException exception) {
            return java.util.Optional.empty();
        }
    }

    private String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private String decode(String value) {
        return new String(Base64.getUrlDecoder().decode(value), java.nio.charset.StandardCharsets.UTF_8);
    }

    private java.util.Optional<Cookie> findCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return java.util.Optional.empty();
        }

        for (Cookie cookie : request.getCookies()) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return java.util.Optional.of(cookie);
            }
        }
        return java.util.Optional.empty();
    }

    private void deleteCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(jwtCookieProperties.secure())
                .sameSite(jwtCookieProperties.sameSite())
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }
}
