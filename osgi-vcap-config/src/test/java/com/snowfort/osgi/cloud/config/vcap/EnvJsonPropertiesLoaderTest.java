package com.snowfort.osgi.cloud.config.vcap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.util.Dictionary;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EnvJsonPropertiesLoaderTest {

    @Mock private ConfigurationAdmin configurationAdmin;
    @Mock private Configuration configuration;
    private EnvJsonPropertiesLoader loader;

    @Before
    public void setup() throws IOException {
        String envValue = "{ \"cassandra\": [ { \"credentials\": { \"keyspace\": \"9ad9304c8c97457894db82febef359ac\", \"password\": \"password-1\", \"cqlsh_port\": \"9042\", \"node_ips\": [\"localhost\", \"127.0.0.1\"], \"username\": \"user-1\" }, \"label\": \"cassandra\", \"name\": \"cengage-cassandra-test\", \"plan\": \"cassandra-shared\", \"provider\": null, \"syslog_drain_url\": null, \"tags\": [ \"cassandra\", \"nosql\" ], \"volume_mounts\": [] } ] }";
        when(configurationAdmin.getConfiguration("test-pid")).thenReturn(configuration);

        loader = new EnvJsonPropertiesLoader("VCAP_SERVICES", "test-pid", configurationAdmin) {
            @Override
            String getEnvVariableValue() {
                return envValue;
            }
        };
    }

    @Test
    public void parses_vCapToProperties() throws Exception {
        final Dictionary<String, Object> dictionary = loader.init();
        assertEquals("9ad9304c8c97457894db82febef359ac", dictionary.get("cassandra_0.credentials.keyspace"));
        assertEquals("localhost", dictionary.get("cassandra_0.credentials.node_ips_0"));
        assertEquals("127.0.0.1", dictionary.get("cassandra_0.credentials.node_ips_1"));
        assertEquals("localhost,127.0.0.1", dictionary.get("cassandra_0.credentials.node_ips"));
        assertEquals("cassandra,nosql", dictionary.get("cassandra_0.tags"));
        assertNull(dictionary.get("cassandra"));
    }
}