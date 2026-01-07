package bg.gemini.model;

import java.util.List;

public class GeminiModel {
    
    private final String name;
    private final String displayName;
    private final String description;
    private final List<String> supportedMethods;
    private final Integer inputTokenLimit;
    private final Integer outputTokenLimit;
    
    public GeminiModel(String name, String displayName, String description,List<String> supportedMethods, Integer inputTokenLimit,  Integer outputTokenLimit) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.supportedMethods = supportedMethods;
        this.inputTokenLimit = inputTokenLimit;
        this.outputTokenLimit = outputTokenLimit;
    }
    
    // Getters
    public String getName() {
        return name;
    }
    
    public String getModelId() {
        return name. replace("models/", "");
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public List<String> getSupportedMethods() {
        return supportedMethods;
    }
    
    public Integer getInputTokenLimit() {
        return inputTokenLimit;
    }
    
    public Integer getOutputTokenLimit() {
        return outputTokenLimit;
    }
    
    // Méthodes utilitaires
    public boolean supportsGenerateContent() {
        return supportedMethods.contains("generateContent");
    }
    
    public boolean supportsCountTokens() {
        return supportedMethods.contains("countTokens");
    }
    
    public String getApiUrl() {
        return "https://generativelanguage.googleapis.com/v1beta/models/" 
                + getModelId() + ":generateContent";
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("📦 ").append(displayName).append("\n");
        sb.append("   Model ID: ").append(getModelId()).append("\n");
        sb.append("   Description: ").append(description).append("\n");
        sb.append("   Méthodes:  ").append(String.join(", ", supportedMethods)).append("\n");
        if (inputTokenLimit != null) {
            sb.append("   Input tokens: ").append(inputTokenLimit).append("\n");
        }
        if (outputTokenLimit != null) {
            sb.append("   Output tokens: ").append(outputTokenLimit).append("\n");
        }
        sb.append("   URL: ").append(getApiUrl());
        return sb.toString();
    }
}