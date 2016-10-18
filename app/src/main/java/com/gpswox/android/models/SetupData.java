package com.gpswox.android.models;

/**
 * Created by gintas on 30/11/15.
 */
public class SetupData
{
    public int id, group_id, map_id;
    public String devices_limit, email, subscription_expiration, lang, unit_of_distance, unit_of_capacity, unit_of_altitude;
    public int timezone_id, sms_gateway;
    public String sms_gateway_url;
    public String sms_gateway_app_date;
    public SmsGatewayParams sms_gateway_params;
}
