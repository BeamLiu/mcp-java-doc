package io.emop.javadocjson.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Simplified Javadoc field model for text-level parsing.
 * Contains field name, modifiers and type, description and complete raw text.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SimpleJavadocField extends BaseJavadocField {
    
    /**
     * Modifiers and type (e.g.: "static int", "public final String")
     */
    private String modifierAndType;
    
    /**
     * Default constructor
     */
    public SimpleJavadocField() {
        super();
    }
    
    /**
     * Complete constructor
     * 
     * @param name Field name
     * @param modifierAndType Modifiers and type
     * @param description Field description
     */
    public SimpleJavadocField(String name, String modifierAndType, String description) {
        super(name, description);
        this.modifierAndType = modifierAndType;
    }
    
    /**
     * Simplified constructor with only name and description
     * 
     * @param name Field name
     * @param description Field description
     */
    public SimpleJavadocField(String name, String description) {
        super(name, description);
    }
}