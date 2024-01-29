package com.smartnews.jpa_entity_generator;

import com.smartnews.jpa_entity_generator.rule.Annotation;
import com.smartnews.jpa_entity_generator.rule.ClassAnnotationRule;
import com.smartnews.jpa_entity_generator.rule.ImportRule;
import com.smartnews.jpa_entity_generator.util.ResourceReader;
import freemarker.cache.StringTemplateLoader;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Code renderer.
 */
public class CodeRenderer {

    /**
     * Renders source code by using Freemarker template engine.
     */
    public static String render(String templatePath, RenderingData data) throws IOException, TemplateException {
        Configuration config = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        StringTemplateLoader templateLoader = new StringTemplateLoader();
        String source;
        try (InputStream is = ResourceReader.getResourceAsStream(templatePath);
             BufferedReader buffer = new BufferedReader(new InputStreamReader(is))) {
            source = buffer.lines().collect(Collectors.joining("\n"));
        }
        templateLoader.putTemplate("template", source);
        config.setTemplateLoader(templateLoader);
        config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        config.setObjectWrapper(new BeansWrapper(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS));
        config.setWhitespaceStripping(true);

        try (Writer writer = new java.io.StringWriter()) {
            Template template = config.getTemplate("template");
            template.process(data, writer);
            return writer.toString();
        }
    }

    /**
     * Data used when rendering source code.
     */
    public static class RenderingData {

        private String packageName;
        private String tableName;
        private String className;
        private String classComment;

        private boolean jpa1Compatible = false;
        private boolean requireJSR305 = false;
        // Default is false for not affecting current logic
        // Can be removed after no javax support and replace requireJSR305 to requireJakartaAnnotation
        private boolean useJakarta = false;

        private List<String> topAdditionalCodeList = new ArrayList<>();
        private List<String> bottomAdditionalCodeList = new ArrayList<>();

        private List<ImportRule> importRules = new ArrayList<>();
        private List<ClassAnnotationRule> classAnnotationRules = new ArrayList<>();
        private List<String> interfaceNames = new ArrayList<>();
        private List<String> classNames = new ArrayList<>();
        private List<Field> fields = new ArrayList<>();
        private List<Field> primaryKeyFields = new ArrayList<>();

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

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

        public String getClassComment() {
            return classComment;
        }

        public void setClassComment(String classComment) {
            this.classComment = classComment;
        }

        public boolean isJpa1Compatible() {
            return jpa1Compatible;
        }

        public void setJpa1Compatible(boolean jpa1Compatible) {
            this.jpa1Compatible = jpa1Compatible;
        }

        public boolean isRequireJSR305() {
            return requireJSR305;
        }

        public void setRequireJSR305(boolean requireJSR305) {
            this.requireJSR305 = requireJSR305;
        }

        public boolean isUseJakarta() {
            return useJakarta;
        }

        public void setUseJakarta(boolean useJakarta) {
            this.useJakarta = useJakarta;
        }

        public List<String> getTopAdditionalCodeList() {
            return topAdditionalCodeList;
        }

        public void setTopAdditionalCodeList(List<String> topAdditionalCodeList) {
            this.topAdditionalCodeList = topAdditionalCodeList;
        }

        public List<String> getBottomAdditionalCodeList() {
            return bottomAdditionalCodeList;
        }

        public void setBottomAdditionalCodeList(List<String> bottomAdditionalCodeList) {
            this.bottomAdditionalCodeList = bottomAdditionalCodeList;
        }

        public List<ImportRule> getImportRules() {
            return importRules;
        }

        public void setImportRules(List<ImportRule> importRules) {
            this.importRules = importRules;
        }

        public List<ClassAnnotationRule> getClassAnnotationRules() {
            return classAnnotationRules;
        }

        public void setClassAnnotationRules(
                List<ClassAnnotationRule> classAnnotationRules) {
            this.classAnnotationRules = classAnnotationRules;
        }

        public List<String> getInterfaceNames() {
            return interfaceNames;
        }

        public void setInterfaceNames(List<String> interfaceNames) {
            this.interfaceNames = interfaceNames;
        }

        public List<String> getClassNames() {
            return classNames;
        }

        public void setClassNames(List<String> classNames) {
            this.classNames = classNames;
        }

        public List<Field> getFields() {
            return fields;
        }

        public void setFields(List<Field> fields) {
            this.fields = fields;
        }

        public List<Field> getPrimaryKeyFields() {
            return primaryKeyFields;
        }

        public void setPrimaryKeyFields(
                List<Field> primaryKeyFields) {
            this.primaryKeyFields = primaryKeyFields;
        }

        public static class Field {
            private String name;
            private String columnName;
            private boolean nullable;
            private String type;
            private String comment;
            private String defaultValue;
            private boolean primaryKey;
            private boolean autoIncrement;
            private boolean primitive;
            private String generatedValueStrategy;
            private List<Annotation> annotations = new ArrayList<>();

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getColumnName() {
                return columnName;
            }

            public void setColumnName(String columnName) {
                this.columnName = columnName;
            }

            public boolean isNullable() {
                return nullable;
            }

            public void setNullable(boolean nullable) {
                this.nullable = nullable;
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public String getComment() {
                return comment;
            }

            public void setComment(String comment) {
                this.comment = comment;
            }

            public String getDefaultValue() {
                return defaultValue;
            }

            public void setDefaultValue(String defaultValue) {
                this.defaultValue = defaultValue;
            }

            public boolean isPrimaryKey() {
                return primaryKey;
            }

            public void setPrimaryKey(boolean primaryKey) {
                this.primaryKey = primaryKey;
            }

            public boolean isAutoIncrement() {
                return autoIncrement;
            }

            public void setAutoIncrement(boolean autoIncrement) {
                this.autoIncrement = autoIncrement;
            }

            public boolean isPrimitive() {
                return primitive;
            }

            public void setPrimitive(boolean primitive) {
                this.primitive = primitive;
            }

            public String getGeneratedValueStrategy() {
                return generatedValueStrategy;
            }

            public void setGeneratedValueStrategy(String generatedValueStrategy) {
                this.generatedValueStrategy = generatedValueStrategy;
            }

            public List<Annotation> getAnnotations() {
                return annotations;
            }

            public void setAnnotations(List<Annotation> annotations) {
                this.annotations = annotations;
            }
        }
    }
}
