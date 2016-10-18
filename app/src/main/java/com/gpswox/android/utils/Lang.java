package com.gpswox.android.utils;

import java.util.Locale;

/**
 * Created by gintas on 09/05/16.
 */
public class Lang
{
    public static String getCurrentLanguage()
    {
        return Locale.getDefault().getLanguage();
    }
}
