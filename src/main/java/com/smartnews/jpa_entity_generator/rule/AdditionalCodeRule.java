package com.smartnews.jpa_entity_generator.rule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Rule used to generate additional code in entity classes.
 */
public class AdditionalCodeRule implements Serializable, ClassMatcher {

    private String className;
    private List<String> classNames = new ArrayList<>();
    private AdditionalCodePosition position = AdditionalCodePosition.Bottom;
    private String code;
    private String jpa1Code;

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

    public AdditionalCodePosition getPosition() {
        return position;
    }

    public void setPosition(AdditionalCodePosition position) {
        this.position = position;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getJpa1Code() {
        return jpa1Code;
    }

    public void setJpa1Code(String jpa1Code) {
        this.jpa1Code = jpa1Code;
    }
}
