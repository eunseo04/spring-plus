package org.example.expert.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest httpRequest,
            @NonNull HttpServletResponse httpResponse,
            @NonNull FilterChain chain
    ) throws ServletException, IOException {

        // HTTP 요청 헤더에서 "Authorization" 헤더값을 가져옴
        String authorizationHeader = httpRequest.getHeader("Authorization");

        // Authorization 헤더가 없거나 "Bearer "로 시작하지 않으면 JWT 인증을 건너뜀
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            chain.doFilter(httpRequest, httpResponse);
            return;
        }

        String jwt = jwtUtil.substringToken(authorizationHeader);

        // JWT 검증 및 인증 설정
        if (!processAuthentication(jwt, httpRequest, httpResponse)) {
            return;
        }

        // JWT 검증 성공 시 다음 필터로 요청 전달
        chain.doFilter(httpRequest, httpResponse);
    }

    // JWT 토큰을 검증하고 SecurityContext에 인증 정보를 설정하는 메서드
    private boolean processAuthentication(String jwt, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        try {
            // JWT 토큰을 파싱하여 Claims(토큰에 담긴 정보) 추출
            Claims claims = jwtUtil.extractClaims(jwt);

            // SecurityContext에 인증 정보가 없으면 설정 (이미 인증된 경우 중복 설정 방지)
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                setAuthentication(claims);
            }
            return true; // 검증 성공
        } catch (SecurityException | MalformedJwtException e) {
            JwtAuthenticationFilter.log.error("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.", e);
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않는 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            JwtAuthenticationFilter.log.error("Expired JWT token, 만료된 JWT token 입니다.", e);
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            JwtAuthenticationFilter.log.error("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.", e);
            httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "지원되지 않는 JWT 토큰입니다.");
        } catch (Exception e) {
            JwtAuthenticationFilter.log.error("Internal server error", e);
            httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return false;
    }

    // JWT Claims 에서 사용자 정보를 추출하여 Spring Security의 인증 정보 설정
    private void setAuthentication(Claims claims) {
        // JWT의 subject claim 에서 사용자 ID 추출 (subject는 JWT 표준 claim)
        Long userId = Long.valueOf(claims.getSubject());
        // 커스텀 claim 에서 이메일 정보 추출
        String email = claims.get("email", String.class);
        // 커스텀 claim 에서 사용자 권한 정보를 추출하여 enum 으로 변환
        UserRole userRole = UserRole.of(claims.get("userRole", String.class));
        // 커스텀 claim 에서 닉네임 정보 추출
        String nickname = claims.get("nickname", String.class);

        // 추출한 정보로 인증된 사용자 객체 생성
        System.out.println(userRole);
        AuthUser authUser = new AuthUser(userId, email, userRole, nickname);
        // Spring Security가 인식할 수 있는 Authentication 객체 생성
        Authentication authenticationToken = new JwtAuthenticationToken(authUser);
        // SecurityContext에 인증 정보 저장 - 이후 @AuthenticationPrincipal로 접근 가능
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
}