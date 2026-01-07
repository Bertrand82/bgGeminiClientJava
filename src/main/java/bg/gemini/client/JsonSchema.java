package bg.gemini.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.util.Map;
import java.util.HashMap;

public class JsonSchema {
    
    private String type;
    private String description;
    private Map<String, PropertySchema> properties;
    private String[] required;
    private JsonSchema items; // Pour les tableaux
    
    public JsonSchema(String type) {
        this.type = type;
        this.properties = new HashMap<>();
    }
    
    public JsonSchema addProperty(String name, PropertySchema property) {
        this.properties.put(name, property);
        return this;
    }
    
    public JsonSchema setRequired(String... required) {
        this.required = required;
        return this;
    }
    
    public JsonSchema setDescription(String description) {
        this.description = description;
        return this;
    }
    
    // AJOUTEZ CETTE MÉTHODE ⬇️
    public JsonSchema setItemsSchema(JsonSchema items) {
        this.items = items;
        return this;
    }
    
    public JsonObject toJsonObject() {
        JsonObject schema = new JsonObject();
        schema.addProperty("type", type);
        
        if (description != null) {
            schema.addProperty("description", description);
        }
        
        // Ajouter les propriétés (pour les objets)
        if (properties != null && !properties.isEmpty()) {
            JsonObject propsObj = new JsonObject();
            for (Map.Entry<String, PropertySchema> entry : properties. entrySet()) {
                propsObj.add(entry.getKey(), entry.getValue().toJsonObject());
            }
            schema.add("properties", propsObj);
        }
        
        // Ajouter les champs requis
        if (required != null && required.length > 0) {
            JsonArray requiredArray = new JsonArray();
            for (String field : required) {
                requiredArray.add(field);
            }
            schema.add("required", requiredArray);
        }
        
        // Ajouter items pour les tableaux
        if (items != null) {
            schema. add("items", items.toJsonObject());
        }
        
        return schema;
    }
    
    // Classe interne PropertySchema
    public static class PropertySchema {
        private String type;
        private String description;
        private String[] enumValues;
        private JsonSchema items;
        
        public PropertySchema(String type) {
            this.type = type;
        }
        
        public PropertySchema setDescription(String description) {
            this.description = description;
            return this;
        }
        
        public PropertySchema setEnum(String... values) {
            this.enumValues = values;
            return this;
        }
        
        public PropertySchema setItems(JsonSchema items) {
            this.items = items;
            return this;
        }
        
        public JsonObject toJsonObject() {
            JsonObject prop = new JsonObject();
            prop.addProperty("type", type);
            
            if (description != null) {
                prop.addProperty("description", description);
            }
            
            if (enumValues != null) {
                JsonArray enumArray = new JsonArray();
                for (String value : enumValues) {
                    enumArray.add(value);
                }
                prop.add("enum", enumArray);
            }
            
            if (items != null) {
                prop.add("items", items.toJsonObject());
            }
            
            return prop;
        }
    }
}