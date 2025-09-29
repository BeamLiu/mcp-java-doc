// Define Java documentation data structures, fully compatible with Java models
export interface JavaDocClass {
  name: string;
  packageName: string | null;
  type: string; // Supports 'class', 'interface', 'enum', 'annotation', etc.
  description: string;
  modifiers: string[];
  superClass: string | null;
  interfaces: string[];
  
  // Unified fields that can contain both detailed and simplified parsing results
  constructors: BaseConstructor[];
  methods: BaseMethod[];
  fields: BaseField[];
}

// Base interface definitions - only contains JSON serializable fields
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

// Detailed type definitions (inheriting from base classes)
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
  defaultValue?: string; // Add missing defaultValue field
}

export interface Parameter {
  name: string;
  type: string;
  description: string;
}

// Simplified type definitions (corresponding to Java Simple classes)
export interface SimpleMethod extends BaseMethod {
  modifierAndType?: string; // Modifier and type (e.g., "static CheckScope", "public static int")
  detailText?: string; // Complete original text containing method signature and detailed description
}

export interface SimpleConstructor extends BaseConstructor {
  modifierAndType?: string; // Modifier and type
  detailText?: string; // Complete original text containing constructor signature and detailed description
}

export interface SimpleField extends BaseField {
  modifierAndType?: string; // Modifier and type (e.g., "static int", "public final String")
  detailText?: string; // Complete original text containing detailed field description
}

// Type checking utility functions
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