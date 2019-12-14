package com.ljapps.tripactionchallenge.util;

import android.app.Activity;
import android.widget.Toast;

public class Util {
    public static void toast(Activity a, String msg) {
        Toast.makeText(a, msg, Toast.LENGTH_SHORT).show();
    }

    public static void toastLong(Activity a, String msg) {
        Toast.makeText(a, msg, Toast.LENGTH_LONG).show();
    }

}
