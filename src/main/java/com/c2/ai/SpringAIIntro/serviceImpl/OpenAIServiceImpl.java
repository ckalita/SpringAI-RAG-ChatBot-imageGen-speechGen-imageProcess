package com.c2.ai.SpringAIIntro.serviceImpl;

import com.c2.ai.SpringAIIntro.config.VectorStoreProperties;
import com.c2.ai.SpringAIIntro.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.model.Media;
import org.springframework.ai.openai.*;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.audio.speech.SpeechModel;
import org.springframework.ai.openai.audio.speech.SpeechPrompt;
import org.springframework.ai.openai.audio.speech.SpeechResponse;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Slf4j
@Component
public class OpenAIServiceImpl implements OpenAIService{

    @Autowired
    ChatModel chatModel;

    @Autowired
    ImageModel imageModel;

    @Autowired
    SpeechModel speechModel;

    @Autowired
    OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel;

    @Autowired
    SimpleVectorStore vectorStore;

    @Value("classpath:templates/get-capital-prompt.st")
    private Resource getCapitalPrompt;

    @Value("classpath:templates/get-capital-with-info-prompt.st")
    private Resource getCapitalWithInfoCustom;

    @Value("classpath:templates/get-capital-prompt-JSON.st")
    private Resource getCapitalPromptJSON;

    @Value("classpath:/templates/rag-prompt-template-meta.st")
    private Resource ragPromptTemplate;

    @Autowired
    private VectorStoreProperties vectorStoreProperties;


    /*public OpenAIServiceImpl(ChatModel chatModel, ImageModel imageModel, SpeechModel speechModel, SimpleVectorStore vectorStore) {
        this.chatModel = chatModel;
        this.imageModel = imageModel;
        this.speechModel = speechModel;
        this.vectorStore = vectorStore;
    }*/


    @Override
    public String getAnswer(String question) {
        //PromptTemplate promptTemplate = new PromptTemplate(question);
        Prompt prompt = new Prompt(question);

        ChatResponse chatResponse = chatModel.call(prompt);

        return chatResponse.getResult().getOutput().getText();
    }

    public ChatResponse getAnswerWithTokenString(String question) {
        /*PromptTemplate promptTemplate = new PromptTemplate(question);
        Prompt p = promptTemplate.create();*/
        Prompt prompt = new Prompt(question);

        ChatResponse chatResponse = chatModel.call(prompt);
        return chatResponse;
    }

    public CustomResponseUsage getAnswerWithTokenCustom(String question) {
        //PromptTemplate promptTemplate = new PromptTemplate(question);
        Prompt prompt = new Prompt(question);

        ChatResponse chatResponse = chatModel.call(prompt);

        Usage springAIUsage = chatResponse.getMetadata().getUsage();

        ObjectMapper objectMapper = new ObjectMapper();

        //TokenUsage tokenUsage = objectMapper.convertValue(springAIUsage, TokenUsage.class);


        TokenUsage tokenUsage = new TokenUsage(springAIUsage.getPromptTokens(), springAIUsage.getGenerationTokens(),
                springAIUsage.getTotalTokens());
        System.out.println("tokenUsage object is : "+tokenUsage);

        return new CustomResponseUsage(chatResponse.getResult().getOutput().getText(),
                tokenUsage);
    }

    @Override
    public Answer getAnswerUsingTemplate(GetCapitalRequest getCapitalRequest) {
        PromptTemplate promptTemplate = new PromptTemplate(getCapitalPrompt);
        Prompt prompt = promptTemplate.create(Map.of("stateOrCountry", getCapitalRequest.stateOrCountry()));
        ChatResponse response = chatModel.call(prompt);

        return new Answer(response.getResult().getOutput().getText());
    }

    @Override
    public Answer getAnswerUsingTemplateCustomResponse(GetCapitalRequest getCapitalRequest) {

        PromptTemplate promptTemplate = new PromptTemplate(getCapitalWithInfoCustom);
        Prompt prompt = promptTemplate.create(Map.of("stateOrCountry", getCapitalRequest.stateOrCountry()));
        ChatResponse chatResponse = chatModel.call(prompt);

        return new Answer(chatResponse.getResult().getOutput().getText());
    }

