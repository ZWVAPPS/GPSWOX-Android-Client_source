package com.gpswox.android.api;

import com.gpswox.android.models.Alert;
import com.gpswox.android.models.AlertDevice;
import com.gpswox.android.models.AlertDistance;
import com.gpswox.android.models.AlertDriver;
import com.gpswox.android.models.AlertEventProtocol;
import com.gpswox.android.models.AlertEventType;
import com.gpswox.android.models.AlertFuelType;
import com.gpswox.android.models.AlertGeofence;
import com.gpswox.android.models.AlertZone;
import com.gpswox.android.models.CustomEvent;
import com.gpswox.android.models.CustomEventConditionType;
import com.gpswox.android.models.CustomEventProtocol;
import com.gpswox.android.models.Device;
import com.gpswox.android.models.DeviceIcon;
import com.gpswox.android.models.Driver;
import com.gpswox.android.models.Event;
import com.gpswox.android.models.FuelMeasurement;
import com.gpswox.android.models.Geofence;
import com.gpswox.android.models.HistoryItem;
import com.gpswox.android.models.HistoryItemClass;
import com.gpswox.android.models.HistoryItemImage;
import com.gpswox.android.models.HistoryMessage;
import com.gpswox.android.models.HistorySensor;
import com.gpswox.android.models.MapIcon;
import com.gpswox.android.models.ObjectGroup;
import com.gpswox.android.models.POIMarker;
import com.gpswox.android.models.Report;
import com.gpswox.android.models.ReportFormat;
import com.gpswox.android.models.ReportStop;
import com.gpswox.android.models.ReportType;
import com.gpswox.android.models.SendCommandCommand;
import com.gpswox.android.models.SendCommandDevice;
import com.gpswox.android.models.SendCommandTemplate;
import com.gpswox.android.models.SendCommandUnit;
import com.gpswox.android.models.SetupData;
import com.gpswox.android.models.SmsAuthenticationSelect;
import com.gpswox.android.models.SmsGatewayEncoding;
import com.gpswox.android.models.SmsGatewayRequestMethod;
import com.gpswox.android.models.Timezone;
import com.gpswox.android.models.UnitOfAltitude;
import com.gpswox.android.models.UnitOfCapacity;
import com.gpswox.android.models.UnitOfDistance;
import com.gpswox.android.models.UserGprsTemplate;
import com.gpswox.android.models.UserSmsTemplate;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;

public interface ApiInterface
{
    @FormUrlEncoded
    @POST("/login")
    void login(@Field("email") String email, @Field("password") String password, Callback<LoginResult> cb);

    public static class LoginResult
    {
        public int status;
        public String user_api_hash;
    }

    @GET("/registration_status")
    void registrationStatus(@Query("lang") String lang, Callback<RegistrationStatusResult> cb);

    public static class RegistrationStatusResult
    {
        public int status;
    }

    @GET("/device_stop_time")
    void deviceStopTime(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang, @Query("device_id") int id, Callback<DeviceStopTimeResult> cb);

    public static class DeviceStopTimeResult
    {
        public String time;
    }

    //////////////////////////////////////////////////////// DEVICES
    @GET("/get_devices")
    void getDevices(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang, Callback<ArrayList<GetDevicesItem>> cb);

    public static class GetDevicesItem
    {
        public String title; // group title
        public ArrayList<Device> items;

        public GetDevicesItem(GetDevicesItem another)
        {
            this.title = another.title;
            this.items = new ArrayList<>();
            for (Device device : another.items)
                items.add(new Device(device));
        }
    }

    @GET("/add_device_data")
    void getFieldsDataForEditing(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang, @Query("perm") int perm, Callback<GetFieldsDataForEditingResult> cb);

    public static class GetFieldsDataForEditingResult
    {
        public ArrayList<ObjectGroup> device_groups;
        public ArrayList<FuelMeasurement> device_fuel_measurements;

        public ArrayList<DeviceIcon> device_icons;

        public ArrayList<Timezone> timezones;
        public String error;
    }

