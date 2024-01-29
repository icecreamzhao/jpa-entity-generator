package com.smartnews.jpa_entity_generator.rule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Rule used to generate imports.
 */
public class ImportRule implements Serializable, ClassMatcher {

    private String className;
    private List<String> classNames = new ArrayList<>();
    private String importValue;

    public boolean importValueContains(String className) {
        if (importValue.startsWith("static")) {
            return false;
        }
        if (importValue.endsWith(".*")) {
            return className.replaceFirst("\\.[^\\.]+$", ".*").equals(importValue);
        } else {
            return className.equals(importValue);
        }
    }

    public static ImportRule createGlobal(String importValue) {
        ImportRule rule = new ImportRule();
        rule.setImportValue(importValue);
        return rule;
    }

    @Override
    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public List<String> getClassNames() {
        return classNames;
    }

    public void setClassNames(List<String> classNames) {
        this.classNames = classNames;
    }

    public String getImportValue() {
        return importValue;
    }

    public void setImportValue(String importValue) {
        this.importValue = importValue;
    }
}