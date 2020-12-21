package com.tanyiqu.modifier.v2.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.tanyiqu.modifier.v2.data.Constants;

import pub.devrel.easypermissions.EasyPermissions;


public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        startActivity(new Intent(SplashActivity.this, MainActivity.class));
//        finish();

        if (EasyPermissions.hasPermissions(this, Constants.perms_storage)) {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
        } else {
            startActivity(new Intent(SplashActivity.this, PermissionActivity.class));
        }
        finish();

    }
}