    @FormUrlEncoded
    @POST("/add_device")
    void addNewDevice(@Field("user_api_hash") String user_api_hash,
                      @Field("lang") String lang,
                      @Field("name") String name,
                      @Field("imei") String imei,
                      @Field("icon_id") int icon_id,
                      @Field("group_id") int group_id,
                      @Field("sim_number") String sim_number,
                      @Field("device_model") String device_model,
                      @Field("plate_number") String plate_number,
                      @Field("vin") String vin,
                      @Field("registration_number") String registration_number,
                      @Field("object_owner") String object_owner,
                      @Field("fuel_measurement_id") int fuel_measurement_id,
                      @Field("fuel_quantity") String fuel_quantity,
                      @Field("fuel_price") String fuel_price,
                      @Field("min_moving_speed") int min_moving_speed,
                      @Field("min_fuel_fillings") int min_fuel_fillings,
                      @Field("min_fuel_thefts") int min_fuel_thefts,
                      @Field("tail_color") String tail_color,
                      @Field("tail_length") int tail_length,
                      @Field("timezone_id") int timezone_id,
                      Callback<AddDeviceResult> cb);

    public static class AddDeviceResult
    {

    }

    @FormUrlEncoded
    @POST("/edit_device")
    void saveEditedDevice(@Field("user_api_hash") String user_api_hash,
                          @Field("lang") String lang,
                          @Field("id") int id,
                          @Field("name") String name,
                          @Field("imei") String imei,
                          @Field("icon_id") int icon_id,
                          @Field("group_id") int group_id,
                          @Field("sim_number") String sim_number,
                          @Field("device_model") String device_model,
                          @Field("plate_number") String plate_number,
                          @Field("vin") String vin,
                          @Field("registration_number") String registration_number,
                          @Field("object_owner") String object_owner,
                          @Field("fuel_measurement_id") int fuel_measurement_id,
                          @Field("fuel_quantity") String fuel_quantity,
                          @Field("fuel_price") String fuel_price,
                          @Field("min_moving_speed") int min_moving_speed,
                          @Field("min_fuel_fillings") int min_fuel_fillings,
                          @Field("min_fuel_thefts") int min_fuel_thefts,
                          @Field("tail_color") String tail_color,
                          @Field("tail_length") int tail_length,
                          @Field("timezone_id") int timezone_id,
                          Callback<SaveEditedDeviceResult> cb);

    public static class SaveEditedDeviceResult
    {

    }

    @POST("/change_active_device")
    void changeActiveDevice(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang, @Query("id") int id, @Query("active") boolean active, Callback<ChangeActiveDeviceResult> cb);

    public static class ChangeActiveDeviceResult
    {
    }


    //////////////////////////////////////////////////////////// REPORTS
    @GET("/get_reports")
    void getReports(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang, Callback<GetReportsResult> cb);

    public static class GetReportsResult
    {
        public int status;
        public GetReportsItem items;

        public class GetReportsItem
        {
            public GetReportsData reports;
            public ArrayList<ReportType> types;
        }

        public class GetReportsData
        {
            public int total, per_page, current_page, last_page, from, to;
            public ArrayList<Report> data;

        }
    }

    @FormUrlEncoded
    @POST("/destroy_report")
    void deleteReport(@Field("user_api_hash") String user_api_hash, @Field("report_id") int report_id, @Field("lang") String lang, Callback<DeleteReportResult> cb);

    public static class DeleteReportResult
    {
    }

    @GET("/add_report_data")
    void getDataForReports(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang, Callback<GetReportDataResult> cb);

    public static class GetReportDataResult
    {
        public ArrayList<Device> devices;
        public ArrayList<ReportFormat> formats;
        public ArrayList<ReportStop> stops;
        public ArrayList<Geofence> geofences;
    }

    @POST("/add_report")
    void addNewReport(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang,
                      @Query("title") String title,
                      @Query("type") int type,
                      @Query("date_from") String dateFrom,
                      @Query("date_to") String dateTo,
                      @Query("format") String format,
                      @Query("stops") int stops,
                      @Query("speed_limit") int speed_limit,
                      @Query("devices") String devices_array,
                      @Query("geofences") String geofences_array,
                      @Query("daily") int daily,
                      @Query("weekly") int weekly,
                      @Query("send_to_email") String send_to_email,
                      Callback<AddNewReportResult> cb);

    public static class AddNewReportResult
    {
    }

    @POST("/edit_report")
    void saveEditedReport(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang,
                          @Query("id") int id,
                          @Query("title") String title,
                          @Query("type") int type,
                          @Query("date_from") String dateFrom,
                          @Query("date_to") String dateTo,
                          @Query("format") String format,
                          @Query("stops") int stops,
                          @Query("speed_limit") int speed_limit,
                          @Query("devices") String devices_array,
                          @Query("geofences") String geofences_array,
                          @Query("daily") int daily,
                          @Query("weekly") int weekly,
                          @Query("send_to_email") String send_to_email,
                          Callback<SaveEditedReportResult> cb);

