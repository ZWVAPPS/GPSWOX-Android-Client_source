package com.gpswox.android.api;

import android.content.Context;
import android.util.Log;


import com.gpswox.android.utils.DataSaver;

import retrofit.Endpoint;
import retrofit.ErrorHandler;
import retrofit.RestAdapter;
import retrofit.RetrofitError;

public class API
{
    public static class DynamicEndpoint implements Endpoint
    {
        private String url;
        public void setUrl(String url) {
            this.url = url;
        }
        @Override
        public String getUrl() {
            return url;
        }

        @Override
        public String getName() {
            return "default";
        }
    }

	static RestAdapter restAdapter;
	static ApiInterface apiInterface;
    static DynamicEndpoint endpoint;
	
	public static ApiInterface getApiInterface(Context context)
	{
        if(endpoint == null)
            endpoint = new DynamicEndpoint();

        endpoint.setUrl((String) DataSaver.getInstance(context).load("server"));

	    if(restAdapter == null)
	        restAdapter = new RestAdapter.Builder()
	        .setEndpoint(endpoint)
	        .setLogLevel(RestAdapter.LogLevel.FULL)
	        //.setErrorHandler(new APIErrorHandler())
	        .build();
	    if(apiInterface == null)
	        apiInterface = restAdapter.create(ApiInterface.class);

	    return apiInterface;
	}

	private class ErrorResponse
	{
		public int status;
		String message;
	}
	private static class APIErrorHandler implements ErrorHandler
	{
		@Override
		public Throwable handleError(RetrofitError cause)
		{
			String errorDescription = "";
			 
			if (cause.isNetworkError())
				errorDescription = "NETWORK ERROR";
			else {
				if (cause.getResponse() == null)
					errorDescription = "NO RESPONSE";
	            else {
	            	try {
	            		ErrorResponse errorResponse = (ErrorResponse) cause.getBodyAs(ErrorResponse.class);
            			errorDescription = errorResponse.message;
	            	} catch (Exception ex) {
	            		try {
	            			errorDescription = "NETWORK HTTP ERROR " + cause.getResponse().getStatus();
	            		} catch (Exception ex2) {
	            			Log.e("API", "HANDLE ERROR: " + ex2.getLocalizedMessage());
	            			errorDescription = "UNKNOWN";
	            		}
	            	}
	            }
			}
			return new Exception(errorDescription);
		}
	}
}
