package com.smartnews.jpa_entity_generator.config;

import com.smartnews.jpa_entity_generator.rule.*;
import com.smartnews.jpa_entity_generator.util.ResourceReader;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Code generator's configuration.
 */
public class CodeGeneratorConfig implements Serializable {

    // ----------
    // NOTE: Explicitly having NoArgsConstructor/AllArgsConstructor is necessary as as a workaround to enable using @Builder
    // see also: https://github.com/rzwitserloot/lombok/issues/816
    public static final List<ClassAnnotationRule> CLASS_ANNOTATIONS_NECESSARY_FOR_LOMBOK_BUILDER = Arrays.asList(
            ClassAnnotationRule.createGlobal(Annotation.fromClassName("lombok.NoArgsConstructor")),
            ClassAnnotationRule.createGlobal(Annotation.fromClassName("lombok.AllArgsConstructor"))
    );

    public static final List<ImportRule> IMPORTS_NECESSARY_FOR_LOMBOK_BUILDER = Arrays.asList(
            ImportRule.createGlobal("lombok.NoArgsConstructor"),
            ImportRule.createGlobal("lombok.AllArgsConstructor")
    );

    // ----------
    // Preset

    // NOTE: @Table(name = "${tableName}") needs tableName of target table.
//    private static final List<ClassAnnotationRule> PRESET_CLASS_ANNOTATIONS = List.of(
//            ClassAnnotationRule.createGlobal(Annotation.fromClassName("lombok.Data")));

    private static final List<ImportRule> PRESET_IMPORTS = Arrays.asList(
            ImportRule.createGlobal("java.sql.*"),
            // Can be removed after no javax support and replace to jakarta.persistence.*
            ImportRule.createGlobal("javax.persistence.*")
//            ImportRule.createGlobal("lombok.Data")
    );

    private static final List<ImportRule> PRESET_JAKARTA_IMPORTS = Arrays.asList(
            ImportRule.createGlobal("java.sql.*"),
            ImportRule.createGlobal("jakarta.persistence.*")
//            ImportRule.createGlobal("lombok.Data")
    );

    // Can be removed after no javax support
    private static final List<ImportRule> JSR_305_PRESET_IMPORTS = Arrays.asList(
            ImportRule.createGlobal("javax.annotation.Nonnull"),
            ImportRule.createGlobal("javax.annotation.Nullable")
    );

    private static final List<ImportRule> JAKARTA_ANNOTATION_PRESET_IMPORTS = Arrays.asList(
            ImportRule.createGlobal("jakarta.annotation.Nonnull"),
            ImportRule.createGlobal("jakarta.annotation.Nullable")
    );
    // ----------

    public CodeGeneratorConfig() {
    }

    public void loadEnvVariables() {
        // JDBC settings
        JDBCSettings settings = getJdbcSettings();
        if (hasEnvVariables(settings.getUrl())) {
            settings.setUrl(replaceEnvVariables(settings.getUrl()));
        }
        if (hasEnvVariables(settings.getUsername())) {
            settings.setUsername(replaceEnvVariables(settings.getUsername()));
        }
        if (hasEnvVariables(settings.getPassword())) {
            settings.setPassword(replaceEnvVariables(settings.getPassword()));
        }
        if (hasEnvVariables(settings.getDriverClassName())) {
            settings.setDriverClassName(replaceEnvVariables(settings.getDriverClassName()));
        }
    }

    static boolean hasEnvVariables(String value) {
        return value != null && value.contains("${");
    }

    private static final Pattern REPLACE_ENV_VARIABLES_PATTERN = Pattern.compile("(\\$\\{[^}]+\\})");

    static String replaceEnvVariables(String value) {
        Matcher matcher = REPLACE_ENV_VARIABLES_PATTERN.matcher(value);
        if (matcher.find()) {
            String replacedValue = value;
            Map<String, String> envVariables = System.getenv();

            for (int i = 0; i < matcher.groupCount(); i++) {
                String grouped = matcher.group(i + 1);
                String envKey = grouped.replaceAll("[\\$\\{\\}]", "");
                String envValue = envVariables.get(envKey);
                if (envValue == null) {
                    throw new IllegalStateException("Env variable: " + envKey + " was not found!");
                } else {
                    replacedValue = replacedValue.replace(grouped, envValue);
                }
            }
            return replacedValue;
        } else {
            return value;
        }
    }

