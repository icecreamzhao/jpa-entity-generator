package com.smartnews.jpa_entity_generator.rule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an interface.
 */
public class Classes implements Serializable {
    private String name;
    private List<String> genericsClassNames = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getGenericsClassNames() {
        return genericsClassNames;
    }

    public void setGenericsClassNames(List<String> genericsClassNames) {
        this.genericsClassNames = genericsClassNames;
    }
}
