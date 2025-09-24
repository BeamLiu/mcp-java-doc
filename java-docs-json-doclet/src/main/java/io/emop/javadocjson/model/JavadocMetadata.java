package io.emop.javadocjson.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;

/**
 * Metadata information for the generated Javadoc JSON.
 */
@Data
public class JavadocMetadata {
    
    @JsonProperty("generatedAt")
    private String generatedAt;
    
    @JsonProperty("source")
    private String source;
    
    @JsonProperty("baseUrl")
    private String baseUrl;
    
    @JsonProperty("version")
    private String version;
    
    public JavadocMetadata() {
        this.generatedAt = Instant.now().toString();
        this.version = "1.0.0";
    }
    
    public JavadocMetadata(String source, String baseUrl) {
        this();
        this.source = source;
        this.baseUrl = baseUrl;
    }
}