    public static class SaveEditedReportResult
    {
    }

    //////////////////////////////////////////////////////////// SENSORS
    @GET("/destroy_sensor")
    void destroySensor(@Query("user_api_hash") String user_api_hash, @Query("sensor_id") int sensor_id, @Query("lang") String lang, Callback<DestroySensorResult> cb);

    public static class DestroySensorResult
    {
        public int status;
    }

    //////////////////////////////////////////////////////////// EVENTS
    @GET("/get_events")
    void getEvents(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang, @Query("page") int page, Callback<GetEventsResult> cb);

    public static class GetEventsResult
    {
        public int status;
        public GetEventsResultItems items;

        public class GetEventsResultItems
        {
            public int total, per_page, current_page, last_page, from, to;
            public ArrayList<Event> data;
        }
    }

    @POST("/destroy_events")
    void clearAllEvents(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang, Callback<ClearEventsResult> cb);

    public static class ClearEventsResult
    {
    }

    //////////////////////////////////////////////////////////// MY ACCOUNT DATA
    @GET("/get_user_data")
    void getMyAccountData(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang, Callback<GetMyAccountDataResult> cb);

    public static class GetMyAccountDataResult
    {
        public String email, expiration_date, plan;
        public int days_left, devices_limit;
    }

    //////////////////////////////////////////////////////////// SETUP
    @GET("/edit_setup_data")
    void getSetupData(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang, Callback<SetupDataResult> cb);

    public static class SetupDataResult
    {
        public SetupData item;
        public ArrayList<Timezone> timezones;

        public ArrayList<UnitOfDistance> units_of_distance;
        public ArrayList<UnitOfCapacity> units_of_capacity;
        public ArrayList<UnitOfAltitude> units_of_altitude;
        public ArrayList<ObjectGroup> groups;

        public int sms_queue_count;
        public ArrayList<SmsGatewayRequestMethod> request_method_select;
        public ArrayList<SmsGatewayEncoding> encoding_select;
        public ArrayList<SmsAuthenticationSelect> authentication_select;
    }

    //////////////////////////////////////////////////////////// DRIVERS
    @GET("/get_user_drivers")
    void getUserDrivers(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang, @Query("page") int page, Callback<GetUserDriversResult> cb);

    public static class GetUserDriversResult
    {
        public int status;
        public GetUserDriversResultItem items;

        public class GetUserDriversResultItem
        {
            public GetUserDriversResultDrivers drivers;
        }

        public class GetUserDriversResultDrivers
        {
            public int total, per_page, current_page, last_page, from, to;
            public ArrayList<Driver> data;
        }
    }

    @POST("/destroy_user_driver")
    void destroyUserDriver(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang, @Query("user_driver_id") int user_driver_id, Callback<DestroyUserDriverResult> cb);

    public static class DestroyUserDriverResult
    {
    }

    @POST("/add_user_driver")
    void addUserDriver(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang,
                       @Query("name") String name,
                       @Query("device_id") int device_id,
                       @Query("rfid") String rfid,
                       @Query("phone") String phone,
                       @Query("email") String email,
                       @Query("description") String description,
                       Callback<AddUserDriverResult> cb);

    public static class AddUserDriverResult
    {
        public int status;
        public Driver item;
    }

    @POST("/edit_setup")
    void saveEditedSetup(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang,
                         @Query("unit_of_distance") String unit_of_distance,
                         @Query("unit_of_capacity") String unit_of_capacity,
                         @Query("unit_of_altitude") String unit_of_altitude,
                         @Query("timezone_id") int timezone_id,
                         @Query("groups") String groups_array,
                         @Query("sms_gateway") int sms_gateway,
                         @Query("request_method") String request_method,
                         @Query("encoding") String encoding,
                         @Query("authentication") String authentication,
                         @Query("username") String username,
                         @Query("password") String password,
                         @Query("sms_gateway_url") String sms_gateway_url,
                         @Query("auth_id") String auth_id,
                         @Query("auth_token") String auth_token,
                         @Query("senders_phone") String senders_phone,
                         Callback<AddUserDriverResult> cb);

    public static class SaveEditedSetupResult
    {
    }

    //////////////////////////////////////////////////////////// MAP ICONS
    @GET("/get_map_icons")
    void getMapIcons(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang, Callback<GetMapIconsResult> cb);

    public static class GetMapIconsResult
    {
        public int status;
        public ArrayList<MapIcon> items;
    }

