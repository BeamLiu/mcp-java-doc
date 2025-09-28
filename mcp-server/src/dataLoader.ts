import { readFileSync, readdirSync, existsSync } from 'fs';
import { join } from 'path';
import { JavaDocClass, JavaDocData } from './types.js';

export class JavaDocDataLoader {
  private javadocJsonPath: string;

  constructor(javadocJsonPath: string) {
    this.javadocJsonPath = javadocJsonPath;
  }

  /**
   * Load all JavaDoc data from the specified JSON repository directory
   */
  loadAllData(): JavaDocData {
    const allClasses = this.loadFromDirectory(this.javadocJsonPath);
    const uniqueClasses = this.deduplicateClasses(allClasses);

    return {
      classes: uniqueClasses,
      totalCount: uniqueClasses.length
    };
  }

  /**
   * Load JavaDoc classes from a directory containing JSON files
   */
  private loadFromDirectory(dirPath: string): JavaDocClass[] {
    if (!existsSync(dirPath)) {
      console.warn(`JavaDoc JSON directory not found: ${dirPath}`);
      return [];
    }

    const classes: JavaDocClass[] = [];
    const files = readdirSync(dirPath).filter(file => file.endsWith('.json'));

    console.log(`Loading ${files.length} JSON files from: ${dirPath}`);

    for (const file of files) {
      try {
        const filePath = join(dirPath, file);
        const content = readFileSync(filePath, 'utf-8');
        const classData = JSON.parse(content) as JavaDocClass;
        
        // Validate data structure
        if (this.isValidClassData(classData)) {
          classes.push(classData);
        } else {
          console.warn(`Invalid class data in file: ${file}`);
        }
      } catch (error) {
        console.error(`Error loading file ${file}:`, error);
      }
    }

    return classes;
  }

  private isValidClassData(data: any): data is JavaDocClass {
    return (
      data &&
      typeof data.name === 'string' &&
      typeof data.type === 'string' &&
      // 支持详细解析字段或简化解析字段
      (Array.isArray(data.methods) || Array.isArray(data.simpleMethods)) &&
      (Array.isArray(data.constructors) || Array.isArray(data.simpleConstructors))
    );
  }

  /**
   * Remove duplicate classes based on their full name (package + class name)
   * If duplicates exist, keep the one with more detailed information
   */
  private deduplicateClasses(classes: JavaDocClass[]): JavaDocClass[] {
    const classMap = new Map<string, JavaDocClass>();
    
    for (const cls of classes) {
      const key = `${cls.packageName || ''}.${cls.name}`;
      
      // If already exists, keep the one with more detailed information
      if (!classMap.has(key) || this.hasMoreDetailedInfo(cls, classMap.get(key)!)) {
        classMap.set(key, cls);
      }
    }
    
    return Array.from(classMap.values());
  }

  /**
   * Determine if a class has more detailed information than another
   */
  private hasMoreDetailedInfo(newClass: JavaDocClass, existingClass: JavaDocClass): boolean {
    // Priority: detailed parsing > simplified parsing
    const newHasDetailed = this.hasDetailedInfo(newClass);
    const existingHasDetailed = this.hasDetailedInfo(existingClass);
    
    if (newHasDetailed && !existingHasDetailed) {
      return true;
    }
    
    if (!newHasDetailed && existingHasDetailed) {
      return false;
    }
    
    // If both have detailed info or both don't, compare total method count
    const newMethodCount = this.getTotalMethodCount(newClass);
    const existingMethodCount = this.getTotalMethodCount(existingClass);
    
    return newMethodCount > existingMethodCount;
  }

  /**
   * Check if a class has detailed parsing information
   */
  private hasDetailedInfo(cls: JavaDocClass): boolean {
    return (cls.methods && cls.methods.length > 0) || 
           (cls.fields && cls.fields.length > 0) ||
           (cls.constructors && cls.constructors.length > 0);
  }

  /**
   * Get total method count (including both detailed and simplified)
   */
  private getTotalMethodCount(cls: JavaDocClass): number {
    let count = 0;
    if (cls.methods) count += cls.methods.length;
    if (cls.simpleMethods) count += cls.simpleMethods.length;
    if (cls.constructors) count += cls.constructors.length;
    if (cls.simpleConstructors) count += cls.simpleConstructors.length;
    return count;
  }
}