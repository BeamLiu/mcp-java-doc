package io.emop.javadocjson.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Simplified Javadoc method model for text-level parsing.
 * Contains method name, modifiers and type, description and complete raw text.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SimpleJavadocMethod extends BaseJavadocMethod {
    
    /**
     * Modifiers and type (e.g.: "static CheckScope", "public static int")
     */
    private String modifierAndType;
    
    /**
     * Complete raw text containing method signature and detailed description
     */
    private String detailText;
    
    /**
     * Default constructor
     */
    public SimpleJavadocMethod() {
        super();
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
        super(name, description);
        this.modifierAndType = modifierAndType;
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
        super(name, description);
        this.detailText = detailText;
    }
    
    /**
     * Simplified constructor with only name and description
     * 
     * @param name Method name
     * @param description Method description
     */
    public SimpleJavadocMethod(String name, String description) {
        super(name, description);
        this.detailText = "";
    }
}