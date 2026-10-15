package com.cleanloop.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AiPredictionDto(
        boolean success,
        String prediction,
        BigDecimal confidence,
        String modelVersion,
        List<ClassProbability> probabilities,
        String message
) {
    public record ClassProbability(
            String className,
            BigDecimal probability
    ) {}
}
