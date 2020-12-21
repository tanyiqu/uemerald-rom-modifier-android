package com.tanyiqu.modifier.v2.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.tanyiqu.modifier.v2.R;
import com.tanyiqu.modifier.v2.data.Constants;

import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class PermissionActivity extends Activity implements EasyPermissions.PermissionCallbacks {

    static final int STORAGE_CALL_BACK_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);
        requestPermissions();
    }

    // 申请权限
    private void requestPermissions() {
        if (EasyPermissions.hasPermissions(this, Constants.perms_storage)) {
            Toast.makeText(this, "已经有存储权限", Toast.LENGTH_SHORT).show();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.request_permission), STORAGE_CALL_BACK_CODE, Constants.perms_storage);
        }
    }

    // 权限申请完成后执行
    private void pass() {
        startActivity(new Intent(this, MainActivity.class));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (requestCode) {
            case STORAGE_CALL_BACK_CODE:
                Toast.makeText(this, "已同意存储权限", Toast.LENGTH_SHORT).show();
                pass();
                break;
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (requestCode) {
            case STORAGE_CALL_BACK_CODE:
                Toast.makeText(this, "已拒绝存储权限", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this, Constants.perms_storage, STORAGE_CALL_BACK_CODE);
                break;
        }
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            Toast.makeText(this, "权限已被永久拒绝", Toast.LENGTH_SHORT).show();
            new AppSettingsDialog
                    .Builder(this)
                    .setTitle("权限已被永久拒绝")
                    .setRationale("该应用需要此权限，否则无法正常使用，是否打开设置")
                    .setPositiveButton("确定")
                    .setNegativeButton("取消")
                    .build()
                    .show();
        }
    }
}