    //////////////////////////////////////////////////////////// CUSTOM EVENTS
    @GET("/get_custom_events")
    void getCustomEvents(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang, Callback<GetCustomEventsResult> cb);

    public static class GetCustomEventsResult
    {
        public int status;
        public GetCustomEventsResultItems items;

        public class GetCustomEventsResultItems
        {
            public GetCustomEventsResultItemsEvents events;

            public class GetCustomEventsResultItemsEvents
            {
                public int total, per_page, current_page, last_page, from, to;
                public ArrayList<CustomEvent> data;
            }
        }
    }

    @POST("/destroy_custom_event")
    void destroyCustomEvent(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang, @Query("custom_event_id") int custom_event_id, Callback<DestroyCustomEventResult> cb);

    public static class DestroyCustomEventResult
    {
    }

    @GET("/add_custom_event")
    void addCustomEvent(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang,
                        @Query("protocol") String protocol,
                        @Query("message") String message,
                        @Query("show_always") int show_always,
                        @Query("conditions") String conditions_array, Callback<AddCustomEventResult> cb);

    public static class AddCustomEventResult
    {
        public int status;
        public CustomEvent item;
    }

    @GET("/add_custom_event_data")
    void getCustomEventData(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang, Callback<GetCustomEventDataResult> cb);

    public static class GetCustomEventDataResult
    {
        public ArrayList<CustomEventProtocol> protocols;
        public ArrayList<CustomEventConditionType> types;
    }


    //////////////////////////////////////////////////////////// USER SMS TEMPLATES
    @GET("/get_user_sms_templates")
    void getUserSmsTemplates(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang, Callback<GetUserSmsTemplatesResult> cb);

    public static class GetUserSmsTemplatesResult
    {
        public int status;
        public GetUserSmsTemplatesResultItems items;

        public class GetUserSmsTemplatesResultItems
        {
            public GetUserSmsTemplatesResultItemsTemplates user_sms_templates;

            public class GetUserSmsTemplatesResultItemsTemplates
            {
                public int total, per_page, current_page, last_page, from, to;
                public ArrayList<UserSmsTemplate> data;
            }
        }
    }

    @POST("/add_user_sms_template")
    void addUserSmsTemplate(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang, @Query("title") String title, @Query("message") String message, Callback<AddUserSmsTemplateResult> cb);

    public static class AddUserSmsTemplateResult
    {
        public int status;
        public UserSmsTemplate item;
    }

    @POST("/destroy_user_sms_template")
    void destroyUserSmsTemplate(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang, @Query("user_sms_template_id") int user_sms_template_id, Callback<DestroyUserSmsTemplateResult> cb);

    public static class DestroyUserSmsTemplateResult
    {
    }

    //////////////////////////////////////////////////////////// USER GPRS TEMPLATES
    @GET("/get_user_gprs_templates")
    void getUserGprsTemplates(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang, Callback<GetUserGprsTemplatesResult> cb);

    public static class GetUserGprsTemplatesResult
    {
        public int status;
        public GetUserGprsTemplatesResultItems items;

        public class GetUserGprsTemplatesResultItems
        {
            public GetUserGprsTemplatesResultItemsTemplates user_gprs_templates;

            public class GetUserGprsTemplatesResultItemsTemplates
            {
                public int total, per_page, current_page, last_page, from, to;
                public ArrayList<UserGprsTemplate> data;
            }
        }
    }

    @POST("/destroy_user_gprs_template")
    void destroyUserGprsTemplate(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang, @Query("user_gprs_template_id") int user_gprs_template_id, Callback<DestroyUserGprsTemplateResult> cb);

    public static class DestroyUserGprsTemplateResult
    {
    }

    @POST("/add_user_gprs_template")
    void addUserGprsTemplate(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang, @Query("title") String title, @Query("message") String message, Callback<AddUserGprsTemplateResult> cb);

    public static class AddUserGprsTemplateResult
    {
        public int status;
        public UserGprsTemplate item;
    }


    //////////////////////////////////////////////////////////// SEND TEST SMS
    @POST("/send_test_sms")
    void sendTestSms(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang,
                     @Query("request_method") String request_method,
                     @Query("authentication") String authentication,
                     @Query("username") String username,
                     @Query("password") String password,
                     @Query("encoding") String encoding,
                     @Query("auth_id") String auth_id,
                     @Query("auth_token") String auth_token,
                     @Query("senders_phone") String senders_phone,
                     @Query("sms_gateway_url") String sms_gateway_url,
                     @Query("mobile_phone") String mobile_phone,
                     @Query("message") String message,
                     Callback<SendTestSmsResult> cb);

