package com.snowfort.osgi.cloud.config.vcap;

import org.osgi.service.cm.ConfigurationAdmin;

/**
 * Created by ravish.rathod on 20/03/17.
 */
public class VCapServicesLoader extends EnvJsonPropertiesLoader {

    public VCapServicesLoader(String pid, ConfigurationAdmin configurationAdmin) throws Exception {
        super("VCAP_SERVICES", pid, configurationAdmin);
        init();
    }
}
