package io.emop.javadocjson.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Base class for all Javadoc constructor representations.
 * Provides common fields and functionality for both detailed and simplified constructor parsing.
 */
@Data
public abstract class BaseJavadocConstructor {
    
    @JsonProperty("name")
    protected String name;
    
    @JsonProperty("description")
    protected String description;
    
    /**
     * Default constructor
     */
    public BaseJavadocConstructor() {
    }
    
    /**
     * Constructor with basic fields
     * 
     * @param name Constructor name
     * @param description Constructor description
     */
    public BaseJavadocConstructor(String name, String description) {
        this.name = name;
        this.description = description;
    }
}