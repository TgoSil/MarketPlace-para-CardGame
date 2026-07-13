package com.tiago.api_gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import com.tiago.api_gateway.utils.JwtUtil;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Claims;

@Component
public class JwtValidationGatewayFilterFactory extends AbstractGatewayFilterFactory<Object>{

    private final JwtUtil jwtUtil;

    public JwtValidationGatewayFilterFactory(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }


    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            String token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (token == null || !token.startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
            try {
                Claims claim = jwtUtil.validateToken(token.substring(7));
                String id = claim.getSubject();
                String cargo = claim.get("cargo", String.class);
                ServerHttpRequest modifiedRequest = exchange.getRequest()  
                                                            .mutate()
                                                            .header("User-Id", id)
                                                            .header("User-cargo", cargo)
                                                            .build();
                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            } catch(JwtException e) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        };
    }
    
}
