package com.gpswox.android.models;

import java.util.ArrayList;

/**
 * Created by gintas on 02/12/15.
 */
public class CustomEvent
{
    public int id, user_id;
    public String protocol;
    public String message;
    public int always;
    public ArrayList<CustomEventCondition> conditions;
    public ArrayList<CustomEventTag> tags;
}
