package com.forehapp.store.security.jwt;

import tools.jackson.databind.ObjectMapper;
import com.forehapp.store.authModule.application.dto.LoginRequestDto;
import com.forehapp.store.security.config.UserDetailsImpl;
import com.forehapp.store.userModule.domain.model.StoreProfile;
import com.forehapp.store.userModule.domain.model.StoreRole;
import com.forehapp.store.userModule.domain.ports.out.IStoreProfileDao;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final IStoreProfileDao storeProfileDao;

    public JwtAuthenticationFilter(IStoreProfileDao storeProfileDao) {
        this.storeProfileDao = storeProfileDao;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        try {
            LoginRequestDto credentials = new ObjectMapper()
                    .readValue(request.getReader(), LoginRequestDto.class);
            logger.info("Login attempt for: {}", credentials.getEmail());
            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            credentials.getEmail(),
                            credentials.getPassword(),
                            Collections.emptyList()
                    )
            );
        } catch (IOException e) {
            throw new BadCredentialsException("Invalid credentials format");
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult)
            throws IOException, ServletException {
        UserDetailsImpl userDetails = (UserDetailsImpl) authResult.getPrincipal();

        StoreProfile storeProfile = storeProfileDao.findByUserId(userDetails.getUser().getId()).orElseGet(() -> {
            StoreProfile profile = new StoreProfile();
            profile.setUser(userDetails.getUser());
            profile.getRoles().add(StoreRole.CUSTOMER);
            StoreProfile saved = storeProfileDao.save(profile);
            logger.info("StoreProfile auto-created on login for userId: {}", userDetails.getUser().getId());
            return saved;
        });

        String accessToken = JwtUtil.createToken(userDetails.getUsername(), userDetails.getAuthorities());
        String refreshToken = JwtUtil.createRefreshToken(userDetails.getUsername(), userDetails.getAuthorities());

        List<String> storeRoles = storeProfile.getRoles().stream()
                .map(Enum::name)
                .toList();

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(new ObjectMapper().writeValueAsString(Map.of(
                "access_token", accessToken,
                "refresh_token", refreshToken,
                "userId", userDetails.getUser().getId(),
                "name", userDetails.getUser().getName(),
                "email", userDetails.getUser().getEmail(),
                "storeRoles", storeRoles
        )));
        response.getWriter().flush();
        logger.info("Login successful for userId: {}", userDetails.getUsername());
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException, ServletException {
        logger.info("Login failed: {}", failed.getMessage());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"error\": \"Invalid credentials\"}");
    }
}
