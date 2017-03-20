package com.snowfort.osgi.cloud.config.vcap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class EnvJsonPropertiesLoader {

    private static final Logger logger = LoggerFactory.getLogger(EnvJsonPropertiesLoader.class);

    private String environmentVariable;
    private String pid;
    private ConfigurationAdmin configAdmin;


    public EnvJsonPropertiesLoader(String environmentVariable, String pid, ConfigurationAdmin configAdmin) {
        this.environmentVariable = environmentVariable;
        this.pid = pid;
        this.configAdmin = configAdmin;
    }

    public EnvJsonPropertiesLoader() {
    }

    public Dictionary<String, Object> init() throws Exception {
        String envJsonStr = getEnvVariableValue();
        if (null != envJsonStr) {
            logger.info("Initializing configuration with environmentVariable: {} for pid: {} ", envJsonStr, pid);

            // Set up a configuration scoped PID=<pid>.  This allows users to grab these values in their blueprints
            // by setting up a <properties-placeholder persistent-id="..."> to pull out the relevant values and set
            // defaults if they aren't available in the environment variable JSON.
            logger.info("Getting existing configuration for PID '"+ pid +"'");

            Configuration config = configAdmin.getConfiguration(pid);
            Dictionary<String, Object> configProps = config.getProperties();
            if (configProps == null) {
                configProps = new Hashtable<>();
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
            return configProps;
        }

        return null;
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
            List<String> arrayValues = new ArrayList<>();
            for (int i=0; i < array.size(); i++) {
                String childElementPath = elementPath+"_"+i;
                if (elementPath.trim().isEmpty()) {
                    childElementPath = "root_"+i;
                }
                if(array.get(i).isJsonPrimitive()){
                    arrayValues.add(array.get(i).getAsJsonPrimitive().getAsString());
                }
                addJsonPropertiesAsConfig(configProps, childElementPath, array.get(i));
            }
            if(! arrayValues.isEmpty()) {
                final String delimitedValue = arrayValues.stream().map(value -> value).collect(Collectors.joining(","));
                configProps.put(elementPath, delimitedValue);
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

    String getEnvVariableValue() {
        return System.getenv(environmentVariable);
    }
}
