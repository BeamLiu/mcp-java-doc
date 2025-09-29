import Fuse from 'fuse.js';
import { JavaDocData, JavaDocClass, SearchResult } from './types.js';

export type SearchMode = 'fuzzy' | 'keyword' | 'regex';

export class JavaDocSearchEngine {
  private javadocData: JavaDocData;
  private classIndex!: Fuse<JavaDocClass>;
  private methodIndex!: Fuse<any>;
  private fieldIndex!: Fuse<any>;
  private allItemsIndex!: Fuse<any>;
  private allClasses: JavaDocClass[] = [];
  private allMethods: any[] = [];
  private allFields: any[] = [];

  constructor(javadocData: JavaDocData) {
    this.javadocData = javadocData;
    this.buildSearchIndices();
  }

  private buildSearchIndices() {
    // Store all data for non-fuzzy search
    this.allClasses = this.javadocData.classes;
    
    // Build class index
    this.classIndex = new Fuse(this.javadocData.classes, {
      keys: [
        { name: 'name', weight: 0.5 },
        { name: 'description', weight: 0.3 },
        { name: 'packageName', weight: 0.2 },
        { name: 'type', weight: 0.1 }
      ],
      threshold: 0.5,
      includeScore: true,
    });

    // Build method and field data
    const methods: any[] = [];
    const fields: any[] = [];
    
    this.javadocData.classes.forEach((cls) => {
      // Process detailed parsed methods
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
      
      // Process detailed parsed constructors
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
      
      // Process detailed parsed fields
      if (cls.fields) {
        cls.fields.forEach((field) => {
          const fieldItem = {
            ...field,
            className: cls.name,
            packageName: cls.packageName,
            classType: cls.type,
            itemType: 'field'
          };
          fields.push(fieldItem);
          // Fields are also added to methods array for backward compatibility
          methods.push(fieldItem);
        });
      }
    });

    this.allMethods = methods;
    this.allFields = fields;

    // Build method index
    this.methodIndex = new Fuse(methods, {
      keys: [
        { name: 'name', weight: 0.5 },
        { name: 'signature', weight: 0.3 },
        { name: 'description', weight: 0.2 }
      ],
      threshold: 0.5,
      includeScore: true,
    });

    // Build field index
    this.fieldIndex = new Fuse(fields, {
      keys: [
        { name: 'name', weight: 0.5 },
        { name: 'type', weight: 0.3 },
        { name: 'description', weight: 0.2 }
      ],
      threshold: 0.5,
      includeScore: true,
    });

    // Build global index (containing all items)
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
        { name: 'name', weight: 0.5 },
        { name: 'description', weight: 0.3 },
        { name: 'signature', weight: 0.2 },
        { name: 'searchText', weight: 0.1 }
      ],
      threshold: 0.5,
      includeScore: true,
    });
  }

  searchAll(query: string, limit: number = 10, mode: SearchMode = 'fuzzy'): SearchResult[] {
    // Handle empty queries
    if (!query || query.trim() === '') {
      return [];
    }

    if (mode === 'fuzzy') {
      const results = this.allItemsIndex.search(query, { limit });
      return results
        .sort((a, b) => (a.score || 0) - (b.score || 0)) // Sort by score in ascending order (lower score is better)
        .map(result => this.formatSearchResult(result.item, result.score));
    } else {
      const allItems = [
        ...this.allClasses.map(cls => ({ ...cls, itemType: 'class' })),
        ...this.allMethods
      ];
      const filteredItems = this.filterByMode(allItems, query, mode);
      return filteredItems
        .slice(0, limit)
        .map(item => this.formatSearchResult(item));
    }
  }

  searchClasses(query: string, limit: number = 10, mode: SearchMode = 'fuzzy'): SearchResult[] {
    // Handle empty queries
    if (!query || query.trim() === '') {
      return [];
    }

    if (mode === 'fuzzy') {
      const results = this.classIndex.search(query, { limit });
      return results
        .sort((a, b) => (a.score || 0) - (b.score || 0)) // Sort by score in ascending order (lower score is better)
        .map(result => this.formatSearchResult(result.item, result.score));
    } else {
      const filteredClasses = this.filterByMode(this.allClasses, query, mode);
      return filteredClasses
        .slice(0, limit)
        .map(cls => this.formatSearchResult({ ...cls, itemType: 'class' }));
    }
  }

  searchMethods(query: string, className?: string, limit: number = 10, mode: SearchMode = 'fuzzy'): SearchResult[] {
    // 处理空查询
    if (!query || query.trim() === '') {
      return [];
    }

    if (mode === 'fuzzy') {
      let results = this.methodIndex.search(query, { limit: limit * 2 });
      
      if (className) {
        results = results.filter(result => 
          result.item.className.toLowerCase().includes(className.toLowerCase())
        );
      }
      
      return results
        .sort((a, b) => (a.score || 0) - (b.score || 0)) // 按score升序排序（score越小越好）
        .slice(0, limit)
        .map(result => this.formatSearchResult(result.item, result.score));
    } else {
      let filteredMethods = this.filterByMode(this.allMethods, query, mode);
      
      if (className) {
        filteredMethods = filteredMethods.filter(method => 
          method.className.toLowerCase().includes(className.toLowerCase())
        );
      }
      
      return filteredMethods
        .slice(0, limit)
        .map(method => this.formatSearchResult(method));
    }
  }

  searchFields(query: string, className?: string, limit: number = 10, mode: SearchMode = 'fuzzy'): SearchResult[] {
    // 处理空查询
    if (!query || query.trim() === '') {
      return [];
    }

    if (mode === 'fuzzy') {
      let results = this.fieldIndex.search(query, { limit: limit * 2 });
      
      if (className) {
        results = results.filter(result => 
          result.item.className.toLowerCase().includes(className.toLowerCase())
        );
      }
      
      return results
        .sort((a, b) => (a.score || 0) - (b.score || 0)) // 按score升序排序（score越小越好）
        .slice(0, limit)
        .map(result => this.formatSearchResult(result.item, result.score));
    } else {
      let filteredFields = this.filterByMode(this.allFields, query, mode);
      
      if (className) {
        filteredFields = filteredFields.filter(field => 
          field.className.toLowerCase().includes(className.toLowerCase())
        );
      }
      
      return filteredFields
        .slice(0, limit)
        .map(field => this.formatSearchResult(field));
    }
  }

  getClassByName(className: string): JavaDocClass | undefined {
    return this.javadocData.classes.find(cls => 
      cls.name === className || 
      `${cls.packageName}.${cls.name}` === className
    );
  }

  private filterByMode(items: any[], query: string, mode: SearchMode): any[] {
    // process empty query
    if (!query || query.trim() === '') {
      return [];
    }

    const lowerQuery = query.toLowerCase();
    
    switch (mode) {
      case 'keyword':
        return items.filter(item => {
          const searchableText = this.getSearchableText(item).toLowerCase();
          return searchableText.includes(lowerQuery);
        });
        
      case 'regex':
        // return all for "*"
        if (query.trim() === '*') {
          return items;
        }
        
        try {
          const regex = new RegExp(query, 'i');
          return items.filter(item => {
            // test regex using the name field
            const name = item.name || '';
            return regex.test(name);
          });
        } catch (error) {
          // fallback to keyword search
          console.warn('Invalid regex pattern, falling back to keyword search:', error);
          return this.filterByMode(items, query, 'keyword');
        }
        
      default:
        return items;
    }
  }

  private getSearchableText(item: any): string {
    const parts = [
      item.name || '',
      item.description || '',
      item.signature || '',
      item.packageName || '',
      item.className || '',
      item.type || ''
    ];
    return parts.join(' ');
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