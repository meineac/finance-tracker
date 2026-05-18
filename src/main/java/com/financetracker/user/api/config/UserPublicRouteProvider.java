package com.financetracker.user.api.config;

import com.financetracker.shared.api.config.PublicRouteProvider;
import org.springframework.stereotype.Component;

@Component
public class UserPublicRouteProvider implements PublicRouteProvider {
    @Override
    public String[] getPublicRoutes() {
        return new String[] {
                "/api/users/register",
                "/api/users/login"
        };
    }
}
