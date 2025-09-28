package io.emop.javadocjson.util;

import io.emop.javadocjson.model.JavadocClass;
import io.emop.javadocjson.model.JavadocMethod;
import io.emop.javadocjson.model.JavadocField;
import io.emop.javadocjson.model.JavadocConstructor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

/**
 * Utility class for generating JSON output from Javadoc models.
 * Provides enhanced formatting and compatibility with mcp-javadoc-search.
 */
public class JsonGenerator {
    
    private final ObjectMapper objectMapper;
    private boolean mcpCompatible = true;

    public JsonGenerator() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }
    
    /**
     * Set MCP compatibility mode.
     * 
     * @param mcpCompatible whether to generate MCP-compatible output
     */
    public void setMcpCompatible(boolean mcpCompatible) {
        this.mcpCompatible = mcpCompatible;
    }
    

    
    /**
     * Write individual JavadocClass to JSON file.
     * 
     * @param javadocClass the JavadocClass object to serialize
     * @param outputFile the target file
     * @throws IOException if writing fails
     */
    public void writeClassToFile(JavadocClass javadocClass, File outputFile) throws IOException {
        // Ensure parent directory exists
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        if (mcpCompatible) {
            writeEnhancedClassFormat(javadocClass, outputFile);
        } else {
            objectMapper.writeValue(outputFile, javadocClass);
        }
    }
    

    

    
    /**
     * Write enhanced format for individual class with MCP compatibility.
     */
    private void writeEnhancedClassFormat(JavadocClass javadocClass, File outputFile) throws IOException {
        String jsonContent = createEnhancedClassFormat(javadocClass);
        Files.write(outputFile.toPath(), jsonContent.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Create enhanced JSON format for individual class with MCP compatibility.
     */
    private String createEnhancedClassFormat(JavadocClass javadocClass) throws IOException {
        ObjectNode classNode = createClassNode(javadocClass);
        
        // Add MCP-specific metadata for individual class
        ObjectNode mcpMetadata = objectMapper.createObjectNode();
        mcpMetadata.put("version", "1.0");
        mcpMetadata.put("format", "javadoc-class-json");
        mcpMetadata.put("compatible", "mcp-javadoc-search");
        mcpMetadata.put("className", javadocClass.getFullName());
        classNode.set("mcpMetadata", mcpMetadata);
        
        return objectMapper.writeValueAsString(classNode);
    }
    

    

    
    /**
     * Create class node with enhanced information.
     */
    private ObjectNode createClassNode(JavadocClass clazz) {
        ObjectNode classNode = objectMapper.valueToTree(clazz);
        
        // Add member counts
        classNode.put("methodCount", clazz.getMethods().size());
        classNode.put("fieldCount", clazz.getFields().size());
        classNode.put("constructorCount", clazz.getConstructors().size());
        
        // Add search keywords
        ArrayNode keywords = objectMapper.createArrayNode();
        keywords.add(clazz.getName());
        keywords.add(clazz.getFullName());
        if (clazz.getType() != null) {
            keywords.add(clazz.getType());
        }
        classNode.set("searchKeywords", keywords);
        
        return classNode;
    }
    

}