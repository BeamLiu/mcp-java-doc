package io.emop.javadocjson.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.ArrayList;

/**
 * Represents a Java package containing classes.
 */
@Data
public class JavadocPackage {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("classes")
    private List<JavadocClass> classes;
    
    public JavadocPackage() {
        this.classes = new ArrayList<>();
    }
    
    public JavadocPackage(String name, String description) {
        this();
        this.name = name;
        this.description = description;
    }
}