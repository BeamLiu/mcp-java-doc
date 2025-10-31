import { readFileSync, readdirSync, existsSync } from 'fs';
import { join } from 'path';
import { JavaDocClass, JavaDocData } from './types.js';

export class JavaDocDataLoader {
  private javadocJsonPaths: string[];

  constructor(javadocJsonPaths: string | string[]) {
    this.javadocJsonPaths = Array.isArray(javadocJsonPaths) ? javadocJsonPaths : [javadocJsonPaths];
  }

  /**
   * Load all JavaDoc data from the specified JSON repository directories
   */
  loadAllData(): JavaDocData {
    const allClasses: JavaDocClass[] = [];
    
    // Load from all specified paths
    for (const path of this.javadocJsonPaths) {
      const classes = this.loadFromDirectory(path);
      allClasses.push(...classes);
    }
    
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

    console.error(`Loading ${files.length} JSON files from: ${dirPath}`);

    for (const file of files) {
      try {
        const filePath = join(dirPath, file);
        const content = readFileSync(filePath, 'utf-8');
        const jsonData = JSON.parse(content);
        
        // Check if this is a package-structured JSON file
        if (this.isPackageStructuredData(jsonData)) {
          const extractedClasses = this.extractClassesFromPackageStructure(jsonData);
          classes.push(...extractedClasses);
          console.error(`Extracted ${extractedClasses.length} classes from package-structured file: ${file}`);
        } else if (this.isValidClassData(jsonData)) {
          // Handle single class JSON files
          classes.push(jsonData as JavaDocClass);
        } else {
          console.warn(`Invalid data structure in file: ${file}`);
        }
      } catch (error) {
        console.error(`Error loading file ${file}:`, error);
      }
    }

    return classes;
  }

  /**
   * Check if the JSON data has package structure (contains packages array)
   */
  private isPackageStructuredData(data: any): boolean {
    return data && Array.isArray(data.packages);
  }

  /**
   * Extract classes from package-structured JSON data
   */
  private extractClassesFromPackageStructure(data: any): JavaDocClass[] {
    const classes: JavaDocClass[] = [];
    
    if (!data.packages || !Array.isArray(data.packages)) {
      return classes;
    }

    for (const pkg of data.packages) {
      if (!pkg.classes || !Array.isArray(pkg.classes)) {
        continue;
      }

      for (const cls of pkg.classes) {
        // Add package name to the class if not already present
        if (!cls.packageName && pkg.name) {
          cls.packageName = pkg.name;
        }
        
        // Validate the extracted class data
        if (this.isValidClassData(cls)) {
          classes.push(cls as JavaDocClass);
        } else {
          console.warn(`Invalid class data for ${cls.name || 'unknown'} in package ${pkg.name || 'unknown'}`);
        }
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
      (Array.isArray(data.methods)) &&
      (Array.isArray(data.constructors))
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
    return !!(cls.methods && cls.methods.length > 0) || 
           !!(cls.fields && cls.fields.length > 0) ||
           !!(cls.constructors && cls.constructors.length > 0);
  }

  /**
   * Get total method count (including both detailed and simplified)
   */
  private getTotalMethodCount(cls: JavaDocClass): number {
    let count = 0;
    if (cls.methods) count += cls.methods.length;
    if (cls.constructors) count += cls.constructors.length;
    return count;
  }
}