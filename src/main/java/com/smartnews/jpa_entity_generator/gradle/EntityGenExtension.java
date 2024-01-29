package com.smartnews.jpa_entity_generator.gradle;

/**
 * <pre>
 * entityGen {
 *   configPath = "src/main/resources/entityGenConfig.yml"
 * }
 * </pre>
 */
public class EntityGenExtension {

    private String configPath = "entityGenConfig.yml";

    public String getConfigPath() {
        return configPath;
    }

    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }
}
