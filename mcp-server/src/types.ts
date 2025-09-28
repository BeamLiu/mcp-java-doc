// 定义 Java 文档的数据结构，与 Java 模型完全兼容
export interface JavaDocClass {
  name: string;
  packageName: string | null;
  type: string; // 支持 'class', 'interface', 'enum', 'annotation' 等
  description: string;
  modifiers: string[];
  superClass: string | null;
  interfaces: string[];
  
  // 详细解析字段（来自 JsonDoclet）
  constructors: Constructor[];
  methods: Method[];
  fields: Field[];
  
  // 简化解析字段（来自 HTML 页面）
  simpleMethods?: SimpleMethod[];
  simpleFields?: SimpleField[];
  simpleConstructors?: SimpleConstructor[];
}

export interface Constructor {
  name: string;
  signature: string;
  description: string;
  modifiers: string[];
  parameters: Parameter[];
  exceptions: string[];
}

export interface Method {
  name: string;
  signature: string;
  description: string;
  modifiers: string[];
  returnType?: string;
  parameters: Parameter[];
  exceptions: string[];
}

export interface Field {
  name: string;
  type: string;
  description: string;
  modifiers: string[];
  defaultValue?: string; // 添加缺失的 defaultValue 字段
}

export interface Parameter {
  name: string;
  type: string;
  description: string;
}

// 简化模型定义（对应 Java 的 Simple* 类）
export interface SimpleMethod {
  name: string;
  modifierAndType: string; // 修饰符和类型（如："static CheckScope", "public static int"）
  description: string;
  detailText: string; // 包含方法签名和详细描述的完整原始文本
}

export interface SimpleField {
  name: string;
  modifierAndType: string;
  description: string;
  detailText: string;
}

export interface SimpleConstructor {
  name: string;
  modifierAndType: string;
  description: string;
  detailText: string;
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