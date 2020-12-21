package com.tanyiqu.modifier.v2.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tanyiqu.modifier.v2.R;
import com.tanyiqu.modifier.v2.data.Constants;
import com.tanyiqu.modifier.v2.util.IOUtil;
import com.tanyiqu.modifier.v2.util.PerUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AddListener();
    }


    // 添加监听
    void AddListener() {
        FloatingActionButton button = findViewById(R.id.fab_open);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, IOUtil.getSDCardPath(), Toast.LENGTH_SHORT).show();
                PerUtil.requestPermissions(MainActivity.this, getString(R.string.request_permission), 0, Constants.perms_storage);
            }
        });
    }

}
