package com.smartnews.jpa_entity_generator.rule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Rule used to generate annotations for a class.
 */
public class ClassAnnotationRule implements Serializable, ClassMatcher {

    private String className;
    private List<String> classNames = new ArrayList<>();
    private List<Annotation> annotations = new ArrayList<>();

    public static ClassAnnotationRule createGlobal(Annotation... annotations) {
        ClassAnnotationRule rule = new ClassAnnotationRule();
        rule.setAnnotations(Arrays.asList(annotations));
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

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<Annotation> annotations) {
        this.annotations = annotations;
    }
}
