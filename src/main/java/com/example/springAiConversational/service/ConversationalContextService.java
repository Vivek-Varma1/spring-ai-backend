package com.example.springAiConversational.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ConversationalContextService {
    List <String> fetchAllContextIds();
    String preparePromptWithContextHistory(final String contextId,final String message);
}
