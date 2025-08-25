package com.c2.ai.SpringAIIntro.controller;

import com.c2.ai.SpringAIIntro.model.Answer;
import com.c2.ai.SpringAIIntro.model.CustomResponseUsage;
import com.c2.ai.SpringAIIntro.model.GetCapitalRequest;
import com.c2.ai.SpringAIIntro.model.Question;
import com.c2.ai.SpringAIIntro.serviceImpl.OpenAIService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/ai")
@Tag(name = "Spring API Controller", description = "Endpoints to interact with multiple Open AI models, including chat, image generation, and audio processing, RAG, Vectorstore Embedding.")
public class AIController {

    @Autowired
    OpenAIService openAIService;


    @GetMapping("/")
    @Operation(
            summary = "Get answer from OpenAI for a given question",
            description = "Accepts a question as a query parameter and returns the AI-generated answer.",
            tags = {"AI Q&A"}
    )
    public String getAnswer(
            @Parameter (description = "The question to ask the AI", required = true)
            @RequestParam String question){
       return openAIService.getAnswer(question);
    }

    @GetMapping("/{question}")
    @Operation(
            summary = "Get answer from OpenAI for a given question",
            description = "Takes a question as a path variable and returns the AI-generated answer.",
            tags = {"AI Q&A"}
    )
    public String getAnswerPathVariable(@PathVariable String question){
        return openAIService.getAnswer(question);
    }

    @GetMapping("/token")
    @Operation(
            summary = "Get AI answer with token usage info",
            description = "Accepts a question as a query parameter and returns a ChatResponse object including answer text and lots of other metadata details like token usage information.",
            tags = {"AI Q&A"}
    )
    public ChatResponse getAnswerWithUsage(@RequestParam String question){

        return openAIService.getAnswerWithTokenString(question);
    }

    @GetMapping("/tokenUsage")
    @Operation(
            summary = "Get AI answer with custom token usage info",
            description = "Accepts a question as a query parameter and returns a CustomResponseUsage object including the AI answer and detailed token usage information.",
            tags = {"AI Q&A"}
    )
    public CustomResponseUsage getAnswerWithUsageCustom(@RequestParam String question){

        return openAIService.getAnswerWithTokenCustom(question);
    }

    @PostMapping("/capital")
    @Operation(
            summary = "Get answer using template",
            description = "Accepts a GetCapitalRequest object and returns an Answer generated using a predefined template in the AI service.",
            tags = {"AI Q&A"}
    )
    public Answer getAnswerUsingTemplate(@RequestBody GetCapitalRequest getCapitalRequest) {
        return openAIService.getAnswerUsingTemplate(getCapitalRequest);
    }

