package com.smartnews.jpa_entity_generator.rule;

import java.io.Serializable;

/**
 * Rule used to determine the Java class name for an entity.
 */
public class ClassNameRule implements Serializable {

    private String tableName;
    private String className;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