    public static class SendTestSmsResult
    {
    }

    //////////////////////////////////////////////////////////// HISTORY
    @GET("/get_history_messages")
    void getHistoryMessages(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang,
                            @Query("device_id") int device_id,
                            @Query("from_date") String from_date,
                            @Query("from_time") String from_time,
                            @Query("to_date") String to_date,
                            @Query("to_time") String to_time,
                            @Query("page") int page,
                            @Query("limit") int limit,
                            Callback<GetHistoryMessagesResult> cb);

    public static class GetHistoryMessagesResult
    {
        public int status;
        public GetHistoryMessagesResultMessages messages;

        public class GetHistoryMessagesResultMessages
        {
            public int total, per_page, current_page, last_page, from, to;
            public ArrayList<HistoryMessage> data;
        }
    }

    @GET("/get_history")
    void getHistory(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang,
                    @Query("device_id") int device_id,
                    @Query("from_date") String from_date,
                    @Query("from_time") String from_time,
                    @Query("to_date") String to_date,
                    @Query("to_time") String to_time,
                    @Query("snap_to_road") boolean snap_to_road,
                    Callback<GetHistoryResult> cb);

    public static class GetHistoryResult
    {
        public int status;
        public ArrayList<HistoryItem> items;
        public String distance_sum, top_speed, move_duration, stop_duration, fuel_consumption;
        public Device device;
        public ArrayList<HistorySensor> sensors;
        public ArrayList<HistoryItemClass> item_class;
        public ArrayList<HistoryItemImage> images;
    }

    //////////////////////////////////////////////////////////// LOAD POI ICONS

    @GET("/get_user_map_icons")
    void loadPOIMarkers(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang, Callback<LoadPOIMarkersResult> cb);

    public static class LoadPOIMarkersResult
    {

        public int status;
        public LoadPOIMarkersResultItems items;

        public class LoadPOIMarkersResultItems
        {
            public ArrayList<POIMarker> mapIcons;
        }
    }

    @POST("/destroy_map_icon")
    void destroyPOIMarker(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang, @Query("map_icon_id") int map_icon_id, Callback<DestroyPOIMarkerResult> cb);

    public static class DestroyPOIMarkerResult
    {
    }

    @GET("/get_map_icons")
    void getPOIMapIcons(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang, Callback<GetPOIMapIconsResult> cb);

    public static class GetPOIMapIconsResult
    {
        public int status;
        public ArrayList<MapIcon> items;
    }

    @POST("/add_map_icon")
    void savePOIMarker(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang,
                       @Query("name") String name,
                       @Query("description") String description,
                       @Query("map_icon_id") int map_icon_id,
                       @Query("coordinates") String coordinates,
                       Callback<SavePOIMarkerResult> cb);

    public static class SavePOIMarkerResult
    {
    }

    //////////////////////////////////////////////////////////// SEND COMMAND
    @GET("/send_command_data")
    void getSendCommandData(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang, Callback<SendCommandData> cb);

    public static class SendCommandData
    {
        public ArrayList<SendCommandDevice> devices_sms;
        public ArrayList<SendCommandDevice> devices_gprs;
        public ArrayList<SendCommandTemplate> sms_templates;
        public ArrayList<SendCommandTemplate> gprs_templates;
        public ArrayList<SendCommandCommand> commands;
        public ArrayList<SendCommandUnit> units;
    }

    @POST("/send_gprs_command")
    void sendGprsCommand(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang,
                         @Query("frequency") int frequency,
                         @Query("unit") String unit,
                         @Query("device_id") int device_id,
                         @Query("type") String type, Callback<SendGprsCommandResult> cb);

    public static class SendGprsCommandResult
    {
    }

    @FormUrlEncoded
    @POST("/send_sms_command")
    void sendSmsCommand(@Field("user_api_hash") String user_api_hash,
                        @Field("lang") String lang,
                        @Field("frequency") int frequency,
                        @Field("unit") String unit,
                        @Field("message") String message,
                        @Field("devices[]") String devices_array,
                        Callback<SendSmsCommandResult> cb);

    public static class SendSmsCommandResult
    {
    }

    //////////////////////////////////////////////////////////// GEOFENCING
    @GET("/get_geofences")
    void getGeofences(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang, Callback<GetGeofencesResult> cb);

    public static class GetGeofencesResult
    {
        public int status;
        public GetGeofencesResultItems items;