    public void setUpPresetRules() {
//        getClassAnnotationRules().addAll(0, PRESET_CLASS_ANNOTATIONS);
        if (useJakarta) {
            getImportRules().addAll(0, PRESET_JAKARTA_IMPORTS);
        } else {
            getImportRules().addAll(0, PRESET_IMPORTS);
        }
        if (autoPreparationForLombokBuilderEnabled) {
            getClassAnnotationRules().addAll(CLASS_ANNOTATIONS_NECESSARY_FOR_LOMBOK_BUILDER);
            getImportRules().addAll(IMPORTS_NECESSARY_FOR_LOMBOK_BUILDER);
        }
        if (jsr305AnnotationsRequired) {
            if (useJakarta) {
                getImportRules().addAll(JAKARTA_ANNOTATION_PRESET_IMPORTS);
            } else {
                getImportRules().addAll(JSR_305_PRESET_IMPORTS);
            }
        }
    }

    private JDBCSettings jdbcSettings;

    private List<String> tableNames = new ArrayList<>();
    private String tableScanMode = "All"; // possible values: All, RuleBased

    private List<TableScanRule> tableScanRules = new ArrayList<>();
    private List<TableExclusionRule> tableExclusionRules = new ArrayList<>();

    // @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Possible values: TABLE, SEQUENCE, IDENTITY, AUTO
    // If you don't need to specify the `strategy`, set null value.
    private String generatedValueStrategy = "IDENTITY";

    private String outputDirectory = "src/main/java";
    private String packageName = "com.smartnews.db";
    private String packageNameForJpa1 = "com.smartnews.db.jpa1";
    private boolean jpa1SupportRequired;
    private boolean jsr305AnnotationsRequired;
    // Can be removed after no javax support and replace jsr305AnnotationsRequired to jakartaAnnotationRequired
    private boolean useJakarta;
    private boolean usePrimitiveForNonNullField;

    // NOTE: Explicitly having NoArgsConstructor/AllArgsConstructor is necessary as as a workaround to enable using @Builder
    // see also: https://github.com/rzwitserloot/lombok/issues/816
    private boolean autoPreparationForLombokBuilderEnabled;

    private List<ImportRule> importRules = new ArrayList<>();

    private List<ClassNameRule> classNameRules = new ArrayList<>();
    private List<ClassAnnotationRule> classAnnotationRules = new ArrayList<>();
    private List<InterfaceRule> interfaceRules = new ArrayList<>();
    private List<ClassAdditionalCommentRule> classAdditionalCommentRules = new ArrayList<>();

    private List<FieldTypeRule> fieldTypeRules = new ArrayList<>();
    private List<FieldAnnotationRule> fieldAnnotationRules = new ArrayList<>();
    private List<FieldDefaultValueRule> fieldDefaultValueRules = new ArrayList<>();
    private List<FieldAdditionalCommentRule> fieldAdditionalCommentRules = new ArrayList<>();

    private List<AdditionalCodeRule> additionalCodeRules = new ArrayList<>();

    private static final Yaml YAML = new Yaml();

    public static CodeGeneratorConfig load(String path) throws IOException {
        try (InputStream is = ResourceReader.getResourceAsStream(path)) {
            try (Reader reader = new InputStreamReader(is)) {
                CodeGeneratorConfig config = YAML.loadAs(reader, CodeGeneratorConfig.class);
                config.loadEnvVariables();
                config.setUpPresetRules();
                return config;
            }
        }
    }

    public JDBCSettings getJdbcSettings() {
        return jdbcSettings;
    }

    public void setJdbcSettings(JDBCSettings jdbcSettings) {
        this.jdbcSettings = jdbcSettings;
    }

    public List<String> getTableNames() {
        return tableNames;
    }

    public void setTableNames(List<String> tableNames) {
        this.tableNames = tableNames;
    }

    public String getTableScanMode() {
        return tableScanMode;
    }

    public void setTableScanMode(String tableScanMode) {
        this.tableScanMode = tableScanMode;
    }

    public List<TableScanRule> getTableScanRules() {
        return tableScanRules;
    }

    public void setTableScanRules(List<TableScanRule> tableScanRules) {
        this.tableScanRules = tableScanRules;
    }

    public List<TableExclusionRule> getTableExclusionRules() {
        return tableExclusionRules;
    }

    public void setTableExclusionRules(
            List<TableExclusionRule> tableExclusionRules) {
        this.tableExclusionRules = tableExclusionRules;
    }

    public String getGeneratedValueStrategy() {
        return generatedValueStrategy;
    }

