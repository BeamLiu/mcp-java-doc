package io.emop.javadocjson.model;

import lombok.Data;

/**
 * Simplified Javadoc method model for text-level parsing.
 * Contains method name, modifiers and type, description and complete raw text.
 */
@Data
public class SimpleJavadocMethod {
    
    /**
     * Method name
     */
    private String name;
    
    /**
     * Modifiers and type (e.g.: "static CheckScope", "public static int")
     */
    private String modifierAndType;
    
    /**
     * Method description
     */
    private String description;
    
    /**
     * Complete raw text containing method signature and detailed description
     */
    private String detailText;
    
    /**
     * Default constructor
     */
    public SimpleJavadocMethod() {
    }
    
    /**
     * Complete constructor
     * 
     * @param name Method name
     * @param modifierAndType Modifiers and type
     * @param description Method description
     * @param detailText Complete raw text
     */
    public SimpleJavadocMethod(String name, String modifierAndType, String description, String detailText) {
        this.name = name;
        this.modifierAndType = modifierAndType;
        this.description = description;
        this.detailText = detailText;
    }
    
    /**
     * Constructor
     * 
     * @param name Method name
     * @param description Method description
     * @param detailText Complete raw text
     */
    public SimpleJavadocMethod(String name, String description, String detailText) {
        this.name = name;
        this.description = description;
        this.detailText = detailText;
    }
    
    /**
     * Simplified constructor with only name and description
     * 
     * @param name Method name
     * @param description Method description
     */
    public SimpleJavadocMethod(String name, String description) {
        this.name = name;
        this.description = description;
        this.detailText = "";
    }
}