package ${packageName};

<#list importRules as rule>
import ${rule.importValue};
</#list>

<#if classComment?has_content>
${classComment}
</#if>
<#list classAnnotationRules as rule>
<#list rule.annotations as annotation>
${annotation.toString()}
</#list>
</#list>
<#-- NOTICE: the name attribute of @Table is intentionally unquoted  -->
@Table(name = "${tableName}")<#if primaryKeyFields.size() \gt 1>
    @IdClass(${className}.PrimaryKeys.class)</#if>
public class ${className}<#if interfaceNames.size() \gt 0> implements ${interfaceNames?join(", ")}</#if><#if classNames.size() \gt 0> extends ${classNames?join(", ")}</#if> {
<#if primaryKeyFields.size() \gt 1>
    public static class PrimaryKeys implements Serializable {
    <#list primaryKeyFields as field>
        private ${field.type} ${field.name}<#if field.defaultValue??> = ${field.defaultValue}</#if>;
    </#list>
    }
</#if>

<#list topAdditionalCodeList as code>
    ${code}
</#list>
<#list fields as field>
    <#if field.primaryKey>
    <#if interfaceNames.size() \gt 0>
    <#if field.comment?has_content>${field.comment}</#if>
    <#if field.primaryKey>
    @Id
    </#if>
    <#if field.autoIncrement>
    <#if field.generatedValueStrategy?has_content>
    @GeneratedValue(strategy = GenerationType.${field.generatedValueStrategy})
    <#else>
    @GeneratedValue
    </#if>
    </#if>
    <#list field.annotations as annotation>
    ${annotation.toString()}
    </#list>
    <#if requireJSR305 && !field.primitive>
    <#if field.nullable>@Nullable<#else>@Nonnull</#if>
    </#if>
<#--  @Column(name = "<#if jpa1Compatible>`<#else>\"</#if>${field.columnName}<#if jpa1Compatible>`<#else>\"</#if>", nullable = ${field.nullable?c})-->
    @Column(name = "${field.columnName}", nullable = ${field.nullable?c})
    private ${field.type} ${field.name}<#if field.defaultValue??> = ${field.defaultValue}</#if>;
    </#if>
    <#else>
    <#if field.comment?has_content>${field.comment}</#if>
    <#if field.autoIncrement>
    <#if field.generatedValueStrategy?has_content>
    @GeneratedValue(strategy = GenerationType.${field.generatedValueStrategy})
    <#else>
    @GeneratedValue
    </#if>
    </#if>
    <#list field.annotations as annotation>
    ${annotation.toString()}
    </#list>
    <#if requireJSR305 && !field.primitive>
    <#if field.nullable>@Nullable<#else>@Nonnull</#if>
    </#if>
<#--  @Column(name = "<#if jpa1Compatible>`<#else>\"</#if>${field.columnName}<#if jpa1Compatible>`<#else>\"</#if>", nullable = ${field.nullable?c})-->
    @Column(name = "${field.columnName}", nullable = ${field.nullable?c})
    private ${field.type} ${field.name}<#if field.defaultValue??> = ${field.defaultValue}</#if>;
    </#if>
</#list>

    /* ---------------------------------------- getter setter ---------------------------------------- */

<#list fields as field>
    public void set${field.name?cap_first} (${field.type} ${field.name}) {
        this.${field.name} = ${field.name};
    }

    public ${field.type} get${field.name?cap_first} () {
        return ${field.name};
    }
</#list>
<#list bottomAdditionalCodeList as code>
    ${code}
</#list>
}
