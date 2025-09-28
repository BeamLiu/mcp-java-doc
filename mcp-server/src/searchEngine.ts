import Fuse from 'fuse.js';
import { JavaDocData, JavaDocClass, SearchResult } from './types.js';

export class JavaDocSearchEngine {
  private javadocData: JavaDocData;
  private classIndex!: Fuse<JavaDocClass>;
  private methodIndex!: Fuse<any>;
  private allItemsIndex!: Fuse<any>;

  constructor(javadocData: JavaDocData) {
    this.javadocData = javadocData;
    this.buildSearchIndices();
  }

  private buildSearchIndices() {
    // 构建类索引
    this.classIndex = new Fuse(this.javadocData.classes, {
      keys: [
        { name: 'name', weight: 0.4 },
        { name: 'description', weight: 0.3 },
        { name: 'packageName', weight: 0.2 },
        { name: 'type', weight: 0.1 }
      ],
      threshold: 0.4,
      includeScore: true,
    });

    // 构建方法索引
    const methods: any[] = [];
    this.javadocData.classes.forEach((cls) => {
      // 处理详细解析的方法
      if (cls.methods) {
        cls.methods.forEach((method) => {
          methods.push({
            ...method,
            className: cls.name,
            packageName: cls.packageName,
            classType: cls.type,
            itemType: 'method'
          });
        });
      }
      
      // 处理简化解析的方法
      if (cls.simpleMethods) {
        cls.simpleMethods.forEach((method) => {
          methods.push({
            name: method.name,
            signature: method.modifierAndType,
            description: method.description,
            detailText: method.detailText,
            className: cls.name,
            packageName: cls.packageName,
            classType: cls.type,
            itemType: 'method',
            isSimple: true
          });
        });
      }
      
      // 处理详细解析的构造函数
      if (cls.constructors) {
        cls.constructors.forEach((constructor) => {
          methods.push({
            ...constructor,
            className: cls.name,
            packageName: cls.packageName,
            classType: cls.type,
            itemType: 'constructor'
          });
        });
      }
      
      // 处理简化解析的构造函数
      if (cls.simpleConstructors) {
        cls.simpleConstructors.forEach((constructor) => {
          methods.push({
            name: constructor.name,
            signature: constructor.modifierAndType,
            description: constructor.description,
            detailText: constructor.detailText,
            className: cls.name,
            packageName: cls.packageName,
            classType: cls.type,
            itemType: 'constructor',
            isSimple: true
          });
        });
      }

      // 处理详细解析的字段
      if (cls.fields) {
        cls.fields.forEach((field) => {
          methods.push({
            ...field,
            className: cls.name,
            packageName: cls.packageName,
            classType: cls.type,
            itemType: 'field'
          });
        });
      }
      
      // 处理简化解析的字段
      if (cls.simpleFields) {
        cls.simpleFields.forEach((field) => {
          methods.push({
            name: field.name,
            type: field.modifierAndType,
            description: field.description,
            detailText: field.detailText,
            className: cls.name,
            packageName: cls.packageName,
            classType: cls.type,
            itemType: 'field',
            isSimple: true
          });
        });
      }
    });

    this.methodIndex = new Fuse(methods, {
      keys: [
        { name: 'name', weight: 0.5 },
        { name: 'signature', weight: 0.3 },
        { name: 'description', weight: 0.2 }
      ],
      threshold: 0.4,
      includeScore: true,
    });

    // 构建全局索引（包含所有项目）
    const allItems = [
      ...this.javadocData.classes.map(cls => ({
        ...cls,
        itemType: 'class',
        searchText: `${cls.name} ${cls.description} ${cls.packageName || ''}`
      })),
      ...methods
    ];

    this.allItemsIndex = new Fuse(allItems, {
      keys: [
        { name: 'name', weight: 0.4 },
        { name: 'description', weight: 0.3 },
        { name: 'signature', weight: 0.2 },
        { name: 'searchText', weight: 0.1 }
      ],
      threshold: 0.4,
      includeScore: true,
    });
  }

  searchAll(query: string, limit: number = 10): SearchResult[] {
    const results = this.allItemsIndex.search(query, { limit });
    return results.map(result => this.formatSearchResult(result.item, result.score));
  }

  searchClasses(query: string, limit: number = 10): SearchResult[] {
    const results = this.classIndex.search(query, { limit });
    return results.map(result => this.formatSearchResult(result.item, result.score));
  }

  searchMethods(query: string, className?: string, limit: number = 10): SearchResult[] {
    let results = this.methodIndex.search(query, { limit: limit * 2 });
    
    if (className) {
      results = results.filter(result => 
        result.item.className.toLowerCase().includes(className.toLowerCase())
      );
    }
    
    return results
      .slice(0, limit)
      .map(result => this.formatSearchResult(result.item, result.score));
  }

  getClassByName(className: string): JavaDocClass | undefined {
    return this.javadocData.classes.find(cls => 
      cls.name === className || 
      `${cls.packageName}.${cls.name}` === className
    );
  }

  getStats() {
    return {
      totalClasses: this.javadocData.totalCount,
      packages: [...new Set(this.javadocData.classes.map(cls => cls.packageName).filter(Boolean))].length
    };
  }

  private formatSearchResult(item: any, score?: number): SearchResult {
    if (item.itemType === 'class' || !item.itemType) {
      return {
        type: 'class',
        name: item.name,
        fullName: item.packageName ? `${item.packageName}.${item.name}` : item.name,
        description: item.description || '',
        package: item.packageName,
        score
      };
    } else {
      return {
        type: item.itemType,
        name: item.name,
        fullName: `${item.className}.${item.name}`,
        description: item.description || '',
        signature: item.signature,
        parentClass: item.className,
        package: item.packageName,
        score
      };
    }
  }
}