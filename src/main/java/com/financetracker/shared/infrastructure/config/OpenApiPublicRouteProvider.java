package com.financetracker.shared.infrastructure.config;

import com.financetracker.shared.api.config.PublicRouteProvider;
import org.springframework.stereotype.Component;

@Component
public class OpenApiPublicRouteProvider implements PublicRouteProvider {
    @Override
    public String[] getPublicRoutes() {
        return new String[]{
                "/v3/api-docs",
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html"
        };
    }
}
