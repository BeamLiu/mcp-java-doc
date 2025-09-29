package io.emop.javadocjson.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Simplified Javadoc constructor model for text-level parsing.
 * Contains constructor name, modifiers, description and complete raw text.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SimpleJavadocConstructor extends BaseJavadocConstructor {
    
    /**
     * Complete raw text containing constructor signature and detailed description
     */
    private String detailText;
    
    /**
     * Default constructor
     */
    public SimpleJavadocConstructor() {
        super();
    }
    

    /**
     * Constructor with all parameters
     * 
     * @param name Constructor name
     * @param description Constructor description
     * @param detailText Complete raw text
     */
    public SimpleJavadocConstructor(String name, String description, String detailText) {
        super(name, description);
        this.detailText = detailText;
    }
    
    /**
     * Simplified constructor
     * 
     * @param name Constructor name
     * @param description Constructor description
     */
    public SimpleJavadocConstructor(String name, String description) {
        super(name, description);
        this.detailText = "";
    }
}