    @PostMapping("/capitalWithCustomResponse")
    @Operation(
            summary = "Get answer using template with custom response",
            description = "Accepts a GetCapitalRequest object and returns a customized Answer object generated using a predefined template in the AI service.",
            tags = {"AI Q&A"},
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Request containing the state or country name",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Capital Request",
                                    value = "{ \"stateOrCountry\": \"France\" }"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "AI-generated custom answer based on template",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = "{ \"answer\": \"The capital of France is Paris.\", \"additionalInfo\": \"Population approx 2.1 million\" }"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request"
                    )
            }
    )
    public Answer getAnswerUsingTemplateCustomResponse(@RequestBody GetCapitalRequest getCapitalRequest) {
        return openAIService.getAnswerUsingTemplateCustomResponse(getCapitalRequest);
    }

    @PostMapping("/capitalUsingTemplateJSONRes")
    @Operation(
            summary = "Get answer using template with JSON response",
            description = "Accepts a GetCapitalRequest object and returns an Answer object generated using a predefined template in JSON format.",
            tags = {"AI Q&A"}
    )
    public Answer getAnswerUsingTemplateJSONResponse(@RequestBody GetCapitalRequest getCapitalRequest) {
        return openAIService.getAnswerUsingTemplateJSONRes(getCapitalRequest);
    }

    @PostMapping("/RAG/getAnswerUsingRag")
    @Operation(
            summary = "Get answer using RAG (Retrieval-Augmented Generation)",
            description = "Accepts a Question object and returns an Answer generated using the RAG approach, combining retrieval with AI generation.",
            tags = {"AI Q&A RAG"}
    )
    public Answer getAnswerUsingRag(@RequestBody Question question) {
        return openAIService.getAnswerUsingRag(question);
    }

    @PostMapping("/RAG/chatBotUsingRag")
    @Operation(
            summary = "Get answer using RAG (Retrieval-Augmented Generation)",
            description = "Accepts a Question object and returns an Answer generated using the RAG approach, combining retrieval with AI generation.",
            tags = {"AI Q&A RAG"}
    )
    public String getAnswerUsingRag(@RequestParam String question) {
        //return openAIService.getAnswerUsingRag(question);
        return openAIService.getAnswerUsingRagHybrid(question);
    }

    /*@PostMapping("/RAG/askChatBot")
    @Operation(
            summary = "Get answer using RAG (Retrieval-Augmented Generation)",
            description = "Accepts a Question object and returns an Answer generated using the RAG approach, combining retrieval with AI generation.",
            tags = {"AI Q&A"}
    )
    public Answer getAnswerUsingCustomChat(@RequestBody Question question) {
        return openAIService.getAnswerUsingCustomChat(question);
    }*/

    @PostMapping(value = "/image/getImage", produces = MediaType.IMAGE_PNG_VALUE)
    @Operation(
            summary = "Generate an image from a question/prompt",
            description = "Accepts a Question object and returns an AI-generated image in PNG format.",
            tags = {"AI Image Generation"}
    )
    public byte[] getImage(@RequestBody Question question) {
        log.info("Fetching image for question : {}", question.question());
        return openAIService.getImage(question);
    }


    @GetMapping(value = "/image/getImage", produces = MediaType.IMAGE_PNG_VALUE)
    @Operation(
            summary = "Generate an image from a query parameter",
            description = "Accepts a question/prompt as a query parameter and returns an AI-generated image in PNG format.",
            tags = {"AI Image Generation"}
    )
    public byte[] getImageQP(@RequestParam String question) {
        log.info("Fetching image for question QP: {}", question);
        // This method is cached to improve performance for frequently requested images.
        // The cache key is the question string, allowing for quick retrieval of previously generated images.
        return openAIService.getImage(question);
    }

    @PostMapping(value = "/image/getImageDescription", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Get image description",
            description = "Accepts an image file and returns a description of the image.",
            tags = {"AI Image Processing"}
    )
    public String getImageDescription(@RequestParam("file") MultipartFile file) {
        return openAIService.getImageDescription(file);
    }

    @PostMapping(value ="/speech/textToAudio", produces = "audio/mpeg")
    @Operation(
            summary = "Convert text to audio",
            description = "Accepts a question as a request body or query parameter and returns the audio representation of the text.",
            tags = {"AI Audio Processing"}
    )
    public byte[] getTextToAudio(@RequestBody Question question) {
        return openAIService.getTextToAudio(question);
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping(value ="/speech/textToAudio", produces = "audio/mpeg")
    @Operation(
            summary = "Convert text to audio",
            description = "Accepts a question as a query parameter and returns the audio representation of the text.",
            tags = {"AI Audio Processing"}
    )
    public byte[] getTextToAudio(@RequestParam String question) {
        return openAIService.getTextToAudio(question);
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping(value = "/speech/getAudioToText", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Convert audio to text",
            description = "Accepts an audio file and returns the transcribed text.",
            tags = {"AI Audio Processing"}
    )
    public String getAudioToText(@RequestParam("file") MultipartFile file) {
        return openAIService.getAudioToText(file);
    }

    @DeleteMapping("/image/evict")
    @Operation(
            summary = "Evict a specific image from cache",
            description = "Clears the cache for a specific question, allowing for fresh image generation.",
            tags = {"AI Image Cache Management"}
    )
    public String evictImage(@RequestParam String question) {
        openAIService.evictImage(question);
        return "Cache cleared for: " + question;
    }

    @DeleteMapping("/image/evictAll")
    @Operation(
            summary = "Evict all images from cache",
            description = "Clears the entire image cache, allowing for fresh image generation for all questions.",
            tags = {"AI Image Cache Management"}
    )
    public String evictAllImages() {
        openAIService.evictAllImages();
        return "All cache cleared!";
    }

    @PostMapping(value = "/refreshCachedImage", produces = MediaType.IMAGE_PNG_VALUE)
    @Operation(
            summary = "Refresh cached image",
            description = "Regenerates and returns a fresh image for a given question, updating the cache.",
            tags = {"AI Image Cache Management"}
    )
    public byte[] refreshCachedImage(@RequestParam String question) {
        return openAIService.refreshCachedImage(question);
    }


    @DeleteMapping("/clearVectorStore")
    @Operation(
            summary = "Clear the vector store",
            description = "Clears the entire vector store, removing all stored vectors and allowing for fresh data insertion.",
            tags = {"AI Vector Store Management"}
    )
    public String clearStore() {
        openAIService.clearVectorStore();
        return "Vector store cleared!";
    }

    @PostMapping("/uploadDocument")
    public ResponseEntity<String> uploadDocument(@RequestParam("file") MultipartFile file) throws IOException {
        // 1. Extract text with Tika

        openAIService.uploadDocument(file);
        return ResponseEntity.ok("Uploaded " + file.getOriginalFilename() + " successfully");
    }
}
