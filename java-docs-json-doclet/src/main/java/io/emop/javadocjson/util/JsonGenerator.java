package io.emop.javadocjson.util;

import io.emop.javadocjson.model.JavadocRoot;
import io.emop.javadocjson.model.JavadocPackage;
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
     * Write JavadocRoot to JSON file.
     * 
     * @param root the JavadocRoot object to serialize
     * @param outputFile the target file
     * @throws IOException if writing fails
     */
    public void writeToFile(JavadocRoot root, File outputFile) throws IOException {
        // Ensure parent directory exists
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        if (mcpCompatible) {
            writeEnhancedFormat(root, outputFile);
        } else {
            objectMapper.writeValue(outputFile, root);
        }
    }
    
    /**
     * Convert JavadocRoot to JSON string.
     * 
     * @param root the JavadocRoot object to serialize
     * @return JSON string representation
     * @throws IOException if serialization fails
     */
    public String toJsonString(JavadocRoot root) throws IOException {
        if (mcpCompatible) {
            return createEnhancedFormat(root);
        } else {
            return objectMapper.writeValueAsString(root);
        }
    }
    
    /**
     * Write enhanced format with MCP compatibility.
     */
    private void writeEnhancedFormat(JavadocRoot root, File outputFile) throws IOException {
        String jsonContent = createEnhancedFormat(root);
        Files.write(outputFile.toPath(), jsonContent.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Create enhanced JSON format with search index and MCP compatibility.
     */
    private String createEnhancedFormat(JavadocRoot root) throws IOException {
        ObjectNode rootNode = objectMapper.createObjectNode();
        
        // Add metadata
        ObjectNode metadataNode = objectMapper.valueToTree(root.getMetadata());
        rootNode.set("metadata", metadataNode);
        
        // Add packages
        ArrayNode packagesArray = objectMapper.createArrayNode();
        for (JavadocPackage pkg : root.getPackages()) {
            ObjectNode packageNode = createPackageNode(pkg);
            packagesArray.add(packageNode);
        }
        rootNode.set("packages", packagesArray);
        
        // Add MCP-specific metadata
        ObjectNode mcpMetadata = objectMapper.createObjectNode();
        mcpMetadata.put("version", "1.0");
        mcpMetadata.put("format", "javadoc-json");
        mcpMetadata.put("compatible", "mcp-javadoc-search");
        rootNode.set("mcpMetadata", mcpMetadata);
        
        return objectMapper.writeValueAsString(rootNode);
    }
    
    /**
     * Create package node with enhanced information.
     */
    private ObjectNode createPackageNode(JavadocPackage pkg) {
        ObjectNode packageNode = objectMapper.valueToTree(pkg);
        
        // Add class count
        packageNode.put("classCount", pkg.getClasses().size());
        
        // Add enhanced class information
        ArrayNode classesArray = objectMapper.createArrayNode();
        for (JavadocClass clazz : pkg.getClasses()) {
            ObjectNode classNode = createClassNode(clazz);
            classesArray.add(classNode);
        }
        packageNode.set("classes", classesArray);
        
        return packageNode;
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
    
    /**
     * Create search index for fast lookups.
     */
    private ObjectNode createSearchIndex(JavadocRoot root) {
        ObjectNode searchIndex = objectMapper.createObjectNode();
        
        // Class index
        ObjectNode classIndex = objectMapper.createObjectNode();
        ArrayNode allClasses = objectMapper.createArrayNode();
        
        // Method index
        ObjectNode methodIndex = objectMapper.createObjectNode();
        ArrayNode allMethods = objectMapper.createArrayNode();
        
        // Field index
        ArrayNode allFields = objectMapper.createArrayNode();
        
        for (JavadocPackage pkg : root.getPackages()) {
            for (JavadocClass clazz : pkg.getClasses()) {
                // Add to class index
                ObjectNode classRef = objectMapper.createObjectNode();
                classRef.put("name", clazz.getName());
                classRef.put("fullName", clazz.getFullName());
                classRef.put("package", pkg.getName());
                classRef.put("type", clazz.getType());
                allClasses.add(classRef);
                
                // Add methods to index
                for (JavadocMethod method : clazz.getMethods()) {
                    ObjectNode methodRef = objectMapper.createObjectNode();
                    methodRef.put("name", method.getName());
                    methodRef.put("className", clazz.getFullName());
                    methodRef.put("returnType", method.getReturnType());
                    methodRef.put("signature", method.getSignature());
                    allMethods.add(methodRef);
                }
                
                // Add fields to index
                for (JavadocField field : clazz.getFields()) {
                    ObjectNode fieldRef = objectMapper.createObjectNode();
                    fieldRef.put("name", field.getName());
                    fieldRef.put("className", clazz.getFullName());
                    fieldRef.put("type", field.getType());
                    allFields.add(fieldRef);
                }
            }
        }
        
        searchIndex.set("classes", allClasses);
        searchIndex.set("methods", allMethods);
        searchIndex.set("fields", allFields);
        
        // Add statistics
        ObjectNode stats = objectMapper.createObjectNode();
        stats.put("totalPackages", root.getPackages().size());
        stats.put("totalClasses", allClasses.size());
        stats.put("totalMethods", allMethods.size());
        stats.put("totalFields", allFields.size());
        searchIndex.set("statistics", stats);
        
        return searchIndex;
    }
}