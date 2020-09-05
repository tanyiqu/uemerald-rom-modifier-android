package com.ws.rommodifier;

import android.Manifest;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;



import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class PermissionActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    public static String[] perms = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};
    public final static int CALL_BACK_STORAGE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("MyApp","PermissionActivity 启动");
        super.onCreate(savedInstanceState);
        //申请权限
        EasyPermissions.requestPermissions(this,"请求权限",CALL_BACK_STORAGE,perms);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        switch (requestCode){
            case CALL_BACK_STORAGE:
                Toast.makeText(this, "已同意存储权限", Toast.LENGTH_SHORT).show();
                startApp();
                break;
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        switch (requestCode){
            case CALL_BACK_STORAGE:
                Toast.makeText(this, "已拒绝存储权限", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this,PermissionActivity.perms,CALL_BACK_STORAGE);
                break;
        }
        if(EasyPermissions.somePermissionPermanentlyDenied(this,perms)){
            Toast.makeText(this, "权限已被永久拒绝", Toast.LENGTH_SHORT).show();
            new AppSettingsDialog
                    .Builder(this)
                    .setTitle("权限已被永久拒绝")
                    .setRationale("该应用需要此权限，否则无法正常使用，是否打开设置")
                    .setPositiveButton("嗯嗯")
                    .setNegativeButton("嘤嘤嘤")
                    .build()
                    .show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults,this);
    }

    //启动
    public void startApp(){
        if(EasyPermissions.hasPermissions(this, perms)){
            startActivity(new Intent(this,MainActivity.class));
            finish();
        }
    }

}
