package bg.gemini.model;

import com.google.gson.Gson;
import com.google.gson. JsonArray;
import com.google.gson.JsonObject;

import bg.commun.ProviderGeminiKey;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class ProviderListGeminiModels {
	
	 String apiKey =ProviderGeminiKey.getInstance().getKeyGemini();
	 List<GeminiModel> listModel;
	private ProviderListGeminiModels() {
		 try {
	         this.listModel=   fetchListModels();
	        } catch (Exception e) {
	            System.err.println("Erreur:  " + e.getMessage());
	            e.printStackTrace();
	        }
	}
	
	private static ProviderListGeminiModels instance;
	
	
    
    public static ProviderListGeminiModels getInstance() {
    	if (instance == null) {
    		instance = new ProviderListGeminiModels();
    	}
		return instance;
		
	}


    
    public  List<GeminiModel> fetchListModels() throws Exception {
    	
    	List<GeminiModel> list = new ArrayList<GeminiModel>();
        // URL de l'API pour lister les modèles
        String url = "https://generativelanguage.googleapis.com/v1beta/models?key=" + apiKey;
        
        // Créer le client HTTP
        HttpClient client = HttpClient.newHttpClient();
        
        // Créer la requête
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .GET()
                .build();
        
        // Envoyer la requête
        HttpResponse<String> response = client.send(request, 
                HttpResponse.BodyHandlers.ofString());
        
        // Vérifier le statut
        if (response.statusCode() != 200) {
            throw new RuntimeException("Erreur API:  " + response.statusCode() 
                    + " - " + response.body());
        }
        
        // Parser la réponse JSON
        Gson gson = new Gson();
        JsonObject jsonResponse = gson. fromJson(response.body(), JsonObject.class);
        JsonArray models = jsonResponse.getAsJsonArray("models");
        
        // Afficher les modèles
       
        
        for (int i = 0; i < models.size(); i++) {
            JsonObject model = models.get(i).getAsJsonObject();
            
            String name = model.get("name").getAsString();
            String displayName = model.has("displayName") ? 
                    model.get("displayName").getAsString() : "N/A";
            String description = model.has("description") ? 
                    model.get("description").getAsString() : "N/A";
            
            // Méthodes supportées
            JsonArray supportedMethods = model.has("supportedGenerationMethods") ?
                    model.getAsJsonArray("supportedGenerationMethods") : new JsonArray();
            

            List<String> listSupportedMethods = new ArrayList<String>();
            for (int j = 0; j < supportedMethods. size(); j++) {               
                listSupportedMethods.add(supportedMethods.get(j).getAsString());
               
            }
            System.out. println();
            
            // Limites de tokens si disponibles
            Integer inputLimit =null;
            if (model.has("inputTokenLimit")) {
               inputLimit = model.get("inputTokenLimit").getAsInt();
               
            }
            Integer outputLimit  = null;
            if (model. has("outputTokenLimit")) {
                outputLimit = model.get("outputTokenLimit").getAsInt();
            
            }
           
            GeminiModel gm = new GeminiModel(name, displayName, description, listSupportedMethods, inputLimit, outputLimit);
            list.add(gm);
           
        }
        return list;
        
    }
    
   
}