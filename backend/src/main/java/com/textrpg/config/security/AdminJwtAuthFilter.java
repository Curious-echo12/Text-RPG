package com.textrpg.config.security;

import com.textrpg.entity.AdminUser;
import com.textrpg.mapper.AdminUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class AdminJwtAuthFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final AdminUserMapper adminMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 只处理 /api/admin/ 路径（排除 /api/admin/login）
        String path = request.getRequestURI();
        if (path.startsWith("/api/admin/") && !path.equals("/api/admin/login")) {
            String header = request.getHeader("Authorization");
            if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
                String token = header.substring(7);
                if (jwtUtil.validateToken(token)) {
                    try {
                        Long adminId = jwtUtil.getUserId(token);
                        AdminUser admin = adminMapper.selectById(adminId);
                        if (admin != null) {
                            UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(adminId, null, Collections.emptyList());
                            SecurityContextHolder.getContext().setAuthentication(auth);
                        }
                    } catch (Exception ignored) {}
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
