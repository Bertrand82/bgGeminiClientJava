package bg.gemini.client;

import com.google.gson. Gson;
import com.google. gson.JsonSyntaxException;

public class GeminiResponse {
    private final String text;
    private final String rawResponse;
    
    public GeminiResponse(String text, String rawResponse) {
        this.text = text;
        this.rawResponse = rawResponse;
    }
    
    public String getText() {
        return text;
    }
    
    public String getRawResponse() {
        return rawResponse;
    }
    
    /**
     * Parse le texte comme JSON avec gestion d'erreur
     */
    public <T> T parseJson(Class<T> clazz) {
        Gson gson = new Gson();
        
        try {
            return gson. fromJson(text, clazz);
        } catch (JsonSyntaxException e) {
            System.err.println("❌ JSON incomplet ou invalide");
            System.err.println("Réponse reçue :");
            System.err.println(text);
            System.err.println("\nErreur:  " + e.getMessage());
            
            // Tentative de récupération :  compléter le JSON
            String fixedJson = tryFixIncompleteJson(text);
            System.err.println("✅ Tentative de correction du JSON.. fixedJson: "+fixedJson);
            if (fixedJson == null) {
            } else {
               
                try {
                    return gson.fromJson(fixedJson, clazz);
                } catch (Exception e2) {
                    System. err.println("❌ Correction échouée");
                }
            }
            
            throw e;
        }
    }
    
    /**
     * Tente de compléter un JSON de tableau incomplet
     */
    private String tryFixIncompleteJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        
        json = json.trim();
        
        // Si c'est un tableau qui n'est pas fermé
        if (json.startsWith("[") && !json.endsWith("]")) {
            // Trouver le dernier objet complet
        	 int x1 = countCharStream(json, '"');
        	 if (x1 % 2 == 1) {
        		    json =json+"\"\n";
        		}
             
            int n1 = countCharStream(json, '{');
            int n2= countCharStream(json, '}');
            if (n1> n2) {
            	json = json+"}\n ";
            }
            int lastCloseBrace = json.lastIndexOf("}");
             if (lastCloseBrace > 0) {
                // Couper après le dernier objet complet et fermer le tableau
                String fixed = json.substring(0, lastCloseBrace + 1) + "]";
                return fixed;
            }
        }
        
        return null;
    }
    
    // Méthode 2 : Stream
    public static int countCharStream(String str, char c) {
        return (int) str.chars().filter(ch -> ch == c).count();
    }
}