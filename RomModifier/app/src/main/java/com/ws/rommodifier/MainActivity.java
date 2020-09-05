package com.ws.rommodifier;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;

public class MainActivity extends AppCompatActivity {

    private static long exitTime = 0;
    public static String lastFilePath = "首次打开";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("MyApp","MainActivity 启动");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        detect();
    }

    /**
     * 读取配置文件
     */
    private void detect(){
        File iniFile = new File(Utils.INI_FILE_PATH);
        boolean flag = false;
        if(!iniFile.exists()){//文件不存在，创建
            try {
                flag = iniFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(flag){//文件创建成功，进行初始写入
                Log.i("MyApp","ini文件创建成功，路径为：" + iniFile.getPath());
                Toast.makeText(this, "正在进行首次运行的初始化", Toast.LENGTH_SHORT).show();
                //写入基本信息
                try {
                    FileWriter writer = new FileWriter(iniFile);
                    BufferedWriter bw = new BufferedWriter(writer);
                    bw.write("[last_open]");bw.newLine();
                    bw.write("首次打开");bw.newLine();
                    bw.write("[is_active]");bw.newLine();
                    bw.write("true");
                    bw.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }else{//文件创建失败
                Log.i("MyApp","ini文件创建失败");
                Toast.makeText(this, "文件创建失败，无法为您处理这个情况\n请重新安装并且同意所有权限！", Toast.LENGTH_SHORT).show();
            }
            Toast.makeText(this, "首次运行初始化完成", Toast.LENGTH_SHORT).show();
        }
        //文件已存在
        //读取上次打开的路径
        try {
            FileReader reader = new FileReader(iniFile);
            BufferedReader br = new BufferedReader(reader);
            br.readLine();
            lastFilePath = br.readLine();
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
        TextView lblLastOpen = findViewById(R.id.lblLastOpen);
        lblLastOpen.setText("上次打开:" + lastFilePath);
    }

    /**
     * 重写onKeyDown实现两次点击返回
     * @param keyCode
     * @param event
     * @return
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 1800) {
                Toast.makeText(getApplicationContext(), "再按一次退出应用",Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            }
            else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //选择Rom
    public void on_btnChooseRom_clicked(View view){
        startActivity(new Intent(this, FileSelectActivity.class));
    }

    //打开上次Rom
    public void on_btnOpenLastRom_clicked(View view){
        File file = new File(lastFilePath);
        if(!file.exists()){
            Toast.makeText(this, "文件已经不存在了", Toast.LENGTH_SHORT).show();
            return;
        }else{//文件存在 且 一定是.gba的
            //启动修改界面
            Intent intent = new Intent(this,TabActivity.class);
            Bundle bundle = new Bundle();
            bundle.putCharSequence("filePath",file.getPath());
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i("MyApp","MainActivity 重新启动");
        //重新读取文件信息
        detect();
    }
}
