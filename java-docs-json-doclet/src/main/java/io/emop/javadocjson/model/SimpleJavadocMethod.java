package io.emop.javadocjson.model;

import lombok.Data;

/**
 * 简化的Javadoc方法模型，用于文本级别的解析
 * 包含方法名、修饰符和类型、描述和完整的原始文本
 */
@Data
public class SimpleJavadocMethod {
    
    /**
     * 方法名称
     */
    private String name;
    
    /**
     * 修饰符和类型 (例如: "static CheckScope", "public static int")
     */
    private String modifierAndType;
    
    /**
     * 方法描述
     */
    private String description;
    
    /**
     * 完整的原始文本，包含方法签名和详细描述
     */
    private String detailText;
    
    /**
     * 默认构造函数
     */
    public SimpleJavadocMethod() {
    }
    
    /**
     * 完整构造函数
     * 
     * @param name 方法名称
     * @param modifierAndType 修饰符和类型
     * @param description 方法描述
     * @param detailText 完整的原始文本
     */
    public SimpleJavadocMethod(String name, String modifierAndType, String description, String detailText) {
        this.name = name;
        this.modifierAndType = modifierAndType;
        this.description = description;
        this.detailText = detailText;
    }
    
    /**
     * 构造函数
     * 
     * @param name 方法名称
     * @param description 方法描述
     * @param detailText 完整的原始文本
     */
    public SimpleJavadocMethod(String name, String description, String detailText) {
        this.name = name;
        this.description = description;
        this.detailText = detailText;
    }
    
    /**
     * 简化构造函数，只有名称和描述
     * 
     * @param name 方法名称
     * @param description 方法描述
     */
    public SimpleJavadocMethod(String name, String description) {
        this.name = name;
        this.description = description;
        this.detailText = "";
    }
}