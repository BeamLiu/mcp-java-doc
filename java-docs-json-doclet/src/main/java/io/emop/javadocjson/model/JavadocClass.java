package io.emop.javadocjson.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.ArrayList;

/**
 * Represents a Java class, interface, enum, or annotation.
 * Supports both detailed parsing (from JsonDoclet) and simplified parsing (from HTML pages).
 */
@Data
public class JavadocClass {

    @JsonProperty("name")
    private String name;

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

    // Unified fields that can contain both detailed and simplified parsing results
    @JsonProperty("constructors")
    private List<BaseJavadocConstructor> constructors;

    @JsonProperty("methods")
    private List<BaseJavadocMethod> methods;

    @JsonProperty("fields")
    private List<BaseJavadocField> fields;

    public JavadocClass() {
        this.modifiers = new ArrayList<>();
        this.interfaces = new ArrayList<>();
        this.constructors = new ArrayList<>();
        this.methods = new ArrayList<>();
        this.fields = new ArrayList<>();
    }

    public JavadocClass(String name, String type) {
        this();
        this.name = name;
        this.type = type;
    }

    /**
     * Get total method count (both detailed and simplified)
     * @return total method count
     */
    @JsonIgnore
    public int getTotalMethodCount() {
        return methods != null ? methods.size() : 0;
    }

    /**
     * Get total field count (both detailed and simplified)
     * @return total field count
     */
    @JsonIgnore
    public int getTotalFieldCount() {
        return fields != null ? fields.size() : 0;
    }
    
    /**
     * Get total constructor count (both detailed and simplified)
     * @return total constructor count
     */
    @JsonIgnore
    public int getTotalConstructorCount() {
        return constructors != null ? constructors.size() : 0;
    }

    @JsonIgnore
    public String getFullName() {
        return packageName + "." + name;
    }
}