package io.emop.javadocjson.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.ArrayList;

/**
 * Represents a method in a Java class.
 */
@Data
public class JavadocMethod {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("signature")
    private String signature;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("modifiers")
    private List<String> modifiers;
    
    @JsonProperty("returnType")
    private String returnType;
    
    @JsonProperty("parameters")
    private List<JavadocParameter> parameters;
    
    @JsonProperty("exceptions")
    private List<String> exceptions;
    
    public JavadocMethod() {
        this.modifiers = new ArrayList<>();
        this.parameters = new ArrayList<>();
        this.exceptions = new ArrayList<>();
    }
    
    public JavadocMethod(String name, String signature, String description) {
        this();
        this.name = name;
        this.signature = signature;
        this.description = description;
    }
}