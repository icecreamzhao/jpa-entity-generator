package com.smartnews.jpa_entity_generator;

import com.smartnews.jpa_entity_generator.config.CodeGeneratorConfig;
import com.smartnews.jpa_entity_generator.metadata.Column;
import com.smartnews.jpa_entity_generator.metadata.Table;
import com.smartnews.jpa_entity_generator.metadata.TableMetadataFetcher;
import com.smartnews.jpa_entity_generator.rule.*;
import com.smartnews.jpa_entity_generator.util.NameConverter;
import com.smartnews.jpa_entity_generator.util.TypeConverter;
import freemarker.template.TemplateException;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 * Lombok-wired JPA entity code generator.
 */
public class CodeGenerator {
	private static final Logger       log                                        = LoggerFactory.getLogger(CodeGenerator.class);
	private static final List<String> EXPECTED_ID_ANNOTATION_CLASS_NAMES         = Arrays.asList("Id", "javax.persistence.Id");
	private static final List<String> EXPECTED_ID_JAKARTA_ANNOTATION_CLASS_NAMES = Arrays.asList("Id", "jakarta.persistence.Id");

	private static final Predicate<CodeRenderer.RenderingData.Field> hasIdAnnotation = (f) -> {
		boolean isPrimaryKey = f.isPrimaryKey();
		boolean hasIdAnnotation = f.getAnnotations().stream().anyMatch(a -> EXPECTED_ID_ANNOTATION_CLASS_NAMES.contains(a.getClassName()));
		return isPrimaryKey || hasIdAnnotation;
	};

	private static final Predicate<CodeRenderer.RenderingData.Field> hasJakartaIdAnnotation = (f) -> {
		boolean isPrimaryKey = f.isPrimaryKey();
		boolean hasIdAnnotation = f.getAnnotations().stream().anyMatch(a -> EXPECTED_ID_JAKARTA_ANNOTATION_CLASS_NAMES.contains(a.getClassName()));
		return isPrimaryKey || hasIdAnnotation;
	};

	public static void generateAll(CodeGeneratorConfig originalConfig) throws SQLException, IOException,
			TemplateException {
		generateAll(originalConfig, false);
	}

