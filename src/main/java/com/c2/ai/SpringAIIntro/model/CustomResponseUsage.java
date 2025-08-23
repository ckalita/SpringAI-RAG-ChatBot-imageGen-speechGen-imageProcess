package com.c2.ai.SpringAIIntro.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomResponseUsage {
    private String response;
    private TokenUsage usage;

}
