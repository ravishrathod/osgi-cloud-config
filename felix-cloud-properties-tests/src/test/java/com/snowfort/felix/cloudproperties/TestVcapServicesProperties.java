package com.snowfort.felix.cloudproperties;

import org.junit.Assert;
import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.options.MavenUrlReference;

import static org.hamcrest.CoreMatchers.is;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;

/**
 * Unit test for simple App.
 */
public class TestVcapServicesProperties {

    @Configuration
    public Option[] config() {
        MavenArtifactUrlReference karafUrl = maven()
                .groupId("org.apache.karaf")
                .artifactId("apache-karaf")
                .version("3.0.0")
                .type("tar.gz");
        MavenUrlReference karafStandardRepo = maven()
                .groupId("org.apache.karaf.features")
                .artifactId("standard")
                .classifier("features")
                .type("xml")
                .versionAsInProject();

        return new Option[] {
                // KarafDistributionOption.debugConfiguration("5005", true),
                karafDistributionConfiguration()
                        .frameworkUrl(karafUrl)
                        .unpackDirectory(new File("target/exam"))
                        .useDeployFolder(false),
                keepRuntimeFolder(),
                features(karafStandardRepo , "scr"),
                mavenBundle()
                        .groupId("org.ops4j.pax.exam.samples")
                        .artifactId("pax-exam-sample8-ds")
                        .versionAsInProject().start(),
        };
    }

    @Test
    public void testVar() {
        Assert.assertThat(true, is(true));
    }

}
