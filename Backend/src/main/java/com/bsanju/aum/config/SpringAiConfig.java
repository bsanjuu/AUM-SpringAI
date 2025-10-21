package com.bsanju.aum.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class SpringAiConfig {

    @Value("${spring.ai.openai.api-key:}")
    private String openAiApiKey;

    @Value("${spring.ai.ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Bean
    @Primary
    @ConditionalOnProperty(name = "app.ai.provider", havingValue = "openai", matchIfMissing = true)
    public ChatClient openAiChatClient(OpenAiChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultOptions(options -> options
                        .withModel("gpt-3.5-turbo")
                        .withTemperature(0.7)
                        .withMaxTokens(1000))
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "app.ai.provider", havingValue = "ollama")
    public ChatClient ollamaChatClient(OllamaChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultOptions(options -> options
                        .withModel("llama2")
                        .withTemperature(0.7))
                .build();
    }
}