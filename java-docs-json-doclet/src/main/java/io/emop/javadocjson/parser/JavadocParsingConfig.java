package io.emop.javadocjson.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Parsing configuration interface for supporting different versions of Javadoc parsing strategies
 */
public interface JavadocParsingConfig {

    /**
     * Get configuration name
     *
     * @return configuration name
     */
    String getConfigName();

    /**
     * Check if this configuration is applicable to the given HTML content
     *
     * @param htmlContent HTML content
     * @return true if applicable, false otherwise
     */
    boolean isApplicable(String htmlContent);

    /**
     * Get method parsing configuration
     *
     * @return method parsing configuration
     */
    MethodParsingConfig getMethodParsingConfig();

    /**
     * Get field parsing configuration
     *
     * @return field parsing configuration
     */
    FieldParsingConfig getFieldParsingConfig();

    /**
     * Get entry point for all classes page
     *
     * @return entry point
     */
    String getAllClassesEntryPoint();

    /**
     * Get class basic information parsing configuration
     *
     * @return class basic information parsing configuration
     */
    ClassInfoParsingConfig getClassInfoParsingConfig();

    /**
     * Get inheritance relationship parsing configuration
     *
     * @return inheritance relationship parsing configuration
     */
    InheritanceParsingConfig getInheritanceParsingConfig();

    /**
     * Get constructor parsing configuration
     *
     * @return constructor parsing configuration
     */
    ConstructorParsingConfig getConstructorParsingConfig();

    /**
     * Method parsing configuration interface
     */
    interface MethodParsingConfig {

        /**
         * Get method selector
         *
         * @return CSS selector string
         */
        String getMethodSelector();

        /**
         * Extract method name from element
         *
         * @param methodElement method element
         * @return method name
         */
        String extractMethodName(Element methodElement);

        /**
         * Method with parameters, e.g. valueOf(java.lang.String name)
         *
         * @param methodElement method element
         * @return method with parameters
         */
        String extractMethodNameWithParameters(Element methodElement);

        /**
         * Extract modifiers and type from element
         *
         * @param methodElement method element
         * @return modifiers and type
         */
        String extractModifierAndType(Element methodElement);

        /**
         * Extract description from element
         *
         * @param methodElement method element
         * @return description
         */
        String extractDescription(Element methodElement);

        /**
         * Extract raw text from element
         *
         * @param methodElement method element
         * @param doc           the full class document
         * @return raw text
         */
        String extractDetailText(Element methodElement, Document doc);

        /**
         * Validate if element is a valid method element
         *
         * @param element element to validate
         * @return true if it's a valid method element, false otherwise
         */
        boolean isValidMethodElement(Element element);
    }

    /**
     * Field parsing configuration interface
     */
    interface FieldParsingConfig {

        /**
         * Get field selector
         *
         * @return CSS selector string
         */
        String getFieldSelector();

        /**
         * Extract field name from element
         *
         * @param fieldElement field element
         * @return field name
         */
        String extractFieldName(Element fieldElement);

        /**
         * Extract modifiers and type from element
         *
         * @param fieldElement field element
         * @return modifiers and type
         */
        String extractModifierAndType(Element fieldElement);

        /**
         * Extract description from element
         *
         * @param fieldElement field element
         * @return description
         */
        String extractDescription(Element fieldElement);

        /**
         * Extract raw text from element
         *
         * @param fieldElement field element
         * @return raw text
         */
        String extractRawText(Element fieldElement);

        /**
         * Validate if element is a valid field element
         *
         * @param element element to validate
         * @return true if it's a valid field element, false otherwise
         */
        boolean isValidFieldElement(Element element);
    }

    /**
     * Class basic information parsing configuration interface
     */
    interface ClassInfoParsingConfig {

        /**
         * Extract class description from document
         *
         * @param doc document object
         * @return class description
         */
        String extractClassDescription(Document doc);

        /**
         * Extract class type from document
         *
         * @param doc document object
         * @return class type (class, interface, enum, annotation)
         */
        String extractClassType(Document doc);

        /**
         * Extract modifiers from document
         *
         * @param doc document object
         * @return modifiers list
         */
        java.util.List<String> extractModifiers(Document doc);
    }

    /**
     * Inheritance relationship parsing configuration interface
     */
    interface InheritanceParsingConfig {

        /**
         * Extract superclass from document
         *
         * @param doc document object
         * @return superclass name, null if none
         */
        String extractSuperClass(Document doc);

        /**
         * Extract implemented interfaces from document
         *
         * @param doc document object
         * @return interfaces list
         */
        java.util.List<String> extractInterfaces(Document doc);
    }

    /**
     * Constructor parsing configuration interface
     */
    interface ConstructorParsingConfig {

        /**
         * Get constructor selector
         *
         * @return CSS selector string
         */
        String getConstructorSelector();

        /**
         * Extract constructor name from element
         *
         * @param constructorElement constructor element
         * @return constructor name
         */
        String extractConstructorName(Element constructorElement);

        /**
         * Extract description from element
         *
         * @param constructorElement constructor element
         * @return description
         */
        String extractDescription(Element constructorElement);

        /**
         * Validate if element is a valid constructor element
         *
         * @param element element to validate
         * @return true if it's a valid constructor element, false otherwise
         */
        boolean isValidConstructorElement(Element element);
    }
}