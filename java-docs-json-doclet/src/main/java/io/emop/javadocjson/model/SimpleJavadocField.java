package io.emop.javadocjson.model;

import lombok.Data;

/**
 * Simplified Javadoc field model for text-level parsing.
 * Contains field name, modifiers and type, description and complete raw text.
 */
@Data
public class SimpleJavadocField {
    
    /**
     * Field name
     */
    private String name;
    
    /**
     * Modifiers and type (e.g.: "static int", "public final String")
     */
    private String modifierAndType;
    
    /**
     * Field description
     */
    private String description;
    
    /**
     * Default constructor
     */
    public SimpleJavadocField() {
    }
    
    /**
     * Complete constructor
     * 
     * @param name Field name
     * @param modifierAndType Modifiers and type
     * @param description Field description
     */
    public SimpleJavadocField(String name, String modifierAndType, String description) {
        this.name = name;
        this.modifierAndType = modifierAndType;
        this.description = description;
    }
    
    /**
     * Simplified constructor with only name and description
     * 
     * @param name Field name
     * @param description Field description
     */
    public SimpleJavadocField(String name, String description) {
        this.name = name;
        this.description = description;
    }
}