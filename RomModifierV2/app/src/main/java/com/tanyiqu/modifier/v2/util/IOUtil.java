package com.tanyiqu.modifier.v2.util;

import android.os.Environment;


public class IOUtil {

    public static String getSDCardPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

}
