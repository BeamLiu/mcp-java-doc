package io.emop.javadocjson.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.ArrayList;

/**
 * Represents a Java class, interface, enum, or annotation.
 */
@Data
public class JavadocClass {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("fullName")
    private String fullName;
    
    @JsonProperty("packageName")
    private String packageName;

    @JsonProperty("type")
    private String type; // class, interface, enum, annotation
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("modifiers")
    private List<String> modifiers;
    
    @JsonProperty("superClass")
    private String superClass;
    
    @JsonProperty("interfaces")
    private List<String> interfaces;
    
    @JsonProperty("constructors")
    private List<JavadocConstructor> constructors;
    
    @JsonProperty("methods")
    private List<JavadocMethod> methods;
    
    @JsonProperty("fields")
    private List<JavadocField> fields;
    
    public JavadocClass() {
        this.modifiers = new ArrayList<>();
        this.interfaces = new ArrayList<>();
        this.constructors = new ArrayList<>();
        this.methods = new ArrayList<>();
        this.fields = new ArrayList<>();
    }
    
    public JavadocClass(String name, String fullName, String type) {
        this();
        this.name = name;
        this.fullName = fullName;
        this.type = type;
    }
}