package io.emop.javadocjson.model;

import lombok.Data;

/**
 * Simplified Javadoc constructor model for text-level parsing.
 * Contains constructor name, modifiers, description and complete raw text.
 */
@Data
public class SimpleJavadocConstructor {
    
    /**
     * Constructor name (usually same as class name)
     */
    private String name;
    
    /**
     * Constructor description
     */
    private String description;
    
    /**
     * Complete raw text containing constructor signature and detailed description
     */
    private String detailText;
    
    /**
     * Default constructor
     */
    public SimpleJavadocConstructor() {
    }
    

    /**
     * Constructor with all parameters
     * 
     * @param name Constructor name
     * @param description Constructor description
     * @param detailText Complete raw text
     */
    public SimpleJavadocConstructor(String name, String description, String detailText) {
        this.name = name;
        this.description = description;
        this.detailText = detailText;
    }
    
    /**
     * Simplified constructor
     * 
     * @param name Constructor name
     * @param description Constructor description
     */
    public SimpleJavadocConstructor(String name, String description) {
        this.name = name;
        this.description = description;
        this.detailText = "";
    }
}