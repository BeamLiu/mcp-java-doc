package io.emop.javadocjson.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.ArrayList;

/**
 * Represents a method in a Java class with detailed parsing information.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class JavadocMethod extends BaseJavadocMethod {
    
    @JsonProperty("signature")
    private String signature;
    
    @JsonProperty("modifiers")
    private List<String> modifiers;
    
    @JsonProperty("returnType")
    private String returnType;
    
    @JsonProperty("parameters")
    private List<JavadocParameter> parameters;
    
    @JsonProperty("exceptions")
    private List<String> exceptions;
    
    public JavadocMethod() {
        super();
        this.modifiers = new ArrayList<>();
        this.parameters = new ArrayList<>();
        this.exceptions = new ArrayList<>();
    }
    
    public JavadocMethod(String name, String signature, String description) {
        super(name, description);
        this.signature = signature;
        this.modifiers = new ArrayList<>();
        this.parameters = new ArrayList<>();
        this.exceptions = new ArrayList<>();
    }
}