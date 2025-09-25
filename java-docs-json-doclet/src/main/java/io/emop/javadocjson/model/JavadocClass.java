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

    @JsonProperty("constructors")
    private List<JavadocConstructor> constructors;

    // Detailed parsing fields (from JsonDoclet)
    @JsonProperty("methods")
    private List<JavadocMethod> methods;

    @JsonProperty("fields")
    private List<JavadocField> fields;

    // Simplified parsing fields (from HTML pages)
    @JsonProperty("simpleMethods")
    private List<SimpleJavadocMethod> simpleMethods;

    @JsonProperty("simpleFields")
    private List<SimpleJavadocField> simpleFields;

    public JavadocClass() {
        this.modifiers = new ArrayList<>();
        this.interfaces = new ArrayList<>();
        this.constructors = new ArrayList<>();
        this.methods = new ArrayList<>();
        this.fields = new ArrayList<>();
        this.simpleMethods = new ArrayList<>();
        this.simpleFields = new ArrayList<>();
    }

    public JavadocClass(String name, String type) {
        this();
        this.name = name;
        this.type = type;
    }

    /**
     * Returns true if this class has detailed parsing information (from JsonDoclet).
     */
    @JsonIgnore
    public boolean hasDetailedInfo() {
        return (methods != null && !methods.isEmpty()) || (fields != null && !fields.isEmpty());
    }

    /**
     * Returns true if this class has simplified parsing information (from HTML pages).
     */
    @JsonIgnore
    public boolean hasSimplifiedInfo() {
        return (simpleMethods != null && !simpleMethods.isEmpty()) || (simpleFields != null && !simpleFields.isEmpty());
    }

    /**
     * Returns the total number of methods (both detailed and simplified).
     */
    @JsonIgnore
    public int getTotalMethodCount() {
        int count = 0;
        if (methods != null) count += methods.size();
        if (simpleMethods != null) count += simpleMethods.size();
        return count;
    }

    /**
     * Returns the total number of fields (both detailed and simplified).
     */
    @JsonIgnore
    public int getTotalFieldCount() {
        int count = 0;
        if (fields != null) count += fields.size();
        if (simpleFields != null) count += simpleFields.size();
        return count;
    }

    @JsonIgnore
    public String getFullName() {
        return packageName + "." + name;
    }
}