    @Override
    public Answer getAnswerUsingTemplateJSONRes(GetCapitalRequest getCapitalRequest) {
        PromptTemplate promptTemplate = new PromptTemplate(getCapitalPromptJSON);
        Prompt prompt = promptTemplate.create(Map.of("stateOrCountry", getCapitalRequest.stateOrCountry()));
        ChatResponse chatResponse = chatModel.call(prompt);

        System.out.println(chatResponse.getResult().getOutput().getText());

        ObjectMapper   objectMapper = new ObjectMapper();
        String responseString;

        try {
            JsonNode jsonNode = objectMapper.readTree(chatResponse.getResult().getOutput().getText());
            responseString = jsonNode.get("answer").asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return new Answer(responseString);
    }

    @Override
    public Answer getAnswerUsingRag(Question question) {
        List<Document> documents = vectorStore.similaritySearch(SearchRequest.builder()
                .query(question.question()).topK(9).build());
        List<String> contentList = documents.stream().map(Document::getContent).toList();

        PromptTemplate promptTemplate = new PromptTemplate(ragPromptTemplate);
        Prompt prompt = promptTemplate.create(Map.of("input", question.question(), "documents",
                String.join("\n", contentList)));

        contentList.forEach(System.out::println);

        ChatResponse response = chatModel.call(prompt);

        return new Answer(response.getResult().getOutput().getContent());
    }

    @Override
    public byte[] getImage(Question question) {
        log.info("inside getImage serviceImpl");
        var imageOptions = OpenAiImageOptions.builder()
                .withHeight(1024).withWidth(1792)
                .withResponseFormat("b64_json")
                .withModel("dall-e-3")
                .withQuality("hd") //default standard
                //.withStyle("natural") //default vivid
                .build();

        ImagePrompt imagePrompt = new ImagePrompt(question.question(), imageOptions);

        var imageResponse = imageModel.call(imagePrompt);
        //return imageResponse.getResult().getOutput().getB64Json().getBytes();
        return Base64.getDecoder().decode(imageResponse.getResult().getOutput().getB64Json());
    }

    @Override
    @Cacheable(value = "imageCache", key = "#question")
    public byte[] getImage(String question) {
        log.info("Generating image from openai. Cache Miss");
        log.info("inside getImage with QueryParam serviceImpl");
        var imageOptions = OpenAiImageOptions.builder()
                .withHeight(1024).withWidth(1792)
                .withResponseFormat("b64_json")
                .withModel("dall-e-3")
                .withQuality("hd") //default standard
                //.withStyle("natural") //default vivid
                .build();

        ImagePrompt imagePrompt = new ImagePrompt(question, imageOptions);

        var imageResponse = imageModel.call(imagePrompt);
        return Base64.getDecoder().decode(imageResponse.getResult().getOutput().getB64Json());
    }

    /**
     * This method is used to get the description of an image.
     * It takes a MultipartFile as input and returns a String description of the image.
     *
     * @param file the image file to be described
     * @return a String description of the image
     */
    @Override
    public String getImageDescription(MultipartFile file) {
        log.info("inside getImageDescription serviceImpl");
        OpenAiChatOptions chatOptions = OpenAiChatOptions.builder()
                .model(OpenAiApi.ChatModel.GPT_4_O.getValue())
                .build();

        var userMessage = new UserMessage("Explain what do you see in this picture?",
                List.of(new Media
                        (MimeTypeUtils.IMAGE_JPEG, file.getResource())));

        ChatResponse response = chatModel.call(new Prompt(List.of(userMessage), chatOptions));
        log.info("Response is : {}", response.getResult().getOutput().getContent());
        return response.getResult().getOutput().getContent();
    }

    @Override
    public byte[] getTextToAudio(String question) {
        OpenAiAudioSpeechOptions speechOptions = OpenAiAudioSpeechOptions.builder()
                .voice(OpenAiAudioApi.SpeechRequest.Voice.ALLOY)
                .speed(1.0f)
                .responseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3)
                .model(OpenAiAudioApi.TtsModel.TTS_1.value)
                .build();

        SpeechPrompt speechPrompt = new SpeechPrompt(question,
                speechOptions);

        SpeechResponse response = speechModel.call(speechPrompt);

        return response.getResult().getOutput();
    }

    @Override
    public byte[] getTextToAudio(Question question) {
        OpenAiAudioSpeechOptions speechOptions = OpenAiAudioSpeechOptions.builder()
                .voice(OpenAiAudioApi.SpeechRequest.Voice.ALLOY)
                .speed(1.0f)
                .responseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3)
                .model(OpenAiAudioApi.TtsModel.TTS_1.value)
                .build();

        SpeechPrompt speechPrompt = new SpeechPrompt(question.question(),
                speechOptions);

        SpeechResponse response = speechModel.call(speechPrompt);

