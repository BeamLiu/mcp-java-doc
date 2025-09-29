package io.emop.javadocjson.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Base class for all Javadoc method representations.
 * Provides common fields and functionality for both detailed and simplified method parsing.
 */
@Data
public abstract class BaseJavadocMethod {
    
    @JsonProperty("name")
    protected String name;
    
    @JsonProperty("description")
    protected String description;
    
    /**
     * Default constructor
     */
    public BaseJavadocMethod() {
    }
    
    /**
     * Constructor with basic fields
     * 
     * @param name Method name
     * @param description Method description
     */
    public BaseJavadocMethod(String name, String description) {
        this.name = name;
        this.description = description;
    }
}