package io.emop.javadocjson.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.ArrayList;

/**
 * Represents a constructor in a Java class with detailed parsing information.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class JavadocConstructor extends BaseJavadocConstructor {
    
    @JsonProperty("signature")
    private String signature;
    
    @JsonProperty("modifiers")
    private List<String> modifiers;
    
    @JsonProperty("parameters")
    private List<JavadocParameter> parameters;
    
    @JsonProperty("exceptions")
    private List<String> exceptions;
    
    public JavadocConstructor() {
        super();
        this.modifiers = new ArrayList<>();
        this.parameters = new ArrayList<>();
        this.exceptions = new ArrayList<>();
    }
    
    public JavadocConstructor(String name, String signature, String description) {
        super(name, description);
        this.signature = signature;
        this.modifiers = new ArrayList<>();
        this.parameters = new ArrayList<>();
        this.exceptions = new ArrayList<>();
    }
}