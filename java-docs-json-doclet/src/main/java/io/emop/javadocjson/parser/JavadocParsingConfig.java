package io.emop.javadocjson.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * 解析配置接口，用于支持不同版本的Javadoc解析策略
 */
public interface JavadocParsingConfig {

    /**
     * 获取配置名称
     *
     * @return 配置名称
     */
    String getConfigName();

    /**
     * 检查此配置是否适用于给定的HTML内容
     *
     * @param htmlContent HTML内容
     * @return 如果适用返回true，否则返回false
     */
    boolean isApplicable(String htmlContent);

    /**
     * 获取方法解析配置
     *
     * @return 方法解析配置
     */
    MethodParsingConfig getMethodParsingConfig();

    /**
     * 获取字段解析配置
     *
     * @return 字段解析配置
     */
    FieldParsingConfig getFieldParsingConfig();

    /**
     * 获取所有类页面的入口点
     *
     * @return 入口点
     */
    String getAllClassesEntryPoint();

    /**
     * 方法解析配置接口
     */
    interface MethodParsingConfig {

        /**
         * 获取方法选择器
         *
         * @return CSS选择器字符串
         */
        String getMethodSelector();

        /**
         * 从元素中提取方法名
         *
         * @param methodElement 方法元素
         * @return 方法名
         */
        String extractMethodName(Element methodElement);

        /**
         * 帶参数的方法， 例如  valueOf(java.lang.String name)
         *
         * @param methodElement 方法元素
         * @return 带参数的方法
         */
        String extractMethodNameWithParameters(Element methodElement);

        /**
         * 从元素中提取修饰符和类型
         *
         * @param methodElement 方法元素
         * @return 修饰符和类型
         */
        String extractModifierAndType(Element methodElement);

        /**
         * 从元素中提取描述
         *
         * @param methodElement 方法元素
         * @return 描述
         */
        String extractDescription(Element methodElement);

        /**
         * 从元素中提取原始文本
         *
         * @param methodElement 方法元素
         * @param doc           the full class document
         * @return 原始文本
         */
        String extractDetailText(Element methodElement, Document doc);

        /**
         * 验证元素是否为有效的方法元素
         *
         * @param element 要验证的元素
         * @return 如果是有效的方法元素返回true，否则返回false
         */
        boolean isValidMethodElement(Element element);
    }

    /**
     * 字段解析配置接口
     */
    interface FieldParsingConfig {

        /**
         * 获取字段选择器
         *
         * @return CSS选择器字符串
         */
        String getFieldSelector();

        /**
         * 从元素中提取字段名
         *
         * @param fieldElement 字段元素
         * @return 字段名
         */
        String extractFieldName(Element fieldElement);

        /**
         * 从元素中提取修饰符和类型
         *
         * @param fieldElement 字段元素
         * @return 修饰符和类型
         */
        String extractModifierAndType(Element fieldElement);

        /**
         * 从元素中提取描述
         *
         * @param fieldElement 字段元素
         * @return 描述
         */
        String extractDescription(Element fieldElement);

        /**
         * 从元素中提取原始文本
         *
         * @param fieldElement 字段元素
         * @return 原始文本
         */
        String extractRawText(Element fieldElement);

        /**
         * 验证元素是否为有效的字段元素
         *
         * @param element 要验证的元素
         * @return 如果是有效的字段元素返回true，否则返回false
         */
        boolean isValidFieldElement(Element element);
    }
}