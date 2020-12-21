package com.tanyiqu.modifier.v2.util;

import android.app.Activity;

import pub.devrel.easypermissions.EasyPermissions;

public class PerUtil {

    public static void requestPermissions(Activity host, String rationale, int requestCode, String[] perms) {

        
        EasyPermissions.requestPermissions(host, rationale, requestCode, perms);

    }

}
