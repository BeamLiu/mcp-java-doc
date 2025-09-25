package io.emop.javadocjson.doclet;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.emop.javadocjson.model.*;
import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import java.io.File;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.ParamTree;
import com.sun.source.util.DocTrees;

/**
 * Custom Doclet implementation that generates JSON output from Javadoc.
 * This replaces the manual parsing approach with the official JDK Doclet API.
 */
public class JsonDoclet implements Doclet {
    
    private Reporter reporter;
    private String outputFile = "javadoc.json";
    private String baseUrl = "";
    private boolean includePrivate = false;
    
    @Override
    public void init(Locale locale, Reporter reporter) {
        this.reporter = reporter;
    }
    
    @Override
    public String getName() {
        return "JsonDoclet";
    }
    
    @Override
    public Set<? extends Option> getSupportedOptions() {
        return Set.of(
            new SimpleOption("-outputFile", 1, "Output JSON file path") {
                @Override
                public boolean process(String option, List<String> arguments) {
                    outputFile = arguments.get(0);
                    return true;
                }
            },
            new SimpleOption("-baseUrl", 1, "Base URL for documentation") {
                @Override
                public boolean process(String option, List<String> arguments) {
                    baseUrl = arguments.get(0);
                    return true;
                }
            },
            new SimpleOption("-includePrivate", 0, "Include private members") {
                @Override
                public boolean process(String option, List<String> arguments) {
                    includePrivate = true;
                    return true;
                }
            }
        );
    }
    
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }
    
    @Override
    public boolean run(DocletEnvironment environment) {
        try {
            reporter.print(Diagnostic.Kind.NOTE, "Starting JSON generation...");
            
            // Create metadata
            JavadocMetadata metadata = new JavadocMetadata();
            metadata.setGeneratedAt(Instant.now().toString());
            metadata.setBaseUrl(baseUrl);
            metadata.setSource("doclet");
            metadata.setVersion("1.0.0");
            
            // Create root object
            JavadocRoot root = new JavadocRoot(metadata);
            
            // Process all included elements
            Set<? extends Element> includedElements = environment.getIncludedElements();
            Map<String, JavadocPackage> packageMap = new HashMap<>();
            
            // Group elements by package
            for (Element element : includedElements) {
                if (element.getKind() == ElementKind.CLASS || 
                    element.getKind() == ElementKind.INTERFACE ||
                    element.getKind() == ElementKind.ENUM ||
                    element.getKind() == ElementKind.ANNOTATION_TYPE) {
                    
                    TypeElement typeElement = (TypeElement) element;
                    processTypeElement(typeElement, packageMap, environment);
                }
            }
            
            // Set packages in root
            root.setPackages(new ArrayList<>(packageMap.values()));
            
            // Write JSON output
            ObjectMapper mapper = new ObjectMapper();
            mapper.findAndRegisterModules(); // For LocalDateTime serialization
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputFile), root);
            
            reporter.print(Diagnostic.Kind.NOTE, 
                String.format("JSON documentation generated: %s (%d packages, %d classes)", 
                    outputFile, packageMap.size(), 
                    packageMap.values().stream().mapToInt(p -> p.getClasses().size()).sum()));
            
            return true;
            
        } catch (Exception e) {
            reporter.print(Diagnostic.Kind.ERROR, "Error generating JSON: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private void processTypeElement(TypeElement typeElement, Map<String, JavadocPackage> packageMap, 
                                   DocletEnvironment environment) {
        
        // Get package name
        String packageName = environment.getElementUtils().getPackageOf(typeElement).getQualifiedName().toString();
        
        // Get or create package
        JavadocPackage javadocPackage = packageMap.computeIfAbsent(packageName, name -> {
            PackageElement packageElement = environment.getElementUtils().getPackageOf(typeElement);
            String packageDescription = environment.getElementUtils().getDocComment(packageElement);
            return new JavadocPackage(name, packageDescription != null ? packageDescription : "");
        });
        
        // Create JavadocClass
        JavadocClass javadocClass = createJavadocClass(typeElement, environment);
        if (javadocClass != null) {
            javadocPackage.getClasses().add(javadocClass);
        }
    }
    
    private JavadocClass createJavadocClass(TypeElement typeElement, DocletEnvironment environment) {
        // Check visibility
        if (!includePrivate && !isPublicOrProtected(typeElement)) {
            return null;
        }
        
        String className = typeElement.getSimpleName().toString();
        String fullName = typeElement.getQualifiedName().toString();
        String classType = getElementType(typeElement);
        
        JavadocClass javadocClass = new JavadocClass(className, classType);
        
        // Set description
        String docComment = environment.getElementUtils().getDocComment(typeElement);
        javadocClass.setDescription(docComment != null ? docComment.trim() : "");
        
        // Set modifiers
        javadocClass.setModifiers(getModifiers(typeElement));
        
        // Set superclass
        TypeMirror superclass = typeElement.getSuperclass();
        if (superclass != null && !superclass.toString().equals("java.lang.Object")) {
            javadocClass.setSuperClass(superclass.toString());
        }
        
        // Set interfaces
        List<String> interfaces = typeElement.getInterfaces().stream()
            .map(TypeMirror::toString)
            .collect(Collectors.toList());
        javadocClass.setInterfaces(interfaces);
        
        // Process methods
        List<ExecutableElement> methods = ElementFilter.methodsIn(typeElement.getEnclosedElements());
        for (ExecutableElement method : methods) {
            JavadocMethod javadocMethod = createJavadocMethod(method, environment);
            if (javadocMethod != null) {
                javadocClass.getMethods().add(javadocMethod);
            }
        }
        
        // Process fields
        List<VariableElement> fields = ElementFilter.fieldsIn(typeElement.getEnclosedElements());
        for (VariableElement field : fields) {
            JavadocField javadocField = createJavadocField(field, environment);
            if (javadocField != null) {
                javadocClass.getFields().add(javadocField);
            }
        }
        
        // Process constructors
        List<ExecutableElement> constructors = ElementFilter.constructorsIn(typeElement.getEnclosedElements());
        for (ExecutableElement constructor : constructors) {
            JavadocConstructor javadocConstructor = createJavadocConstructor(constructor, environment);
            if (javadocConstructor != null) {
                javadocClass.getConstructors().add(javadocConstructor);
            }
        }
        
        return javadocClass;
    }
    
    private JavadocMethod createJavadocMethod(ExecutableElement method, DocletEnvironment environment) {
        // Check visibility
        if (!includePrivate && !isPublicOrProtected(method)) {
            return null;
        }
        
        JavadocMethod javadocMethod = new JavadocMethod();
        javadocMethod.setName(method.getSimpleName().toString());
        javadocMethod.setReturnType(method.getReturnType().toString());
        javadocMethod.setSignature(generateMethodSignature(method));
        javadocMethod.setModifiers(getModifiers(method));
        
        // Set description (main body only, excluding block tags to avoid duplication)
        javadocMethod.setDescription(extractMainDescription(method, environment));
        
        // Extract parameter descriptions from @param tags
        Map<String, String> paramDescriptions = extractParameterDescriptions(method, environment);
        
        // Set parameters
        List<JavadocParameter> parameters = new ArrayList<>();
        for (VariableElement param : method.getParameters()) {
            JavadocParameter javadocParam = new JavadocParameter();
            String paramName = param.getSimpleName().toString();
            javadocParam.setName(paramName);
            javadocParam.setType(param.asType().toString());
            javadocParam.setDescription(paramDescriptions.getOrDefault(paramName, ""));
            parameters.add(javadocParam);
        }
        javadocMethod.setParameters(parameters);
        
        // Set exceptions
        List<String> exceptions = method.getThrownTypes().stream()
            .map(TypeMirror::toString)
            .collect(Collectors.toList());
        javadocMethod.setExceptions(exceptions);
        
        return javadocMethod;
    }
    
    private JavadocField createJavadocField(VariableElement field, DocletEnvironment environment) {
        // Check visibility
        if (!includePrivate && !isPublicOrProtected(field)) {
            return null;
        }
        
        JavadocField javadocField = new JavadocField();
        javadocField.setName(field.getSimpleName().toString());
        javadocField.setType(field.asType().toString());
        javadocField.setModifiers(getModifiers(field));
        
        // Set description
        String docComment = environment.getElementUtils().getDocComment(field);
        javadocField.setDescription(docComment != null ? docComment.trim() : "");
        
        // Set default value if available
        Object constantValue = field.getConstantValue();
        if (constantValue != null) {
            javadocField.setDefaultValue(constantValue.toString());
        }
        
        return javadocField;
    }
    
    private JavadocConstructor createJavadocConstructor(ExecutableElement constructor, DocletEnvironment environment) {
        // Check visibility
        if (!includePrivate && !isPublicOrProtected(constructor)) {
            return null;
        }
        
        JavadocConstructor javadocConstructor = new JavadocConstructor();
        javadocConstructor.setName(constructor.getEnclosingElement().getSimpleName().toString());
        javadocConstructor.setSignature(generateMethodSignature(constructor));
        javadocConstructor.setModifiers(getModifiers(constructor));
        
        // Set description (main body only, excluding block tags to avoid duplication)
        javadocConstructor.setDescription(extractMainDescription(constructor, environment));
        
        // Extract parameter descriptions from @param tags
        Map<String, String> paramDescriptions = extractParameterDescriptions(constructor, environment);
        
        // Set parameters
        List<JavadocParameter> parameters = new ArrayList<>();
        for (VariableElement param : constructor.getParameters()) {
            JavadocParameter javadocParam = new JavadocParameter();
            String paramName = param.getSimpleName().toString();
            javadocParam.setName(paramName);
            javadocParam.setType(param.asType().toString());
            javadocParam.setDescription(paramDescriptions.getOrDefault(paramName, ""));
            parameters.add(javadocParam);
        }
        javadocConstructor.setParameters(parameters);
        
        // Set exceptions
        List<String> exceptions = constructor.getThrownTypes().stream()
            .map(TypeMirror::toString)
            .collect(Collectors.toList());
        javadocConstructor.setExceptions(exceptions);
        
        return javadocConstructor;
    }
    
    private boolean isPublicOrProtected(Element element) {
        Set<Modifier> modifiers = element.getModifiers();
        return modifiers.contains(Modifier.PUBLIC) || modifiers.contains(Modifier.PROTECTED);
    }
    
    private List<String> getModifiers(Element element) {
        return element.getModifiers().stream()
            .map(Modifier::toString)
            .collect(Collectors.toList());
    }
    
    private String getElementType(TypeElement element) {
        switch (element.getKind()) {
            case CLASS: return "class";
            case INTERFACE: return "interface";
            case ENUM: return "enum";
            case ANNOTATION_TYPE: return "annotation";
            default: return "class";
        }
    }
    
    private String generateMethodSignature(ExecutableElement method) {
        StringBuilder signature = new StringBuilder();
        
        // Add modifiers
        Set<Modifier> modifiers = method.getModifiers();
        for (Modifier modifier : modifiers) {
            signature.append(modifier.toString()).append(" ");
        }
        
        // Add return type (for methods, not constructors)
        if (method.getKind() == ElementKind.METHOD) {
            signature.append(method.getReturnType().toString()).append(" ");
        }
        
        // Add method name
        signature.append(method.getSimpleName().toString());
        
        // Add parameters
        signature.append("(");
        List<? extends VariableElement> parameters = method.getParameters();
        for (int i = 0; i < parameters.size(); i++) {
            if (i > 0) signature.append(", ");
            VariableElement param = parameters.get(i);
            signature.append(param.asType().toString()).append(" ").append(param.getSimpleName());
        }
        signature.append(")");
        
        // Add exceptions
        List<? extends TypeMirror> thrownTypes = method.getThrownTypes();
        if (!thrownTypes.isEmpty()) {
            signature.append(" throws ");
            for (int i = 0; i < thrownTypes.size(); i++) {
                if (i > 0) signature.append(", ");
                signature.append(thrownTypes.get(i).toString());
            }
        }
        
        return signature.toString();
    }
    
    /**
     * Extracts parameter descriptions from @param tags in the Javadoc comment.
     * 
     * @param element The method or constructor element
     * @param environment The doclet environment
     * @return A map of parameter names to their descriptions
     */
    private Map<String, String> extractParameterDescriptions(ExecutableElement element, DocletEnvironment environment) {
        Map<String, String> paramDescriptions = new HashMap<>();
        
        DocTrees docTrees = environment.getDocTrees();
        DocCommentTree docCommentTree = docTrees.getDocCommentTree(element);
        
        if (docCommentTree != null) {
            for (DocTree blockTag : docCommentTree.getBlockTags()) {
                if (blockTag.getKind() == DocTree.Kind.PARAM) {
                    ParamTree paramTag = (ParamTree) blockTag;
                    String paramName = paramTag.getName().toString();
                    String description = paramTag.getDescription().toString().trim();
                    paramDescriptions.put(paramName, description);
                }
            }
        }
        
        return paramDescriptions;
    }
    
    /**
     * Extracts the main description from Javadoc comment, excluding @param and other block tags.
     * This helps avoid duplicate parameter descriptions in method descriptions.
     * 
     * @param element The element to extract description from
     * @param environment The doclet environment
     * @return The main description without block tags
     */
    private String extractMainDescription(Element element, DocletEnvironment environment) {
        DocTrees docTrees = environment.getDocTrees();
        DocCommentTree docCommentTree = docTrees.getDocCommentTree(element);
        
        if (docCommentTree != null) {
            // Get only the main body, excluding block tags
            return docCommentTree.getFullBody().toString().trim();
        }
        
        // Fallback to the old method if DocCommentTree is not available
        String docComment = environment.getElementUtils().getDocComment(element);
        return docComment != null ? docComment.trim() : "";
    }
    
    /**
     * Simple option implementation for command-line arguments.
     */
    @Getter
    @RequiredArgsConstructor
    private abstract static class SimpleOption implements Option {
        private final String name;
        private final int argumentCount;
        private final String description;

        @Override
        public Kind getKind() {
            return Kind.STANDARD;
        }

        @Override
        public List<String> getNames() {
            return List.of(name);
        }

        @Override
        public String getParameters() {
            return argumentCount > 0 ? "<value>" : "";
        }
    }
}