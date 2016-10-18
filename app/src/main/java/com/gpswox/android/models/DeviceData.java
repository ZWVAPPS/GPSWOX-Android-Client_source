package com.gpswox.android.models;

/**
 * Created by gintas on 12/11/15.
 */
public class DeviceData {
    public int id, user_id, traccar_device_id, icon_id, fuel_measurement_id, tail_length;
    public int group_id, timezone_id;
    public int active, deleted;

    public String name, imei, fuel_quantity, fuel_price, fuel_per_km, sim_number, expiration_date, current_geofences, tail_color;
    public String engine_hours, detect_engine;
    public int min_moving_speed, min_fuel_fillings, min_fuel_thefts, snap_to_road;

    public String plate_number, device_model;

    public String vin;
    public String registration_number;
    public String object_owner;

    public DeviceData()
    {
        name = "";
        imei = "";
        fuel_quantity = "";
        fuel_price = "";
        //fuel_per_km = "";
        sim_number = "";
        tail_color = "";
        engine_hours = "";
        detect_engine = "";
        plate_number = "";
        device_model = "";
        vin = "";
        registration_number = "";
        object_owner = "";
        fuel_measurement_id = 1;
    }

    public DeviceData(DeviceData another)
    {
        this.id = another.id;
        this.user_id = another.user_id;
        this.traccar_device_id = another.traccar_device_id;
        this.icon_id = another.icon_id;
        this.fuel_measurement_id = another.fuel_measurement_id;
        this.tail_length = another.tail_length;
        this.group_id = another.group_id;
        this.timezone_id = another.timezone_id;
        this.active = another.active;
        this.deleted = another.deleted;
        this.name = another.name;
        this.imei = another.imei;
        this.tail_length = another.tail_length;
        this.fuel_quantity = another.fuel_quantity;
        this.fuel_price = another.fuel_price;
        this.fuel_per_km = another.fuel_per_km;
        this.sim_number = another.sim_number;
        this.expiration_date = another.expiration_date;
        this.current_geofences = another.current_geofences;
        this.tail_color = another.tail_color;
        this.engine_hours = another.engine_hours;
        this.detect_engine = another.detect_engine;
        this.min_moving_speed = another.min_moving_speed;
        this.min_fuel_fillings = another.min_fuel_fillings;
        this.min_fuel_thefts = another.min_fuel_thefts;
        this.snap_to_road = another.snap_to_road;
        this.plate_number = another.plate_number;
        this.device_model = another.device_model;
        this.vin = another.vin;
        this.registration_number = another.registration_number;
        this.object_owner = another.object_owner;
    }
}
/*-
        current_events: null
        current_driver_id: null
        timezone: null
        }*/