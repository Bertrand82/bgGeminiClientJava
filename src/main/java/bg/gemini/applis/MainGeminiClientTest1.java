package bg.gemini.applis;

import com. google.gson.annotations.SerializedName;

import bg.gemini.client.GeminiClient;
import bg.gemini.client.GeminiResponse;
import bg.gemini.client.JsonSchema;

public class MainGeminiClientTest1 {
    
    // Classe pour mapper la réponse JSON
    static class Person {
        @SerializedName("name")
        private String name;
        
        @SerializedName("age")
        private int age;
        
        @SerializedName("city")
        private String city;
        
        @SerializedName("profession")
        private String profession;
        
        // Getters
        public String getName() { return name; }
        public int getAge() { return age; }
        public String getCity() { return city; }
        public String getProfession() { return profession; }
        
        @Override
        public String toString() {
            return String.format("Person{name='%s', age=%d, city='%s', profession='%s'}", 
                    name, age, city, profession);
        }
    }
    
    public static void main(String[] args) {
        try {
            // Initialiser le client avec votre clé API
          
            GeminiClient client = new GeminiClient();
            
            // Définir le schéma JSON attendu
            JsonSchema schema = new JsonSchema("object")
                    .addProperty("name", 
                            new JsonSchema. PropertySchema("string")
                                    .setDescription("Le nom de la personne"))
                    .addProperty("age", 
                            new JsonSchema.PropertySchema("integer")
                                    .setDescription("L'âge de la personne"))
                    .addProperty("city", 
                            new JsonSchema. PropertySchema("string")
                                    .setDescription("La ville de résidence"))
                    .addProperty("profession", 
                            new JsonSchema.PropertySchema("string")
                                    .setDescription("La profession"))
                    .setRequired("name", "age", "city", "profession");
            
            // Créer le prompt
            String prompt = "Génère les informations fictives d'une personne française " +
                           "qui travaille dans la tech à Paris.";
            
            // Envoyer la requête
            GeminiResponse response = client.sendRequest(prompt, schema);
            
            // Afficher la réponse brute
            System.out.println("Réponse JSON:  " + response.getText());
            
            // Parser la réponse en objet Java
            Person person = response.parseJson(Person.class);
            System.out.println("Objet parsé: " + person);
            
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }
}