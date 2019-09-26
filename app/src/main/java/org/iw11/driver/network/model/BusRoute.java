package org.iw11.driver.network.model;


import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class BusRoute {

    private List<BusStation> stations;

    public BusRoute(List<BusStation> stations) {
        this.stations = Collections.unmodifiableList(stations);
    }

    public List<BusStation> getStations() {
        return stations;
    }
}
