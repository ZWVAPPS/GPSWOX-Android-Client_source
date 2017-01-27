package com.gpswox.android.models;


import java.util.ArrayList;

/**
 * Created by gintas on 06/10/15.
 */
public class Device
{
    public int id;
    public String name;
    public String online;
    public String time;
    public int speed;
    public float lat, lng;
    public String course;
    public String power;
    public int altitude;
    public String address;
    public String protocol;
    public long timestamp;
    public ArrayList<Sensor> sensors;
    public ArrayList<Service> services;
    public Driver driver_data;
    public String distance_unit_hour;
    public String unit_of_distance;
    public String unit_of_altitude;
    public String unit_of_capacity;
    public ArrayList<TailItem> tail;

    public DeviceData device_data;

    public Device()
    {
        name = "";
        //time = "";
        //course = "";
        //power = "";
        //address = "";
        protocol = "";
        sensors = new ArrayList<>();
        services = new ArrayList<>();
        driver_data = new Driver();
        device_data = new DeviceData();
        tail = new ArrayList<>();
    }

    public Device(Device another)
    {
        this.id = another.id;
        this.name = another.name;
        this.time = another.time;
        this.speed = another.speed;
        this.lat = another.lat;
        this.lng = another.lng;
        this.course = another.course;
        this.power = another.power;
        this.altitude = another.altitude;
        this.address = another.address;
        this.protocol = another.protocol;
        this.timestamp = another.timestamp;
        this.power = another.power;
        this.online = another.online;
        this.sensors = new ArrayList<>();
        for(Sensor sensor : another.sensors)
            this.sensors.add(new Sensor(sensor));
        this.services = new ArrayList<>();
        for(Service service : another.services)
            this.services.add(new Service(service));
        this.driver_data = new Driver(another.driver_data);
        this.device_data = new DeviceData(another.device_data);
        this.unit_of_distance = another.unit_of_distance;
        this.unit_of_altitude = another.unit_of_altitude;
        this.unit_of_capacity = another.unit_of_capacity;
        this.distance_unit_hour = another.distance_unit_hour;
        this.tail = new ArrayList<>();
        for(TailItem item : another.tail)
            this.tail.add(item);
    }

    @Override
    public String toString() {
        return device_data.name;
    }

    public boolean fitForFilter(String filterString) {
        if(name != null && name.toLowerCase().contains(filterString.toLowerCase()))
            return true;
        if(time != null && time.toLowerCase().contains(filterString.toLowerCase()))
            return true;
        if(address != null && address.toLowerCase().contains(filterString.toLowerCase()))
            return true;
        if(protocol != null && protocol.toLowerCase().contains(filterString.toLowerCase()))
            return true;
        if(driver_data != null && driver_data.name != null &&driver_data.name.toLowerCase().contains(filterString.toLowerCase()))
            return true;
        if(device_data.object_owner != null && device_data.object_owner.toLowerCase().contains(filterString.toLowerCase()))
            return true;
        if(device_data.registration_number != null && device_data.registration_number.toLowerCase().contains(filterString.toLowerCase()))
            return true;
        if(device_data.plate_number != null && device_data.plate_number.toLowerCase().contains(filterString.toLowerCase()))
            return true;
        if(device_data.sim_number != null && device_data.sim_number.toLowerCase().contains(filterString.toLowerCase()))
            return true;
        return false;
    }
}