        public class GetGeofencesResultItems
        {
            public ArrayList<Geofence> geofences;
        }
    }

    @GET("/add_geofence_data")
    void getGeofenceData(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang, Callback<GetGeofenceDataResult> cb);

    public static class GetGeofenceDataResult
    {
    }

    @POST("/destroy_geofence")
    void destroyGeofence(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang, @Query("geofence_id") int geofence_id, Callback<DestroyGeofenceResult> cb);

    public static class DestroyGeofenceResult
    {
    }

    @POST("/change_active_geofence")
    void setGeofenceActive(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang, @Query("id") int id, @Query("active") boolean active, Callback<SetGeofenceActiveResult> cb);

    public static class SetGeofenceActiveResult
    {
    }

    @POST("/add_geofence")
    void addNewGeofence(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang,
                        @Query("name") String name,
                        @Query("polygon_color") String polygon_color,
                        @Query("polygon") String polygon_array,
                        Callback<AddNewGeofenceResult> cb);

    public static class AddNewGeofenceResult
    {
    }

    //////////////////////////////////////////////////////////// ALERTS
    @GET("/get_alerts")
    void getAlerts(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang, Callback<GetAlertsResult> cb);

    public static class GetAlertsResult
    {
        int status;
        public GetAlertsResultItemsAlerts items;

        public class GetAlertsResultItemsAlerts
        {
            public ArrayList<Alert> alerts;
        }
    }

    @GET("/add_alert_data")
    void getAlertData(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang, Callback<GetAlertDataResult> cb);

    public static class GetAlertDataResult
    {
        int status;
        public ArrayList<AlertDevice> devices;
        public ArrayList<AlertGeofence> geofences;
        public ArrayList<AlertDriver> drivers;
        public ArrayList<AlertZone> alert_zones;
        public ArrayList<AlertFuelType> alert_fuel_type;
        public ArrayList<AlertDistance> alert_distance;
        public ArrayList<AlertEventType> event_types;
        public ArrayList<AlertEventProtocol> event_protocols;
    }

    @POST("/change_active_alert")
    void changeAlertActive(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang, @Query("id") int id, @Query("active") boolean active, Callback<ChangeAlertActiveResult> cb);

    public static class ChangeAlertActiveResult
    {
    }

    @POST("/destroy_alert")
    void destroyAlert(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang, @Query("alert_id") int alert_id, Callback<DestroyAlertResult> cb);

    public static class DestroyAlertResult
    {
    }

    @GET("/get_protocols")
    void getProtocolsForAlertData(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang, Callback<GetProtocolsResult> cb);

    public static class GetProtocolsResult
    {
        int status;
        public ArrayList<GetProtocolsResultItem> items;

        public class GetProtocolsResultItem
        {
            public int type;
            public ArrayList<AlertEventProtocol> items;
        }
    }

    @GET("/get_events_by_protocol")
    void getEventsByProtocolForDropdown(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang, @Query("type") int type, @Query("protocol") String protocol, Callback<CustomEventsByProtocol> cb);

    class CustomEventsByProtocol
    {
        /*public ArrayList<CustomEventByProtocol> list;

        public CustomEventsByProtocol()
        {
            for (CustomEventByProtocol responseItem : list)
            {

            }
        }*/
    }

    @POST("/add_alert")
    void addNewAlert(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang,
                     @Query("name") String name,
                     @Query("email") String email,
                     @Query("mobile_phone") String mobile_phone,
                     @Query("devices") String devices_array,
                     @Query("drivers") String drivers_array,
                     @Query("geofences") String geofences_array,
                     @Query("overspeed[speed]") int overspeed_speed,
                     @Query("overspeed[distance]") int overspeed_distance,
                     @Query("events_custom") String events_custom_array,
                     Callback<AddNewAlertResult> cb);

    public static class AddNewAlertResult
    {
    }

    @POST("/edit_alert")
    void saveEditedAlert(@Query("user_api_hash") String user_api_hash, @Query("lang") String lang,
                         @Query("id") int id,
                         @Query("name") String name,
                         @Query("email") String email,
                         @Query("mobile_phone") String mobile_phone,
                         @Query("devices") String devices_array,
                         @Query("drivers") String drivers_array,
                         @Query("geofences") String geofences_array,
                         @Query("overspeed[speed]") int overspeed_speed,
                         @Query("overspeed[distance]") int overspeed_distance,
                         @Query("events_custom") String events_custom_array,
                         Callback<SaveEditedAlertResult> cb);

    public static class SaveEditedAlertResult
    {
    }
}
