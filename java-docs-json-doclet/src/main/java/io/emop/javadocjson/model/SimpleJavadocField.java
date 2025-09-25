package io.emop.javadocjson.model;

import lombok.Data;

/**
 * 简化的Javadoc字段模型，用于文本级别的解析
 * 包含字段名、修饰符和类型、描述和完整的原始文本
 */
@Data
public class SimpleJavadocField {
    
    /**
     * 字段名称
     */
    private String name;
    
    /**
     * 修饰符和类型 (例如: "static int", "public final String")
     */
    private String modifierAndType;
    
    /**
     * 字段描述
     */
    private String description;
    
    /**
     * 默认构造函数
     */
    public SimpleJavadocField() {
    }
    
    /**
     * 完整构造函数
     * 
     * @param name 字段名称
     * @param modifierAndType 修饰符和类型
     * @param description 字段描述
     */
    public SimpleJavadocField(String name, String modifierAndType, String description) {
        this.name = name;
        this.modifierAndType = modifierAndType;
        this.description = description;
    }
    
    /**
     * 简化构造函数，只有名称和描述
     * 
     * @param name 字段名称
     * @param description 字段描述
     */
    public SimpleJavadocField(String name, String description) {
        this.name = name;
        this.description = description;
    }
}