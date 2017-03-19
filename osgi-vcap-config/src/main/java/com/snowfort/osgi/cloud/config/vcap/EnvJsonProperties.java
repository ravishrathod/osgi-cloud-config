package com.snowfort.osgi.cloud.config.vcap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

public class EnvJsonProperties {

    private static final Logger logger = LoggerFactory.getLogger(EnvJsonProperties.class);

    private String environmentVariable;
    private String prefix;
    private ConfigurationAdmin configAdmin;


    public EnvJsonProperties(String environmentVariable, String prefix) {
        this.environmentVariable = environmentVariable;
        this.prefix = prefix;
    }

    public ConfigurationAdmin getConfigurationAdmin() {
        return configAdmin;
    }

    public void setConfigurationAdmin(ConfigurationAdmin configAdmin) {
        this.configAdmin = configAdmin;
    }

    public void init() throws Exception {
        String envJsonStr = System.getenv(environmentVariable);
        if (null != envJsonStr) {
            logger.info("Initializing configuration with environmentVariable:  " + envJsonStr);

            // Set up a configuration scoped PID=<prefix>.  This allows users to grab these values in their blueprints
            // by setting up a <properties-placeholder persistent-id="..."> to pull out the relevant values and set
            // defaults if they aren't available in the environment variable JSON.
            logger.info("Getting existing configuration for PID '"+prefix+"'");
            Configuration config = configAdmin.getConfiguration(prefix);
            Dictionary<String, Object> configProps = config.getProperties();
            if (configProps == null) {
                configProps = new Hashtable<String, Object>();
            }

            // iterate over all JSON properties in environmentVariable and if it's a JsonPrimitive (leaf node),
            // create a flattened vcap.services.* config property based on the value.
            JsonElement envJson = new JsonParser().parse(envJsonStr);
            addJsonPropertiesAsConfig(configProps, "", envJson);

            outputConfig(configProps);

            config.update(configProps);
            logger.info("config:  " + config.toString());

            // Output the loaded variables to the log for debugging.
            outputConfig(configProps);
        }
    }

    private void addJsonPropertiesAsConfig(Dictionary<String, Object> configProps, String elementPath, JsonElement element) {
        if (element.isJsonPrimitive()) {
            logger.info("Added config KEY: {},  VALUE: {}", elementPath, element.getAsJsonPrimitive().getAsString());
            configProps.put(elementPath, element.getAsJsonPrimitive().getAsString());
        } else if (element.isJsonObject()) {
            element.getAsJsonObject().entrySet().stream().forEach(entry -> {
                String childElementPath = elementPath+"."+entry.getKey();
                if (elementPath.trim().isEmpty()) {
                    childElementPath = entry.getKey();
                }
                addJsonPropertiesAsConfig(configProps, childElementPath, entry.getValue());
            });
        } else if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            for (int i=0; i < array.size(); i++) {
                String childElementPath = elementPath+"_"+i;
                if (elementPath.trim().isEmpty()) {
                    childElementPath = "root_"+i;
                }
                addJsonPropertiesAsConfig(configProps, childElementPath, array.get(i));
            }
        }
    }

    private void outputConfig(Dictionary<String, Object> configProps) {
        Enumeration<String> keys = configProps.keys();
        while(keys.hasMoreElements()){
            String key = keys.nextElement();
            logger.info("KEY:  " + key + ",   VALUE:  " + configProps.get(key));
        }
    }

}
