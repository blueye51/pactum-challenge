package com.pareto.pactum_challenge.service;

public enum PromptMode {
    CHAT("prompts/chat.txt");

    private final String resourcePath;

    PromptMode(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String getResourcePath() {
        return resourcePath;
    }
}
