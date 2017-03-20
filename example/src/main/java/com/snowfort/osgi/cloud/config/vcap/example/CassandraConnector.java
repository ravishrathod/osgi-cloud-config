package com.snowfort.osgi.cloud.config.vcap.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ravish.rathod on 20/03/17.
 */
public class CassandraConnector {
    public static final Logger LOGGER = LoggerFactory.getLogger(CassandraConnector.class);

    public CassandraConnector(String connectionPoints, String username, String password, String datacenter) {
        LOGGER.info("ContactPoints: {} Username: {} Password: {} Datecenter: {}", connectionPoints, username, password, datacenter);

    }
}
