package com.example.bankcards.security.jwt;

import com.example.bankcards.config.properties.SecurityProperties;
import com.example.bankcards.security.exception.JwtAuthenticationException;
import com.example.bankcards.security.model.UserPrincipal;
import com.example.bankcards.security.service.BankUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Сервис генерации и валидации JWT токенов.
 */
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private static final String TOKEN_TYPE_CLAIM = "type";
    private static final String USERNAME_CLAIM = "username";
    private static final String ROLES_CLAIM = "roles";
    private static final String AUDIENCE_CLAIM = "aud";

    private final SecurityProperties securityProperties;
    private final BankUserDetailsService userDetailsService;
    private SecurityProperties.Jwt properties;
    private SignatureAlgorithm signatureAlgorithm;
    private Key signingKey;
    private JwtParser jwtParser;

    @PostConstruct
    void init() {
        this.properties = securityProperties.getJwt();
        this.signatureAlgorithm = SignatureAlgorithm.forName(properties.getAlgorithm());
        if (!signatureAlgorithm.isHmac()) {
            throw new IllegalArgumentException("Only HMAC algorithms are supported");
        }
        byte[] secretBytes = properties.getSecret().getBytes(StandardCharsets.UTF_8);
        this.signingKey = Keys.hmacShaKeyFor(secretBytes);
        this.jwtParser = Jwts.parserBuilder()
                .requireIssuer(properties.getIssuer())
                .setSigningKey(signingKey)
                .build();
    }

    public Duration getAccessTokenTtl() {
        return properties.getAccessToken().getTtl();
    }

    public Duration getRefreshTokenTtl() {
        return properties.getRefreshToken().getTtl();
    }

    public String createAccessToken(UserPrincipal principal) {
        return buildToken(principal, TokenType.ACCESS, properties.getAccessToken());
    }

    public String createRefreshToken(UserPrincipal principal) {
        return buildToken(principal, TokenType.REFRESH, properties.getRefreshToken());
    }

    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        TokenType tokenType = extractTokenType(claims);
        if (tokenType != TokenType.ACCESS) {
            throw new JwtAuthenticationException("Invalid token type");
        }
        Long userId = extractUserId(claims);
        UserPrincipal principal = userDetailsService.loadUserById(userId);
        validatePrincipal(principal);
        return new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities());
    }

    public UserPrincipal getUserFromRefreshToken(String refreshToken) {
        Claims claims = parseClaims(refreshToken);
        TokenType tokenType = extractTokenType(claims);
        if (tokenType != TokenType.REFRESH) {
            throw new JwtAuthenticationException("Invalid token type");
        }
        Long userId = extractUserId(claims);
        UserPrincipal principal = userDetailsService.loadUserById(userId);
        validatePrincipal(principal);
        return principal;
    }

    private void validatePrincipal(UserPrincipal principal) {
        if (!principal.isAccountNonLocked()) {
            throw new JwtAuthenticationException("User account is locked");
        }
        if (!principal.isEnabled()) {
            throw new JwtAuthenticationException("User account is disabled");
        }
        if (!principal.isAccountNonExpired()) {
            throw new JwtAuthenticationException("User account is expired");
        }
    }

    private String buildToken(UserPrincipal principal, TokenType tokenType,
                              SecurityProperties.Jwt.Token tokenProperties) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(tokenProperties.getTtl());

        JwtBuilder builder = Jwts.builder()
                .setSubject(principal.getId().toString())
                .setIssuer(properties.getIssuer())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiresAt))
                .claim(TOKEN_TYPE_CLAIM, tokenType.name())
                .claim(USERNAME_CLAIM, principal.getUsername())
                .claim(ROLES_CLAIM, extractAuthorities(principal.getAuthorities()));

        if (!tokenProperties.getAudience().isEmpty()) {
            builder.claim(AUDIENCE_CLAIM, tokenProperties.getAudience());
        }

        return builder.signWith(signingKey, signatureAlgorithm).compact();
    }

    private Claims parseClaims(String token) {
        try {
            return jwtParser.parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException ex) {
            SecurityContextHolder.clearContext();
            throw new JwtAuthenticationException("Token expired", ex);
        } catch (JwtException | IllegalArgumentException ex) {
            SecurityContextHolder.clearContext();
            throw new JwtAuthenticationException("Invalid JWT token", ex);
        }
    }

    private TokenType extractTokenType(Claims claims) {
        String type = claims.get(TOKEN_TYPE_CLAIM, String.class);
        if (!StringUtils.hasText(type)) {
            throw new JwtAuthenticationException("Token type is missing");
        }
        try {
            return TokenType.valueOf(type);
        } catch (IllegalArgumentException ex) {
            throw new JwtAuthenticationException("Unknown token type", ex);
        }
    }

    private Long extractUserId(Claims claims) {
        String subject = claims.getSubject();
        if (!StringUtils.hasText(subject)) {
            throw new JwtAuthenticationException("Token subject is missing");
        }
        try {
            return Long.parseLong(subject);
        } catch (NumberFormatException ex) {
            throw new JwtAuthenticationException("Token subject is invalid", ex);
        }
    }

    private List<String> extractAuthorities(Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    private enum TokenType {
        ACCESS,
        REFRESH
    }
}