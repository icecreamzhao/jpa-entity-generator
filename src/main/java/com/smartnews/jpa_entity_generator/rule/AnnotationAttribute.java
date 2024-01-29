package com.smartnews.jpa_entity_generator.rule;

import java.io.Serializable;

/**
 * Represents an attribute of a Java annotation.
 */
public class AnnotationAttribute implements Serializable {

    private String name;
    private String value;
    private String code;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
