package bg.gemini.client;

import com.google.gson.Gson;
import com. google.gson.JsonObject;

import bg.commun.ProviderGeminiKey;

import com.google.gson.JsonArray;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class GeminiClient {
    
    private static final String GEMINI_API_URL_BASE = "https://generativelanguage.googleapis.com/v1beta/models/";
    //https://generativelanguage.googleapis.com/v1beta/models/{MODEL_ID}:generateContent?key={API_KEY}
    private static String MODEL ="gemini-pro-latest" ;//"gemini-2.5-flash";
    private static final String GEMINI_API_URL = GEMINI_API_URL_BASE+MODEL+":generateContent";
    private final String apiKey = ProviderGeminiKey.getInstance().getKeyGemini();
    private final HttpClient httpClient;
    private final Gson gson;
    
    public GeminiClient() {
        
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.gson = new Gson();
    }
    
    /**
     * Envoie un prompt à Gemini avec un schéma JSON attendu
     * @param prompt Le prompt à envoyer
     * @param responseSchema Le schéma JSON de la réponse attendue
     * @return La réponse de Gemini
     */
    public GeminiResponse sendRequest(String prompt, JsonSchema responseSchema) throws Exception {
        
        // Construire le corps de la requête
        JsonObject requestBody = buildRequestBody(prompt, responseSchema);
        
        // Créer la requête HTTP
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GEMINI_API_URL + "?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();
        
        // Envoyer la requête
        HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());
        
        // Vérifier le code de statut
        if (response. statusCode() != 200) {
            throw new RuntimeException("Erreur API Gemini:  " + response.statusCode() 
                    + " - " + response.body()+" - GEMINI_API_URL : "+GEMINI_API_URL);
        }
        
        // Parser la réponse
        return parseResponse(response.body());
    }
    
    /**
     * Construit le corps de la requête JSON
     */
    private JsonObject buildRequestBody(String prompt, JsonSchema responseSchema) {
        JsonObject requestBody = new JsonObject();
        
        // Ajouter le contenu
        JsonArray contents = new JsonArray();
        JsonObject content = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject part = new JsonObject();
        part.addProperty("text", prompt);
        parts.add(part);
        content.add("parts", parts);
        contents.add(content);
        requestBody.add("contents", contents);
        
        // Ajouter la configuration de génération si un schéma est fourni
        if (responseSchema != null) {
            JsonObject generationConfig = new JsonObject();
            generationConfig.addProperty("temperature", 0.7);
            generationConfig.addProperty("topK", 40);
            generationConfig.addProperty("topP", 0.95);
            generationConfig. addProperty("maxOutputTokens", 65536);
            
            // Ajouter le schéma de réponse (response_mime_type et response_schema)
            generationConfig.addProperty("response_mime_type", "application/json");
            generationConfig.add("response_schema", responseSchema. toJsonObject());
            
            requestBody. add("generationConfig", generationConfig);
        }
        
        return requestBody;
    }
    
    /**
     * Parse la réponse de Gemini
     */
    private GeminiResponse parseResponse(String responseBody) {
        JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
        
        String text = jsonResponse
                .getAsJsonArray("candidates")
                .get(0).getAsJsonObject()
                .getAsJsonObject("content")
                .getAsJsonArray("parts")
                .get(0).getAsJsonObject()
                .get("text").getAsString();
        
        return new GeminiResponse(text, responseBody);
    }
}