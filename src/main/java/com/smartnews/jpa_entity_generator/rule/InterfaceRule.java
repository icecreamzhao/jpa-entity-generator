package com.smartnews.jpa_entity_generator.rule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Rule used to generate an interface.
 */
public class InterfaceRule implements Serializable, ClassMatcher {

    /**
     * A single partial-matching rule.
     */
    private String className;

    /**
     * multiple partial-matching rule.
     */
    private List<String> classNames = new ArrayList<>();

    /**
     * The interfaces to be implemented.
     */
    private List<Interface> interfaces = new ArrayList<>();

    /**
     * The classes to be extended.
     */
    private List<Classes> classes = new ArrayList<>();

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

    public List<Interface> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(List<Interface> interfaces) {
        this.interfaces = interfaces;
    }

    public List<Classes> getClasses() {
        return classes;
    }

    public void setClasses(List<Classes> classes) {
        this.classes = classes;
    }
}
