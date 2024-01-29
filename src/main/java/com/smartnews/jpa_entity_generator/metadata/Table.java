package com.smartnews.jpa_entity_generator.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Database metadata: a table
 */
public class Table {

    private String name;
    private Optional<String> schema = Optional.empty();
    private Optional<String> description = Optional.empty();
    private List<Column> columns = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Optional<String> getSchema() {
        return schema;
    }

    public void setSchema(Optional<String> schema) {
        this.schema = schema;
    }

    public Optional<String> getDescription() {
        return description;
    }

    public void setDescription(Optional<String> description) {
        this.description = description;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }
}
