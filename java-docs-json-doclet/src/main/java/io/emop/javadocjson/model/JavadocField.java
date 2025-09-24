package io.emop.javadocjson.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.ArrayList;

/**
 * Represents a field in a Java class.
 */
@Data
public class JavadocField {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("modifiers")
    private List<String> modifiers;
    
    @JsonProperty("defaultValue")
    private String defaultValue;
    
    public JavadocField() {
        this.modifiers = new ArrayList<>();
    }
    
    public JavadocField(String name, String type, String description) {
        this();
        this.name = name;
        this.type = type;
        this.description = description;
    }
}