package com.trevari.spring.tredgeservice.dto;

import lombok.Builder;

@Builder
public record ValidTokenRequestDTO(
        String token
) {}
