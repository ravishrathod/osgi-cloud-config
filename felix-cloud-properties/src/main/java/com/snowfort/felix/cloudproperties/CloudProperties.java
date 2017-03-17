package com.snowfort.felix.cloudproperties;

import org.springframework.cloud.Cloud;
import org.springframework.cloud.CloudFactory;
import org.springframework.cloud.service.ServiceInfo;
import org.springframework.cloud.service.common.MongoServiceInfo;

import java.util.List;

public class CloudProperties {

    private List<ServiceInfo> services;

    public static void main(String[] args) {
        CloudFactory cloudFactory = new CloudFactory();
        Cloud cloud = cloudFactory.getCloud();

        if(cloud == null) {
            System.out.println("No cloud");
        } else {
            for (ServiceInfo serviceInfo : cloud.getServiceInfos()) {
                System.out.println((MongoServiceInfo)serviceInfo);
            }
        }
    }

}
