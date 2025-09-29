package io.emop.javadocjson.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Base class for all Javadoc field representations.
 * Provides common fields and functionality for both detailed and simplified field parsing.
 */
@Data
public abstract class BaseJavadocField {
    
    @JsonProperty("name")
    protected String name;
    
    @JsonProperty("description")
    protected String description;
    
    /**
     * Default constructor
     */
    public BaseJavadocField() {
    }
    
    /**
     * Constructor with basic fields
     * 
     * @param name Field name
     * @param description Field description
     */
    public BaseJavadocField(String name, String description) {
        this.name = name;
        this.description = description;
    }
}