package com.c2.ai.SpringAIIntro.serviceImpl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OpenAIServiceImplTest {

    @Autowired
    OpenAIService openAIService;

    //@Test
    void getAnswer() {
        String response = openAIService.getAnswer("prime minister of india" );
        System.out.println("Response is : " + response);

    }

    //@Test
    void getAnswer1() {
        String response = openAIService.getAnswer("capital of india" );
        System.out.println("Response is : " + response);

    }
}