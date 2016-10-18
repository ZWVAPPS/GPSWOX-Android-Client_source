package com.gpswox.android.models;

public class Permission
{
    public String name;
    public int view;
    public int edit;
    public int remove;

    @Override
    public String toString() {
        return name;
    }
}