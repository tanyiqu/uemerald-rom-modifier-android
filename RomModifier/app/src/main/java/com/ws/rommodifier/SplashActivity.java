package com.ws.rommodifier;

import android.Manifest;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import pub.devrel.easypermissions.EasyPermissions;

public class SplashActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("MyApp","SplashActivity 启动");
        super.onCreate(savedInstanceState);
        if(EasyPermissions.hasPermissions(this,PermissionActivity.perms)){
            startActivity(new Intent(this, MainActivity.class));
        }else {
            startActivity(new Intent(this, PermissionActivity.class));//启动完主Activity就finish
        }
        finish();
    }
}
