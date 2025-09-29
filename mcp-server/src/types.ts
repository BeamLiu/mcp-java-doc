// 定义 Java 文档的数据结构，与 Java 模型完全兼容
export interface JavaDocClass {
  name: string;
  packageName: string | null;
  type: string; // 支持 'class', 'interface', 'enum', 'annotation' 等
  description: string;
  modifiers: string[];
  superClass: string | null;
  interfaces: string[];
  
  // 统一字段，可以包含详细和简化的解析结果
  constructors: BaseConstructor[];
  methods: BaseMethod[];
  fields: BaseField[];
}

// 基类接口定义 - 只包含JSON序列化的字段
export interface BaseMethod {
  name: string;
  description: string;
}

export interface BaseConstructor {
  name: string;
  description: string;
}

export interface BaseField {
  name: string;
  description: string;
}

// 详细类型定义（继承自基类）
export interface Constructor extends BaseConstructor {
  signature: string;
  modifiers: string[];
  parameters: Parameter[];
  exceptions: string[];
}

export interface Method extends BaseMethod {
  signature: string;
  modifiers: string[];
  returnType?: string;
  parameters: Parameter[];
  exceptions: string[];
}

export interface Field extends BaseField {
  type: string;
  modifiers: string[];
  defaultValue?: string; // 添加缺失的 defaultValue 字段
}

export interface Parameter {
  name: string;
  type: string;
  description: string;
}

// 简化类型定义（对应Java的Simple类）
export interface SimpleMethod extends BaseMethod {
  modifierAndType?: string; // 修饰符和类型（如："static CheckScope", "public static int"）
  detailText?: string; // 包含方法签名和详细描述的完整原始文本
}

export interface SimpleConstructor extends BaseConstructor {
  modifierAndType?: string; // 修饰符和类型
  detailText?: string; // 包含构造函数签名和详细描述的完整原始文本
}

export interface SimpleField extends BaseField {
  modifierAndType?: string; // 修饰符和类型（如："static int", "public final String"）
  detailText?: string; // 包含字段详细描述的完整原始文本
}

// 类型判断工具函数
export function isDetailedMethod(method: BaseMethod): method is Method {
  return 'signature' in method && 'modifiers' in method;
}

export function isDetailedConstructor(constructor: BaseConstructor): constructor is Constructor {
  return 'signature' in constructor && 'modifiers' in constructor;
}

export function isDetailedField(field: BaseField): field is Field {
  return 'type' in field && 'modifiers' in field;
}

export interface SearchResult {
  type: 'class' | 'method' | 'constructor' | 'field';
  name: string;
  fullName: string;
  description: string;
  signature?: string;
  parentClass?: string;
  package?: string;
  score?: number;
}

export interface JavaDocData {
  classes: JavaDocClass[];
  totalCount: number;
}