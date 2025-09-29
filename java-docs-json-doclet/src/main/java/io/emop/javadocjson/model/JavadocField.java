package io.emop.javadocjson.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.ArrayList;

/**
 * Represents a field in a Java class with detailed parsing information.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class JavadocField extends BaseJavadocField {
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("modifiers")
    private List<String> modifiers;
    
    @JsonProperty("defaultValue")
    private String defaultValue;
    
    public JavadocField() {
        super();
        this.modifiers = new ArrayList<>();
    }
    
    public JavadocField(String name, String type, String description) {
        super(name, description);
        this.type = type;
        this.modifiers = new ArrayList<>();
    }
}