    public void setGeneratedValueStrategy(String generatedValueStrategy) {
        this.generatedValueStrategy = generatedValueStrategy;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageNameForJpa1() {
        return packageNameForJpa1;
    }

    public void setPackageNameForJpa1(String packageNameForJpa1) {
        this.packageNameForJpa1 = packageNameForJpa1;
    }

    public boolean isJpa1SupportRequired() {
        return jpa1SupportRequired;
    }

    public void setJpa1SupportRequired(boolean jpa1SupportRequired) {
        this.jpa1SupportRequired = jpa1SupportRequired;
    }

    public boolean isJsr305AnnotationsRequired() {
        return jsr305AnnotationsRequired;
    }

    public void setJsr305AnnotationsRequired(boolean jsr305AnnotationsRequired) {
        this.jsr305AnnotationsRequired = jsr305AnnotationsRequired;
    }

    public boolean isUseJakarta() {
        return useJakarta;
    }

    public void setUseJakarta(boolean useJakarta) {
        this.useJakarta = useJakarta;
    }

    public boolean isUsePrimitiveForNonNullField() {
        return usePrimitiveForNonNullField;
    }

    public void setUsePrimitiveForNonNullField(boolean usePrimitiveForNonNullField) {
        this.usePrimitiveForNonNullField = usePrimitiveForNonNullField;
    }

    public boolean isAutoPreparationForLombokBuilderEnabled() {
        return autoPreparationForLombokBuilderEnabled;
    }

    public void setAutoPreparationForLombokBuilderEnabled(boolean autoPreparationForLombokBuilderEnabled) {
        this.autoPreparationForLombokBuilderEnabled = autoPreparationForLombokBuilderEnabled;
    }

    public List<ImportRule> getImportRules() {
        return importRules;
    }

    public void setImportRules(List<ImportRule> importRules) {
        this.importRules = importRules;
    }

    public List<ClassNameRule> getClassNameRules() {
        return classNameRules;
    }

    public void setClassNameRules(List<ClassNameRule> classNameRules) {
        this.classNameRules = classNameRules;
    }

    public List<ClassAnnotationRule> getClassAnnotationRules() {
        return classAnnotationRules;
    }

    public void setClassAnnotationRules(
            List<ClassAnnotationRule> classAnnotationRules) {
        this.classAnnotationRules = classAnnotationRules;
    }

    public List<InterfaceRule> getInterfaceRules() {
        return interfaceRules;
    }

    public void setInterfaceRules(List<InterfaceRule> interfaceRules) {
        this.interfaceRules = interfaceRules;
    }

    public List<ClassAdditionalCommentRule> getClassAdditionalCommentRules() {
        return classAdditionalCommentRules;
    }

    public void setClassAdditionalCommentRules(
            List<ClassAdditionalCommentRule> classAdditionalCommentRules) {
        this.classAdditionalCommentRules = classAdditionalCommentRules;
    }

    public List<FieldTypeRule> getFieldTypeRules() {
        return fieldTypeRules;
    }

    public void setFieldTypeRules(List<FieldTypeRule> fieldTypeRules) {
        this.fieldTypeRules = fieldTypeRules;
    }

    public List<FieldAnnotationRule> getFieldAnnotationRules() {
        return fieldAnnotationRules;
    }

    public void setFieldAnnotationRules(
            List<FieldAnnotationRule> fieldAnnotationRules) {
        this.fieldAnnotationRules = fieldAnnotationRules;
    }

    public List<FieldDefaultValueRule> getFieldDefaultValueRules() {
        return fieldDefaultValueRules;
    }

    public void setFieldDefaultValueRules(
            List<FieldDefaultValueRule> fieldDefaultValueRules) {
        this.fieldDefaultValueRules = fieldDefaultValueRules;
    }

    public List<FieldAdditionalCommentRule> getFieldAdditionalCommentRules() {
        return fieldAdditionalCommentRules;
    }

    public void setFieldAdditionalCommentRules(
            List<FieldAdditionalCommentRule> fieldAdditionalCommentRules) {
        this.fieldAdditionalCommentRules = fieldAdditionalCommentRules;
    }

    public List<AdditionalCodeRule> getAdditionalCodeRules() {
        return additionalCodeRules;
    }

    public void setAdditionalCodeRules(
            List<AdditionalCodeRule> additionalCodeRules) {
        this.additionalCodeRules = additionalCodeRules;
    }
}