	/**
	 * Generates all entities from existing tables.
	 */
	public static void generateAll(CodeGeneratorConfig originalConfig, boolean isJpa1) throws SQLException, IOException,
			TemplateException {
		Path dir = Paths.get(originalConfig.getOutputDirectory() + "/" + (isJpa1 ?
				originalConfig.getPackageNameForJpa1().replaceAll("\\.", "/") :
				originalConfig.getPackageName().replaceAll("\\.", "/")));
		Files.createDirectories(dir);

		TableMetadataFetcher metadataFetcher = new TableMetadataFetcher();
		List<String>         allTableNames   = metadataFetcher.getTableNames(originalConfig.getJdbcSettings());
		List<String>         tableNames      = filterTableNames(originalConfig, allTableNames);
		for (String tableName : tableNames) {
			boolean shouldExclude = originalConfig.getTableExclusionRules().stream().anyMatch(rule -> rule.matches(tableName));
			if (shouldExclude) {
				log.debug("Skipped to generate entity for {}", tableName);
				continue;
			}
			CodeGeneratorConfig config = SerializationUtils.clone(originalConfig);
			Table               table  = metadataFetcher.getTable(config.getJdbcSettings(), tableName);

			CodeRenderer.RenderingData data = new CodeRenderer.RenderingData();
			data.setJpa1Compatible(isJpa1);
			data.setRequireJSR305(config.isJsr305AnnotationsRequired());
			data.setUseJakarta(config.isUseJakarta());

			if (isJpa1) {
				data.setPackageName(config.getPackageNameForJpa1());
			} else {
				data.setPackageName(config.getPackageName());
			}

			String className = NameConverter.toClassName(table.getName(), config.getClassNameRules());
			data.setClassName(className);
			data.setTableName(table.getName());

			ClassAnnotationRule entityClassAnnotationRule = new ClassAnnotationRule();
			String              entityClassName           =
					data.isUseJakarta() ? "jakarta.persistence.Entity" : "javax.persistence.Entity";
			Annotation          entityAnnotation          = Annotation.fromClassName(entityClassName);
			AnnotationAttribute entityAnnotationValueAttr = new AnnotationAttribute();
			entityAnnotationValueAttr.setName("name");
//            entityAnnotationValueAttr.setValue("\"" + data.getPackageName() + "." + data.getClassName() + "\"");
			entityAnnotationValueAttr.setValue("\"" + data.getClassName() + "\"");
			entityAnnotation.getAttributes().add(entityAnnotationValueAttr);
			entityClassAnnotationRule.setAnnotations(List.of(entityAnnotation));
			entityClassAnnotationRule.setClassName(className);
			config.getClassAnnotationRules().add(entityClassAnnotationRule);

			data.setClassComment(buildClassComment(className, table, config.getClassAdditionalCommentRules()));

			data.setImportRules(config.getImportRules().stream().filter(r -> r.matches(className)).collect(toList()));

			List<CodeRenderer.RenderingData.Field> fields = table.getColumns().stream().map(c -> {
				CodeRenderer.RenderingData.Field f = new CodeRenderer.RenderingData.Field();

				String fieldName = NameConverter.toFieldName(c.getName());
				f.setName(fieldName);
				f.setColumnName(c.getName());
				f.setNullable(c.isNullable());

				f.setComment(buildFieldComment(className, f.getName(), c, config.getFieldAdditionalCommentRules()));

				f.setAnnotations(config.getFieldAnnotationRules().stream().filter(rule -> rule.matches(className, f.getName())).flatMap(rule -> rule.getAnnotations().stream()).peek(a -> a.setClassName(collectAndConvertFQDN(a.getClassName(), data.getImportRules()))).collect(toList()));

				Optional<FieldTypeRule> fieldTypeRule = orEmptyListIfNull(config.getFieldTypeRules()).stream().filter(b -> b.matches(className, fieldName)).findFirst();
				if (fieldTypeRule.isPresent()) {
					f.setType(fieldTypeRule.get().getTypeName());
					f.setPrimitive(isPrimitive(f.getType()));
				} else {
					f.setType(TypeConverter.toJavaType(c.getTypeCode()));
					if (!c.isNullable() && config.isUsePrimitiveForNonNullField()) {
						f.setType(TypeConverter.toPrimitiveTypeIfPossible(f.getType()));
					}
					f.setPrimitive(isPrimitive(f.getType()));
				}

				Optional<FieldDefaultValueRule> fieldDefaultValueRule = orEmptyListIfNull(config.getFieldDefaultValueRules()).stream().filter(r -> r.matches(className, fieldName)).findFirst();
				fieldDefaultValueRule.ifPresent(defaultValueRule -> f.setDefaultValue(defaultValueRule.getDefaultValue()));
				if (StringUtils.isNotEmpty(config.getGeneratedValueStrategy())) {
					f.setGeneratedValueStrategy(config.getGeneratedValueStrategy());
				}

				f.setAutoIncrement(c.isAutoIncrement());
				f.setPrimaryKey(c.isPrimaryKey());
				return f;

			}).collect(toList());

			Predicate<CodeRenderer.RenderingData.Field> fieldPredicate =
					data.isUseJakarta() ? hasJakartaIdAnnotation : hasIdAnnotation;
			if (fields.stream().noneMatch(fieldPredicate)) {
				throw new IllegalStateException("Entity class " + data.getClassName() + " has no @Id field!");
			}

			boolean shouldHaveId = true;
			for (InterfaceRule interfaceRule : config.getInterfaceRules()) {
				if (!interfaceRule.getClasses().isEmpty()) {
					shouldHaveId = false;
					break;
				}
			}

			if (!shouldHaveId) fields.removeAll(fields.stream().filter(field -> field.getColumnName().equals("id")).toList());

			data.setFields(fields);
			data.setPrimaryKeyFields(fields.stream().filter(CodeRenderer.RenderingData.Field::isPrimaryKey).collect(toList()));

			data.setInterfaceNames(orEmptyListIfNull(config.getInterfaceRules()).stream().filter(r -> r.matches(className)).peek(rule -> {
				for (Interface i : rule.getInterfaces()) {
					i.setName(collectAndConvertFQDN(i.getName(), data.getImportRules()));
					i.setGenericsClassNames(i.getGenericsClassNames().stream().map(cn -> collectAndConvertFQDN(cn, data.getImportRules())).collect(toList()));
				}
			}).flatMap(r -> r.getInterfaces().stream().map(i -> {
				String genericsPart = !i.getGenericsClassNames().isEmpty() ?
						i.getGenericsClassNames().stream().map(n -> n.equals("{className}") ? className :
								n).collect(Collectors.joining(", ", "<", ">")) : "";
				return i.getName() + genericsPart;
			})).collect(toList()));
			data.setClassNames(orEmptyListIfNull(config.getInterfaceRules()).stream().filter(r -> r.matches(className)).peek(rule -> {
				for (Classes i : rule.getClasses()) {
					i.setName(collectAndConvertFQDN(i.getName(), data.getImportRules()));
					i.setGenericsClassNames(i.getGenericsClassNames().stream().map(cn -> collectAndConvertFQDN(cn, data.getImportRules())).collect(toList()));
				}
			}).flatMap(r -> r.getClasses().stream().map(i -> {
				String genericsPart = !i.getGenericsClassNames().isEmpty() ?
						i.getGenericsClassNames().stream().map(n -> n.equals("{className}") ? className :
								n).collect(Collectors.joining(", ", "<", ">")) : "";
				return i.getName() + genericsPart;
			})).collect(toList()));

			data.setClassAnnotationRules(orEmptyListIfNull(config.getClassAnnotationRules()).stream().filter(r -> r.matches(className)).peek(rule -> rule.getAnnotations().forEach(a -> {
				a.setClassName(collectAndConvertFQDN(a.getClassName(), data.getImportRules()));
			})).collect(toList()));

			orEmptyListIfNull(config.getAdditionalCodeRules()).forEach(rule -> {
				if (rule.matches(className)) {
					String code = null;
					if (isJpa1 && rule.getJpa1Code() != null) {
						code = rule.getJpa1Code();
					} else if (rule.getCode() != null) {
						code = rule.getCode();
					}

					if (code != null) {
						StringJoiner joiner = new StringJoiner("\n  ", "  ", "");
						for (String line : code.split("\\n")) {
							joiner.add(line);
						}
						String optimizedCode = joiner.toString();
						if (rule.getPosition() == AdditionalCodePosition.Top) {
							data.getTopAdditionalCodeList().add(optimizedCode);
						} else {
							data.getBottomAdditionalCodeList().add(optimizedCode);
						}
					}
				}
			});

			orEmptyListIfNull(data.getImportRules()).sort(Comparator.comparing(ImportRule::getImportValue));

			String code = CodeRenderer.render("entityGen/entity.ftl", data);

			String filepath = config.getOutputDirectory() + "/" + data.getPackageName().replaceAll("\\.", "/") + "/" + className + ".java";
			Path   path     = Paths.get(filepath);
			if (!Files.exists(path)) {
				Files.createFile(path);
			}
			Files.write(path, code.getBytes());

			log.debug("path: {}, code: {}", path, code);
		}
	}

