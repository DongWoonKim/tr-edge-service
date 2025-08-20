package com.trevari.spring.tredgeservice.client;

import com.trevari.spring.tredgeservice.dto.ValidTokenRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AuthServiceClient {

    private final WebClient authClient;
    private static final String TOKEN_VALIDATE = "/api/auth/tokens/validate";

    /**
     * 토큰 검증 후 상태 코드 반환: 1(유효) / 2(무효) / -1(오류)
     */
    public Mono<Integer> validToken(String token) {
        token = token.replaceFirst("(?i)^Bearer ", "");

        return authClient.post()
                .uri(TOKEN_VALIDATE)
                .bodyValue(new ValidTokenRequestDTO(token))
                .retrieve()
                // 4xx → 무효(2), 5xx → 예외
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.empty())
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.error(new RuntimeException("auth 5xx")))
                .bodyToMono(Integer.class)
                .switchIfEmpty(Mono.just(2))   // 4xx → 2
                .onErrorResume(ex -> Mono.just(-1)); // 예외 → -1
    }

}
