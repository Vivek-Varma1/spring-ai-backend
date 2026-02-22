package com.example.springAiConversational.controller;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("")
public class BasicChatController {

    private final OpenAiChatModel chatClient;
    @Autowired
    public BasicChatController(OpenAiChatModel chatClient) {
        this.chatClient = chatClient;
    }
    @GetMapping(value = "/chat", params = "!contextId")
    public Map<String,String> chat(@RequestParam(value = "message",defaultValue = "random facts") String message){


        return Map.of("Generated",chatClient.call(message));
    }


}
