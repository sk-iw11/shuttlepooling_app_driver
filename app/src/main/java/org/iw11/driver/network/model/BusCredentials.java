package org.iw11.driver.network.model;

public class BusCredentials {

    private String name;
    private String password;

    public BusCredentials() { }

    public BusCredentials(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }
}