	private static List<String> filterTableNames(CodeGeneratorConfig config, List<String> allTableNames) {
		String tableScanMode = config.getTableScanMode();
		if (tableScanMode == null) {
			return allTableNames;
		}
		String normalizedTableScanMode = tableScanMode.trim().toLowerCase(Locale.ENGLISH);
		if (normalizedTableScanMode.equals("all")) {
			return allTableNames;
		} else if (normalizedTableScanMode.equals("rulebased")) {
			List<String> filteredTableNames = new ArrayList<>();
			for (String tableName : allTableNames) {
				boolean isScanTarget = true;
				for (TableScanRule rule : config.getTableScanRules()) {
					if (!rule.matches(tableName)) {
						isScanTarget = false;
						break;
					}
				}
				if (isScanTarget) {
					filteredTableNames.add(tableName);
				}
			}
			return filteredTableNames;
		} else {
			throw new IllegalStateException("Invalid value (" + tableScanMode + ") is specified for tableScanName");
		}
	}

	private static String buildClassComment(String className, Table table, List<ClassAdditionalCommentRule> rules) {
		List<String> comment = new ArrayList<>(table.getDescription().map(c -> Arrays.stream(c.split("\n")).filter(l -> l != null && !l.isEmpty()).collect(toList())).orElse(Collections.emptyList()));
		List<String> additionalComments = rules.stream().filter(r -> r.matches(className)).map(ClassAdditionalCommentRule::getComment).flatMap(c -> Arrays.stream(c.split("\n"))).toList();
		comment.addAll(additionalComments);
		if (!comment.isEmpty()) {
			return comment.stream().collect(joining("\n * ", "/**\n * ", "\n */"));
		} else {
			return null;
		}
	}

	private static String buildFieldComment(
			String className, String fieldName, Column column, List<FieldAdditionalCommentRule> rules) {
		List<String> comment = new ArrayList<>(column.getDescription().map(c -> Arrays.stream(c.split("\n")).filter(l -> l != null && !l.isEmpty()).collect(toList())).orElse(Collections.emptyList()));
		List<String> additionalComments = rules.stream().filter(r -> r.matches(className, fieldName)).map(FieldAdditionalCommentRule::getComment).flatMap(c -> Arrays.stream(c.split("\n"))).toList();
		comment.addAll(additionalComments);
		if (!comment.isEmpty()) {
			return comment.stream().collect(joining("\n     * ", "/**\n     * ", "\n     */"));
		} else {
			return null;
		}
	}

	private static <T> List<T> orEmptyListIfNull(List<T> list) {
		return Optional.ofNullable(list).orElse(Collections.emptyList());
	}

	private static String collectAndConvertFQDN(String fqdn, List<ImportRule> imports) {
		if (fqdn != null && fqdn.contains(".") && fqdn.matches("^[a-zA-Z0-9.]+$")) {
			if (imports.stream().noneMatch(i -> i.importValueContains(fqdn))) {
				ImportRule rule = new ImportRule();
				rule.setImportValue(fqdn);
				imports.add(rule);
			}
			String[] elements = fqdn.split("\\.");
			return elements[elements.length - 1];
		} else {
			return fqdn;
		}
	}

	private static boolean isPrimitive(String type) {
		if (type == null) {
			return false;
		}
		if (type.contains(".")) {
			return false;
		}
		return Character.isLowerCase(type.charAt(0));
	}
}
