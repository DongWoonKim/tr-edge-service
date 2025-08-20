package com.trevari.spring.tredgeservice.filter;

import com.trevari.spring.tredgeservice.client.AuthServiceClient;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@Order(0)
@Component
public class PreGatewayFilter extends AbstractGatewayFilterFactory<PreGatewayFilter.Config> {

    private final AuthServiceClient authServiceClient;

    public PreGatewayFilter(AuthServiceClient authServiceClient) {
        super(Config.class);
        this.authServiceClient = authServiceClient;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            exchange.getRequest().getHeaders().forEach((key, value) -> {
                log.info("Header: {} => {}", key, value);
            });

            String token = exchange.getRequest().getHeaders().getFirst(AUTHORIZATION);

            // 토큰 유효성 검사
            if (token == null || !token.startsWith(config.getTokenPrefix())) {
                exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            return authServiceClient.validToken(token)
                    .flatMap(valided -> {
                        log.info("token valid {}", valided);
                        if (valided == 2) {
                            exchange.getResponse().setRawStatusCode(419);
                            return exchange.getResponse().setComplete();
                        } else if (valided == -1) {
                            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.name()));
                            return exchange.getResponse().setComplete();
                        }

                        return chain.filter(exchange);
                    })
                    .onErrorResume(e -> {
                        log.error("Error during token validation", e);
                        exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
                        return exchange.getResponse().setComplete();
                    });
        };
    }

    @Getter
    @Setter
    public static class Config {
        private String tokenPrefix = "Bearer ";
        private int authenticationTimeout = 419;
    }
}
