package com.c2.ai.SpringAIIntro.serviceImpl;

import com.c2.ai.SpringAIIntro.model.Answer;
import com.c2.ai.SpringAIIntro.model.CustomResponseUsage;
import com.c2.ai.SpringAIIntro.model.GetCapitalRequest;
import com.c2.ai.SpringAIIntro.model.Question;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.web.multipart.MultipartFile;

public interface OpenAIService {

    public String getAnswer(String question);

    public ChatResponse getAnswerWithTokenString(String question);

    CustomResponseUsage getAnswerWithTokenCustom(String question);

    Answer getAnswerUsingTemplate(GetCapitalRequest getCapitalRequest);

    Answer getAnswerUsingTemplateCustomResponse(GetCapitalRequest getCapitalRequest);

    Answer getAnswerUsingTemplateJSONRes(GetCapitalRequest getCapitalRequest);

    Answer getAnswerUsingRag(Question question);

    byte[] getImage(Question question);

    byte[] getImage(String question);

    String getImageDescription(MultipartFile file);

    byte[] getTextToAudio(String question);

    byte[] getTextToAudio(Question question);

    String getAudioToText(MultipartFile file);

    void evictImage(String question);

    void evictAllImages();

    byte[] refreshCachedImage(String question);

    String clearVectorStore();

    String getAnswerUsingRag(String question);

    String getAnswerUsingRagHybrid(String question);

    // getAnswerUsingCustomChat(Question );
}
