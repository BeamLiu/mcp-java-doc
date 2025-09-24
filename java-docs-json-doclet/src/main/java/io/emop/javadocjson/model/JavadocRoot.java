package io.emop.javadocjson.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.ArrayList;

/**
 * Root object representing the complete Javadoc JSON structure.
 */
@Data
public class JavadocRoot {
    
    @JsonProperty("metadata")
    private JavadocMetadata metadata;
    
    @JsonProperty("packages")
    private List<JavadocPackage> packages;
    
    public JavadocRoot() {
        this.packages = new ArrayList<>();
    }
    
    public JavadocRoot(JavadocMetadata metadata) {
        this();
        this.metadata = metadata;
    }
}