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

public class VcapServicesProperties {

    private static final Logger logger = LoggerFactory.getLogger(VcapServicesProperties.class);

    private ConfigurationAdmin configAdmin;


    public ConfigurationAdmin getConfigurationAdmin() {
        return configAdmin;
    }

    public void setConfigurationAdmin(ConfigurationAdmin configAdmin) {
        this.configAdmin = configAdmin;
    }

    public void init() throws Exception {
        String vcapServicesJson = System.getenv("VCAP_SERVICES");
        if (null != vcapServicesJson) {
            // load up the config admin with all VCAP_SERVICES properties
            logger.info("Initializing configuration with VCAP_SERVICES:  " + vcapServicesJson);

            Configuration config = configAdmin.getConfiguration("vcap.services");
            Dictionary<String, Object> configProps = config.getProperties();

            if (configProps == null) {
                configProps = new Hashtable<String, Object>();
            }

            // iterate over all VCAP_SERVICES properties and if it's a JsonPrimitive (leaf node),
            // create a flattened vcap.services.* config property based on the value.
            JsonElement vcapServices = new JsonParser().parse(vcapServicesJson);
            addJsonPropertiesAsConfig(configProps, "vcap.services", vcapServices);

            Enumeration<String> keys = configProps.keys();
            while(keys.hasMoreElements()){
                String key = keys.nextElement();
                logger.info("KEY:  " + key + ",   VALUE:  " + configProps.get(key));
            }



//            String jdbcUrl = var("cleardb[0].credentials.jdbcUrl");
//            logger.info("Setting vcap.services.mysql.jdbcUrl to " + jdbcUrl);
//            configProps.put("mysql.jdbcUrl", jdbcUrl);
//
//            String username = var("cleardb[0].credentials.username");
//            logger.info("Setting vcap.services.mysql.username to " + username);
//            configProps.put("mysql.username", username);
//
//            String password = var("cleardb[0].credentials.password");
//            logger.info("Setting vcap.services.mysql.password to " + password);
//            configProps.put("mysql.password", password);
//
//            config.update(configProps);
//            logger.info("config:  " + config.toString());
        }
    }

    private void addJsonPropertiesAsConfig(Dictionary<String, Object> configProps, String elementPath, JsonElement element) {
        if (element.isJsonPrimitive()) {
            configProps.put(elementPath, element.getAsJsonPrimitive().getAsString());
        } else if (element.isJsonObject()) {
            element.getAsJsonObject().entrySet().stream().forEach(entry -> {
                addJsonPropertiesAsConfig(configProps, elementPath+"."+entry.getKey(), entry.getValue());
            });
        } else if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            for (int i=0; i < array.size(); i++) {
                addJsonPropertiesAsConfig(configProps, elementPath+"["+i+"]", array.get(i));
            }
        }
    }

}
