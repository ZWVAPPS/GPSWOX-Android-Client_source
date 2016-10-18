package com.gpswox.android.models;

/**
 * Created by gintas on 29/11/15.
 */
public class Driver
{
    public int id, user_id, device_id;
    public String name, rfid, phone, email, description;
    public Device device;

    public Driver()
    {
        name = "";
        rfid = "";
        phone = "";
        email = "";
        description = "";
    }

    public Driver(Driver another)
    {
        this.id = another.id;
        this.user_id = another.user_id;
        this.device_id = another.device_id;
        this.name = another.name;
        this.rfid = another.rfid;
        this.phone = another.phone;
        this.email = another.email;
        this.description = another.description;
    }
}