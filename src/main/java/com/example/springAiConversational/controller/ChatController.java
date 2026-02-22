package com.example.springAiConversational.controller;

import com.example.springAiConversational.service.ConversationalContextService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import javax.validation.constraints.Max;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*")
public class ChatController {


    private final OpenAiChatModel chatClient;
    private final ConversationalContextService contextService;
    ChatClient chatClient2;


    @Autowired
    public ChatController(OpenAiChatModel chatClient,ConversationalContextService contextService,ChatClient chatClient2) {
        this.chatClient = chatClient;
        this.contextService = contextService;
        this.chatClient2=chatClient2;
    }


    @GetMapping("/test")
    public String test() {
        return "Controller is working!";
    }

    @GetMapping("/contexts")
    public List<String> allContext() {
        return contextService.fetchAllContextIds();
    }

//    @GetMapping(value = "/chat", params = "contextId")
//    public Map<String,String> chat(@RequestParam(value = "contextId",defaultValue = "") String contextId,
//                                   @RequestParam(value = "message",defaultValue = "random facts") String message){
//       if(contextId.isEmpty()){
//           contextId= UUID.randomUUID().toString();
//       }
//       String prompt=contextService.preparePromptWithContextHistory(contextId,message);
//       return Map.of("Generated",chatClient.call(prompt));
//    }
@GetMapping(value = "/chat", params = "contextId")
public Map<String,String> chat(@RequestParam(value = "contextId",defaultValue = "") String contextId,
                               @RequestParam(value = "message",defaultValue = "random facts") String message){
    if(contextId.isEmpty()){
        contextId= UUID.randomUUID().toString();
    }
    try {
        String prompt = contextService.preparePromptWithContextHistory(contextId, message);
        String botReply = chatClient.call(prompt);
        Map<String, String> result = new HashMap<>();
        result.put("contextId", contextId);
        result.put("reply", botReply);
        return result;
    } catch (Exception e) {
        Map<String, String> errorResult = new HashMap<>();
        errorResult.put("contextId", contextId);
        errorResult.put("reply", "Sorry, something went wrong processing your request.");
        return errorResult;
    }


}
    @GetMapping(value = "/chatwithstream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Map<String, String>> chatStream(
            @RequestParam(value = "contextId", defaultValue = "") String contextId,
            @RequestParam(value = "message", defaultValue = "random facts") String message) {

        if (contextId.isEmpty()) {
            contextId = UUID.randomUUID().toString();
        }

        String finalContextId = contextId;

        try {
            String prompt = contextService.preparePromptWithContextHistory(finalContextId, message);

            return chatClient2.prompt()
                    .user(prompt)
                    .stream()
                    .content()
                    .map(chunk -> {
                        Map<String, String> response = new HashMap<>();
                        response.put("contextId", finalContextId);
                        response.put("reply", chunk);
                        return response;
                    })
                   // .doOnNext(chunk -> System.out.print(chunk.get("reply")))
                    .onErrorResume(e -> {
                        Map<String, String> error = new HashMap<>();
                        error.put("contextId", finalContextId);
                        error.put("reply", "Sorry, something went wrong.");
                        return Flux.just(error);
                    });

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("contextId", finalContextId);
            error.put("reply", "Sorry, something went wrong processing your request.");
            return Flux.just(error);
        }
    }
}