        return response.getResult().getOutput();
    }

    @Override
    public String getAudioToText(MultipartFile file) {
        OpenAiAudioTranscriptionOptions transcriptionOptions = OpenAiAudioTranscriptionOptions.builder()
                .responseFormat(OpenAiAudioApi.TranscriptResponseFormat.VERBOSE_JSON)
                .language("en")
                .temperature(0f)
                .build();

        AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(file.getResource(), transcriptionOptions);

        AudioTranscriptionResponse response = openAiAudioTranscriptionModel.call(prompt);

        return response.getResult().getOutput();
    }

    @Override
    @CacheEvict(value = "imageCache", key = "#question")
    public void evictImage(String question) {
        log.info("Evicting image from cache for question: {}", question);

    }

    @Override
    @CacheEvict(value = "imageCache", allEntries = true)
    // This method will clear all entries in the imageCache
    public void evictAllImages() {
        log.info("Evicting all images from cache");
        // This method will clear all entries in the imageCache
        // No additional logic needed, as the annotation handles it
    }

    @Override
    @CachePut(value = "imageCache", key = "#question")
    public byte[] refreshCachedImage(String question) {
        return new byte[0];
    }

    @Override
    public String clearVectorStore() {
        log.info("Clearing vector store");
        File vectorStoreFile = new File(vectorStoreProperties.getVectorStorePath());

        log.info("Entering vectorStore Bean creation");
        if (vectorStoreFile.exists()) {
            vectorStoreFile.delete();
            log.info("Vector store cleared successfully");
            return "Vector store file deleted!";
        }
        log.info("No vector store file found.");
        return "No vector store file found.";
    }

    @Override
    public String getAnswerUsingRag(String question) {
        log.info("getAnswerUsingRag : Performing RAG search for question: {}", question);
        List<Document> documents = vectorStore.similaritySearch(SearchRequest.builder()
                .query(question).topK(9).build());
        List<String> contentList = documents.stream().map(Document::getContent).toList();

        PromptTemplate promptTemplate = new PromptTemplate(ragPromptTemplate);
        Prompt prompt = promptTemplate.create(Map.of("input", question, "documents",
                String.join("\n", contentList)));

        //contentList.forEach(System.out::println);

        ChatResponse response = chatModel.call(prompt);

        return response.getResult().getOutput().getText();

    }

    public String getAnswerUsingRagHybrid(String question) {
        log.info("getAnswerUsingRagHybrid : Incoming question: {}", question);

        // Try vector store retrieval
        List<Document> docs = vectorStore.similaritySearch(question);
        String context = "";

        if (docs != null && !docs.isEmpty()) {
            context = docs.stream()
                    .map(Document::getContent)
                    .collect(Collectors.joining("\n---\n"));
        }

        String prompt = context.isEmpty()
                ? question // fallback: no context, ask directly
                : """
                  Use the following context to answer the question. 
                  If the context does not help, still try to answer.
                  Do not mention the context or whether it was used.\s
                  Just answer the question directly.

                  Context:
                  %s

                  Question: %s
                  """.formatted(context, question);

        ChatResponse response = chatModel.call(new Prompt(new UserMessage(prompt)));
        return response.getResult().getOutput().getContent();
    }

    @Override
    public void uploadDocument(MultipartFile file) {
        log.info("Uploading document: {}", file.getOriginalFilename());
        try {
            // 1. Read using TikaDocumentReader
            TikaDocumentReader reader = new TikaDocumentReader(file.getResource());
            List<Document> docs = reader.get();

            // 2. Split into smaller chunks
            TextSplitter splitter = new TokenTextSplitter();
            List<Document> chunks = splitter.apply(docs);

            // 3. Add to vector store
            vectorStore.add(chunks);

            // 4. (Optional, only for local file-based store)
            if (vectorStoreProperties.getVectorStorePath() != null) {
                vectorStore.save(new File(vectorStoreProperties.getVectorStorePath()));
            }

            log.info("Uploaded {} into vector store as {} chunks.",
                    file.getOriginalFilename(), chunks.size());
        } catch (Exception e) {
            log.error("Error uploading document: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload document", e);
        }
    }


    private String askLLMDirect(String input, boolean withContext) {
        ChatResponse response = chatModel.call(new Prompt(input));
        String answer = response.getResult().getOutput().getContent();

        if (!withContext) {
            // Optional transparency
            /*return "📘 *Note: This answer comes from general knowledge (outside of the uploaded PDFs).* \\n\\n"
                    + answer;*/
            return answer;
        }
        return answer;
    }

}
