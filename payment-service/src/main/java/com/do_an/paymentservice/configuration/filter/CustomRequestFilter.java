package com.do_an.paymentservice.configuration.filter;

import com.do_an.paymentservice.exceptions.enums.ErrorCode;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class CustomRequestFilter extends OncePerRequestFilter {

    @NonFinal
    @Value("${publicKey}")
    String publicKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // System.out.println("---- Request Info ----");
        // System.out.println("Method: " + request.getMethod());
        // System.out.println("URI: " + request.getRequestURI());
        // System.out.println("Query String: " + request.getQueryString());
        // System.out.println("Remote Addr: " + request.getRemoteAddr());
        // System.out.println("Content Type: " + request.getContentType());

        // Enumeration<String> headerNames = request.getHeaderNames();
        // while (headerNames.hasMoreElements()) {
        //     String headerName = headerNames.nextElement();
        //     String headerValue = request.getHeader(headerName);
        //     System.out.println(headerName + ": " + headerValue);
        // }


        String authHeader = request.getHeader("Authorization") != null
                ? request.getHeader("Authorization")
                : request.getHeader("authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                if(!this.validate(token)) {
                    sendError(response, ErrorCode.UNAUTHENTICATED);
                    return;
                }

                SignedJWT signedJWT = SignedJWT.parse(token);
                JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
                List<String> roles = (List<String>) claims.getClaim("roles");
                Set<String> roleSet = roles != null ? Set.copyOf(roles) : Set.of();
                UserDTO user = UserDTO.builder()
                        .id(UUID.fromString(claims.getStringClaim("user_id")))
                        .fullname(claims.getStringClaim("full_name"))
                        .phone(claims.getStringClaim("phone"))
                        .roles(roleSet)
                        .primaryRole(claims.getStringClaim("phone"))
                        .build();
                var authorities = user.getRoles().stream()
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                        .toList();

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                user, token, authorities);
                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authentication);

            } catch (Exception e) {
                sendError(response, ErrorCode.UNAUTHENTICATED);
                return;
            }
        } else {
            // Skip prelight request

//            if (!"OPTIONS".equalsIgnoreCase(request.getMethod())) {
//                sendError(response, ErrorCode.UNAUTHENTICATED);
//                return;
//            }

        }

        filterChain.doFilter(request, response);
    }

    private void sendError(HttpServletResponse response, ErrorCode errorCode)
            throws IOException {
        String body = String.format(
                "{\"code\": %d, \"message\": \"%s\"}",
                errorCode.getCode(),
                errorCode.getMessage());

        response.setStatus(errorCode.getHttpStatusCode().value());
        response.setContentType("application/json");
        response.getWriter().write(body);
    }

    public Boolean validate(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new RSASSAVerifier(loadPublicKey());

            if (!(signedJWT.verify(verifier))) {
                return false;
            }
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    private RSAPublicKey loadPublicKey() throws Exception {
        byte[] encoded = Base64.getDecoder().decode(publicKey);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);

        return (RSAPublicKey) keyFactory.generatePublic(keySpec);
    }

}