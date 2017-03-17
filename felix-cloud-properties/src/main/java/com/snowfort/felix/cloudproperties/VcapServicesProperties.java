package com.snowfort.felix.cloudproperties;


import com.jayway.jsonpath.JsonPath;

public class VcapServicesProperties {

    public String var(String path) {
        String val = null;
        String vcapServicesJson = System.getenv("VCAP_SERVICES");
        if (null != vcapServicesJson) {
            val = JsonPath.read(vcapServicesJson,"$."+path).toString();
        }
        return val;
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage:  VcapServicesProperties <path>");
            System.exit(1);
        }

        VcapServicesProperties vcap = new VcapServicesProperties();
        System.out.println("VALUE:  " + vcap.var(args[0]));
    }

}
