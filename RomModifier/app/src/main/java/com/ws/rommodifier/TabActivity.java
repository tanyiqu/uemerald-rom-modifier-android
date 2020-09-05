package com.ws.rommodifier;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.AndroidException;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class TabActivity extends Activity {

    public static String filePath = "null";
    HashMap<Integer, String> codeMap;
    static final int MAX_SKILL = 0x400;
    static final int MAX_BREED = 0x4B0;
    static final int MAX_ITEM = 0x320;
    String[] Pokes;
    String[] Skills;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("MyApp","TabActivity 启动");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);
        //取出传入的路径
        getFilePath();
        //加载选项卡
        init();
        //同步ini文件
        Utils.syncIniFile(filePath);
        //计算初始偏移量
        Utils.setRomOffset(filePath);
        //给所有Spinner添加监听
        addSpinnerListener();
    }

    //取出传送进去的rom路径
    private void getFilePath() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        filePath = bundle.getString("filePath");
        Log.i("MyApp","向 TabActivity 传递的信息：" + filePath);
    }

    //加载选项卡
    private void init() {
        //创建tabHost
        TabHost tabHost = findViewById(android.R.id.tabhost);
        //初始化
        tabHost.setup();
        //添加标签页
        LayoutInflater inflater = LayoutInflater.from(TabActivity.this);
        //加载布局文件
        //加载第一个
        inflater.inflate(R.layout.tab_pokemon,tabHost.getTabContentView());
        //加载第二个
        inflater.inflate(R.layout.tab_skill,tabHost.getTabContentView());
        inflater.inflate(R.layout.tab_about,tabHost.getTabContentView());
        //...
        //添加布局文件
        //添加第一个
        tabHost.addTab(tabHost.newTabSpec("tab_pokemon").setIndicator("宝可梦").setContent(R.id.tabPokemon));
        //添加第二个
        tabHost.addTab(tabHost.newTabSpec("tab_skill").setIndicator("技能").setContent(R.id.tabSkill));
        tabHost.addTab(tabHost.newTabSpec("tab_about").setIndicator("关于").setContent(R.id.tabAbout));
        //...
        //加载解码map
        loadCodeMap();
        //加载名字
        loadPokeNames();
        loadSkillNames();
    }

    private void loadCodeMap() {
        codeMap = new HashMap<Integer, String>();
        LoadCodeMap.loadCodeMap(codeMap);
//        Log.i("MyApp",codeMap.get(0x1D));
//        Log.i("MyApp",codeMap.get(0x1024));
//        Log.i("MyApp",codeMap.get(0x1E5C));
    }

    String decode(HashMap<Integer,String> codeMap,byte[] data,int size){
        StringBuilder sb = new StringBuilder("");
//        String str = "";
//        for(int i=0;i<data.length;i++){
//            str += ((int)((0xFF)&data[i]) + " ");
//        }
//        Log.i("MyApp","data : "+ str);

        for(int i=0;i<size;i++){
            //读取一个元素
            int t1 = data[i] & 0xFF;
//            Log.i("MyApp","第" + i + "次");
//            Log.i("MyApp","t1=" + t1);
            int t2;
            int key;
            //如果为FF，直接返回
            if(t1 == 0xFF)
                return sb.toString();
            //读取下一个元素
            if(i != size-1) {//如果当前不是最后一个，读取下一个
                t2 = data[i+1] & 0xFF;
                if(t2 == 0xFF)
                {
                    sb.append(codeMap.get(t1));
                    return sb.toString();
                }
                //检查有没有这个key
                key = 0xFFFF & (((data[i]&0xFF)<<8) | (data[i+1])&0xFF);
//                Log.i("MyApp","key=" + key);
                if(!codeMap.containsKey(key)){//不存在这个key
                    sb.append(codeMap.get(t1));
                    continue;
                }else{//存在这个key
                    sb.append(codeMap.get(key));
                    i++;
                    continue;
                }
            }else{
                return sb.toString();
            }
        }
        return sb.toString();
    }

    private long getOffset(long off){
        byte[] arr = new byte[4];
        File file = new File(filePath);
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(file,"r");
            raf.seek(off);
            raf.read(arr,0,3);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                assert raf != null;
                raf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        arr[3] = 0x01;
        return (long) ((arr[0] & 0xFF)|((arr[1] & 0xFF)<<8)|((arr[2] & 0xFF)<<16)|((arr[3] & 0xFF)<<24));
    }


    private void loadPokeNames() {
        Spinner spinnerPokemon = findViewById(R.id.spinnerPokemon);
        List<String> nameList = new ArrayList<>();
        long offset = getOffset(0x144);
        offset &= 0x00FFFFFF;
        byte[] data = new byte[11];
        File file = new File(filePath);
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(file,"r");
            raf.seek(offset);
            for(int i=0;i<MAX_BREED;i++){
                raf.read(data,0,11);
                String num = String.format("%03X:",i);
                String str = decode(codeMap,data,11);
                if(str.equals(""))
                    str = "-";
                nameList.add(num + str);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                raf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        int size = nameList.size();
        Pokes = (String[])nameList.toArray(new String[size]);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,Pokes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPokemon.setAdapter(adapter);
    }

    private void loadSkillNames() {
        Spinner spinnerSkill = findViewById(R.id.spinnerSkill);
        List<String> nameList = new ArrayList<>();
        long offset = getOffset(0x148);

        Log.i("MyApp","offset : " + offset);

        byte[] data = new byte[13];
        File file = new File(filePath);
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(file,"r");
            raf.seek(offset);
            for(int i=0;i<MAX_SKILL;i++){
                raf.read(data,0,13);
                String num = String.format("%03X:",i);
                String str = decode(codeMap,data,13);
                if(str.equals("")) str = "-";
                nameList.add(num + str);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                assert raf != null;
                raf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        int size = nameList.size();
        Skills = (String[])nameList.toArray(new String[size]);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,Skills);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSkill.setAdapter(adapter);
    }

    //给所有Spinner添加监听
    private void addSpinnerListener() {
        //宝可梦下拉列表监听
        Spinner spinnerPokemon = findViewById(R.id.spinnerPokemon);
        spinnerPokemon.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String str = "已选择:" + parent.getItemAtPosition(position).toString();
                if(position != 0)
                    Toast.makeText(TabActivity.this, str, Toast.LENGTH_SHORT).show();
                //根据编号显示pokemon信息
                showPokeValue(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
        //技能下拉列表监听
        Spinner spinnerSkill = findViewById(R.id.spinnerSkill);
        spinnerSkill.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String str = "已选择:" + parent.getItemAtPosition(position).toString();
                if(position != 0)
                    Toast.makeText(TabActivity.this, str, Toast.LENGTH_SHORT).show();
                //根据编号显示pokemon信息
                showSkillValue(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void showPokeValue(int index){
        //显示种族值
        showBreedValue(index);
        //显示属性
        showType(index);
        //显示特性
        showAbility(index);
        //显示蛋组
        showEggGroup(index);
        //显示其他
        showOther(index);
    }
    //没有激活只能写入属性
    private void writePokeValue(int index){
        String acString = Utils.activeString();
        //写入属性
        writeType(index);
        if(acString.length() < 4){ return; }else{
        if(acString.charAt(0) != 't'){Utils.toastToActive(this);return; }
        if(acString.charAt(1) != 'r'){Utils.toastToActive(this);return; }
        if(acString.charAt(2) != 'u'){Utils.toastToActive(this);return; }
        if(acString.charAt(3) != 'e'){Utils.toastToActive(this);return; }}
        //写入种族值
        writeBreedValue(index);
        if(acString.length() < 4){ return; }else{
        if(acString.charAt(0) != 't'){Utils.toastToActive(this);return; }
        if(acString.charAt(1) != 'r'){Utils.toastToActive(this);return; }
        if(acString.charAt(2) != 'u'){Utils.toastToActive(this);return; }
        if(acString.charAt(3) != 'e'){Utils.toastToActive(this);return; }}
        //写入特性
        writeAbility(index);
        //写入蛋组
        writeEggGroup(index);
        //写入其他
        writeOther(index);
        //提示写入成功
        Toast.makeText(this, "写入宝可梦成功", Toast.LENGTH_SHORT).show();
        //刷新一下显示数据
        showPokeValue(index);
    }
    private void showSkillValue(int index){
        String warning = getString(R.string.warning);
        long offset = Utils.OFFSET_SKILL + 0xC * index;
        int power = 0xFF & Utils.ReadByte(filePath,offset + 1);
        int rate = 0xFF & Utils.ReadByte(filePath,offset + 3);
        int type = 0xFF & Utils.ReadByte(filePath,offset + 2);
        int style = 0xFF & Utils.ReadByte(filePath,offset + 10);
        int PP = 0xFF & Utils.ReadByte(filePath,offset + 4);
        int priority = 0xFF & Utils.ReadByte(filePath,offset + 7);
        if(type>0x17 || style>0x02){
            Toast.makeText(this, warning, Toast.LENGTH_SHORT).show();
        }
        EditText txtPower = findViewById(R.id.txtPower);
        EditText txtRate = findViewById(R.id.txtRate);
        Spinner spinnerSkillType = findViewById(R.id.spinnerSkillType);
        Spinner spinnerSkillStyle = findViewById(R.id.spinnerSkillStyle);
        EditText txtPP = findViewById(R.id.txtPP);
        EditText txtPriority = findViewById(R.id.txtPriority);
        txtPower.setText(power + "");
        txtRate.setText(rate + "");
        spinnerSkillType.setSelection(type);
        spinnerSkillStyle.setSelection(style);
        txtPP.setText(PP + "");
        txtPriority.setText(priority + "");
    }
    private void writeSkillValue(int index){
        String acString = Utils.activeString();
        writeBreedValue(index);
        if(acString.length() < 4){ return; }else{
            if(acString.charAt(0) != 't'){Utils.toastToActive(this);return; }
            if(acString.charAt(1) != 'r'){Utils.toastToActive(this);return; }
            if(acString.charAt(2) != 'u'){Utils.toastToActive(this);return; }
            if(acString.charAt(3) != 'e'){Utils.toastToActive(this);return; }}
        long offset = Utils.OFFSET_SKILL + 0xC * index;
        EditText txtPower = findViewById(R.id.txtPower);
        EditText txtRate = findViewById(R.id.txtRate);
        Spinner spinnerSkillType = findViewById(R.id.spinnerSkillType);
        Spinner spinnerSkillStyle = findViewById(R.id.spinnerSkillStyle);
        EditText txtPP = findViewById(R.id.txtPP);
        EditText txtPriority = findViewById(R.id.txtPriority);
        int power,rate,type,style,PP,priority;
        String sPower,sRate,sPP,sPriority;
        sPower = txtPower.getText().toString();
        sRate = txtRate.getText().toString();
        sPP = txtPP.getText().toString();
        sPriority = txtPriority.getText().toString();
        power = Integer.parseInt(sPower);
        rate = Integer.parseInt(sRate);
        PP = Integer.parseInt(sPP);
        priority = Integer.parseInt(sPriority);
        type = spinnerSkillType.getSelectedItemPosition();
        style = spinnerSkillStyle.getSelectedItemPosition();
        if(power>255) power = 255;
        if(rate>255) rate = 255;
        if(PP>255) PP = 255;
        if(priority>127)priority = 127;
        Utils.WriteByte(filePath,offset + 1,power);
        Utils.WriteByte(filePath,offset + 3,rate);
        Utils.WriteByte(filePath,offset + 2,type);
        Utils.WriteByte(filePath,offset + 10,style);
        Utils.WriteByte(filePath,offset + 4,PP);
        Utils.WriteByte(filePath,offset + 7,priority);
        //提示写入成功
        Toast.makeText(this, "写入技能成功", Toast.LENGTH_SHORT).show();
        //刷新一下显示数据
        showSkillValue(index);
    }

    //显示种族值
    private void showBreedValue(int index){
        int bv[] = new int[6];
        EditText txtBv[] = new EditText[6];
        long offset = Utils.OFFSET_POKE + index * 0x1C;
        bv[0] = 0xFF & Utils.ReadByte(filePath,offset);
        bv[1] = 0xFF & Utils.ReadByte(filePath,offset + 0x01L);
        bv[2] = 0xFF & Utils.ReadByte(filePath,offset + 0x02L);
        bv[3] = 0xFF & Utils.ReadByte(filePath,offset + 0x03L);
        bv[4] = 0xFF & Utils.ReadByte(filePath,offset + 0x04L);
        bv[5] = 0xFF & Utils.ReadByte(filePath,offset + 0x05L);
        txtBv[0] = findViewById(R.id.txtHP);
        txtBv[1] = findViewById(R.id.txtATK);
        txtBv[2] = findViewById(R.id.txtDEF);
        txtBv[3] = findViewById(R.id.txtSPEED);
        txtBv[4] = findViewById(R.id.txtSPATK);
        txtBv[5] = findViewById(R.id.txtSPDEF);
        txtBv[0].setText(bv[0]+"");
        txtBv[1].setText(bv[1]+"");
        txtBv[2].setText(bv[2]+"");
        txtBv[3].setText(bv[3]+"");
        txtBv[4].setText(bv[4]+"");
        txtBv[5].setText(bv[5]+"");
    }
    //显示属性
    private void showType(int index){
        String warning = getString(R.string.warning);
        int type1 = 0,type2 = 0;
        Spinner spinnerType1 = findViewById(R.id.spinnerType1);
        Spinner spinnerType2 = findViewById(R.id.spinnerType2);
        long offset = Utils.OFFSET_POKE + index * 0x1C;
        type1 = 0xFF & Utils.ReadByte(filePath,offset + 6);
        type2 = 0xFF & Utils.ReadByte(filePath,offset + 7);
        if(type1 > 0x17 || type2 > 0x17){
            Toast.makeText(this, warning, Toast.LENGTH_SHORT).show();
            return;
        }
        spinnerType1.setSelection(type1);
        spinnerType2.setSelection(type2);
    }
    //显示特性
    private void showAbility(int index){
        String warning = getString(R.string.warning);
        int ab1 = 0,ab2 = 0,ab3 = 0;
        Spinner spinnerAB1 = findViewById(R.id.spinnerAbility1);
        Spinner spinnerAB2 = findViewById(R.id.spinnerAbility2);
        Spinner spinnerAB3 = findViewById(R.id.spinnerAbility3);
        long offset = Utils.OFFSET_POKE + index * 0x1C;
        ab1 = 0xFF & Utils.ReadByte(filePath,offset + 22L);
        ab2 = 0xFF & Utils.ReadByte(filePath,offset + 23L);
        ab3 = 0xFF & Utils.ReadByte(filePath,offset + 26L);
        if(ab1 > 0xE9 || ab2 > 0xE9 || ab3 > 0xE9){
            Toast.makeText(this, warning, Toast.LENGTH_SHORT).show();
            return;
        }
        spinnerAB1.setSelection(ab1);
        spinnerAB2.setSelection(ab2);
        spinnerAB3.setSelection(ab3);
    }
    //显示蛋组
    private void showEggGroup(int index){
        String warning = getString(R.string.warning);
        int eggGroup1 = 0,eggGroup2 = 0;
        Spinner spinnerEggGroup1 = findViewById(R.id.spinnerEggGroup1);
        Spinner spinnerEggGroup2 = findViewById(R.id.spinnerEggGroup2);
        long offset = Utils.OFFSET_POKE + index * 0x1C;
        eggGroup1 = 0xFF & Utils.ReadByte(filePath,offset + 20);
        eggGroup2 = 0xFF & Utils.ReadByte(filePath,offset + 21);
        if(eggGroup1 > 0x0F || eggGroup2 > 0x0F){
            Toast.makeText(this, warning, Toast.LENGTH_SHORT).show();
            return;
        }
        spinnerEggGroup1.setSelection(eggGroup1);
        spinnerEggGroup2.setSelection(eggGroup2);
    }
    //显示其他
    private void showOther(int index){
        int sexRate = 0,incubationCycle = 0,catchRate = 0;
        EditText txtSexRate = findViewById(R.id.txtSexRate);
        EditText txtIncubationCycle= findViewById(R.id.txtIncubationCycle);
        EditText txtCatchRate = findViewById(R.id.txtCatchRate);
        long offset = Utils.OFFSET_POKE + index * 0x1C;
        sexRate = 0xFF & Utils.ReadByte(filePath,offset + 16);
        incubationCycle = 0xFF & Utils.ReadByte(filePath,offset + 17);
        catchRate = 0xFF & Utils.ReadByte(filePath,offset + 8);
        txtSexRate.setText(sexRate + "");
        txtIncubationCycle.setText(incubationCycle + "");
        txtCatchRate.setText(catchRate + "");
    }
    //写入种族值
    private void writeBreedValue(int index){
        long offset = Utils.OFFSET_POKE + index * 0x1C;
        int bv[] = new int[6];
        String sTxtBv[] = new String[6];
        EditText txtBv[] = new EditText[6];
        txtBv[0] = findViewById(R.id.txtHP);
        txtBv[1] = findViewById(R.id.txtATK);
        txtBv[2] = findViewById(R.id.txtDEF);
        txtBv[3] = findViewById(R.id.txtSPEED);
        txtBv[4] = findViewById(R.id.txtSPATK);
        txtBv[5] = findViewById(R.id.txtSPDEF);
        for(int i=0;i<6;i++) {
            sTxtBv[i] = txtBv[i].getText().toString();
            bv[i] = Integer.parseInt(sTxtBv[i]);
            if(bv[i] > 255 || bv[i] < 0)
                bv[i] = 255;
            Utils.WriteByte(filePath,offset+i,bv[i]);
        }
    }
    //写入属性
    private void writeType(int index){
        long offset = Utils.OFFSET_POKE + index * 0x1C;
        Spinner spinnerType1 = findViewById(R.id.spinnerType1);
        Spinner spinnerType2 = findViewById(R.id.spinnerType2);
        int s1 = spinnerType1.getSelectedItemPosition();
        int s2 = spinnerType2.getSelectedItemPosition();
        Utils.WriteByte(filePath,offset + 6,s1);
        Utils.WriteByte(filePath,offset + 7,s2);
    }
    //写入特性
    private void writeAbility(int index){
        long offset = Utils.OFFSET_POKE + index * 0x1C;
        Spinner spinnerAB1 = findViewById(R.id.spinnerAbility1);
        Spinner spinnerAB2 = findViewById(R.id.spinnerAbility2);
        Spinner spinnerAB3 = findViewById(R.id.spinnerAbility3);
        int ab1 = spinnerAB1.getSelectedItemPosition();
        int ab2 = spinnerAB2.getSelectedItemPosition();
        int ab3 = spinnerAB3.getSelectedItemPosition();
        Utils.WriteByte(filePath,offset + 22,ab1);
        Utils.WriteByte(filePath,offset + 23,ab2);
        Utils.WriteByte(filePath,offset + 26,ab3);
    }
    //写入蛋组
    private void writeEggGroup(int index){
        long offset = Utils.OFFSET_POKE + index * 0x1C;
        Spinner spinnerEggGroup1 = findViewById(R.id.spinnerEggGroup1);
        Spinner spinnerEggGroup2 = findViewById(R.id.spinnerEggGroup2);
        int eggGroup1 = spinnerEggGroup1.getSelectedItemPosition();
        int eggGroup2 = spinnerEggGroup2.getSelectedItemPosition();
        Utils.WriteByte(filePath,offset + 20,eggGroup1);
        Utils.WriteByte(filePath,offset + 21,eggGroup2);
    }
    //写入其他
    private void writeOther(int index){
        long offset = Utils.OFFSET_POKE + index * 0x1C;
        EditText txtSexRate = findViewById(R.id.txtSexRate);
        EditText txtIncubationCycle= findViewById(R.id.txtIncubationCycle);
        EditText txtCatchRate = findViewById(R.id.txtCatchRate);
        String sSexRate = txtSexRate.getText().toString();
        String sIncubationCycle = txtIncubationCycle.getText().toString();
        String sCatchRate = txtCatchRate.getText().toString();
        int sexRate = Integer.parseInt(sSexRate);
        int incubationCycle = Integer.parseInt(sIncubationCycle);
        int catchRate = Integer.parseInt(sCatchRate);
        if(sexRate < 0 || sexRate >255) sexRate = 255;
        if(incubationCycle < 0 || incubationCycle >255) incubationCycle = 255;
        if(catchRate < 0 || catchRate >255) catchRate = 255;
        Utils.WriteByte(filePath,offset + 16,sexRate);
        Utils.WriteByte(filePath,offset + 17,incubationCycle);
        Utils.WriteByte(filePath,offset + 8,catchRate);
    }

    public void on_btnSearchPoke_clicked(View view){
        Spinner spinnerPokemon = findViewById(R.id.spinnerPokemon);
        EditText txtSearchPoke = findViewById(R.id.txtSearchPoke);
        //获取输入法
        InputMethodManager imm =(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        //隐藏输入法
        imm.hideSoftInputFromWindow(txtSearchPoke.getWindowToken(), 0);
        //获取输入框文本
        String sub = txtSearchPoke.getText().toString();
        if("".equals(sub)){
            Toast.makeText(this, "请输入", Toast.LENGTH_SHORT).show();
            return;
        }
        //获取String-array
        //就是 Pokes
        int len = Pokes.length;
        //记录当前位置
        int curr = spinnerPokemon.getSelectedItemPosition();
        for(int i = curr+1;i<len;i++){
            if(Pokes[i].contains(sub)){
                spinnerPokemon.setSelection(i);
                return;
            }
        }
        for(int i=0;i<curr;i++){
            if(Pokes[i].contains(sub)){
                spinnerPokemon.setSelection(i);
                return;
            }
        }
        Toast.makeText(this, "未找到下一处", Toast.LENGTH_SHORT).show();
    }
    public void on_btnSearchSkill_clicked(View view){
        Spinner spinnerSkill = findViewById(R.id.spinnerSkill);
        EditText txtSearchSkill = findViewById(R.id.txtSearchSkill);
        //获取输入法
        InputMethodManager imm =(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        //隐藏输入法
        imm.hideSoftInputFromWindow(txtSearchSkill.getWindowToken(), 0);
        //获取输入框文本
        String sub = txtSearchSkill.getText().toString();
        if("".equals(sub)){
            Toast.makeText(this, "请输入", Toast.LENGTH_SHORT).show();
            return;
        }
        //获取String-array
//        String[] skill = getResources().getStringArray(R.array.skill);
        int len = Skills.length;
        int curr = spinnerSkill.getSelectedItemPosition();
        for(int i = curr+1;i<len;i++){
            if(Skills[i].contains(sub)){
                spinnerSkill.setSelection(i);
                return;
            }
        }
        for(int i=0;i<curr;i++){
            if(Skills[i].contains(sub)){
                spinnerSkill.setSelection(i);
                return;
            }
        }
        Toast.makeText(this, "未找到下一处", Toast.LENGTH_SHORT).show();
    }
    public void on_btnWritePoke_clicked(View view){
        Spinner spinnerPokemon = findViewById(R.id.spinnerPokemon);
        writePokeValue(spinnerPokemon.getSelectedItemPosition());
    }
    public void on_btnRefreshPoke_clicked(View view){
        Spinner spinnerPokemon = findViewById(R.id.spinnerPokemon);
        showPokeValue(spinnerPokemon.getSelectedItemPosition());
    }
    public void on_btnWriteSkill_clicked(View view){
        Spinner spinnerSkill = findViewById(R.id.spinnerSkill);
        writeSkillValue(spinnerSkill.getSelectedItemPosition());
    }
    public void on_btnRefreshSkill_clicked(View view){
        Spinner spinnerSkill = findViewById(R.id.spinnerSkill);
        showSkillValue(spinnerSkill.getSelectedItemPosition());
    }

    //激活
    public void on_btnActive_clicked(View view){
        if(Utils.activeString().equals("true")){
            Toast.makeText(this, "已经激活！\n现在已经不收费了哦！", Toast.LENGTH_SHORT).show();
        }
    }
    //开源
    public void on_btnOpenSource_clicked(View view){
        Intent intent = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.lanzous.com/b720676")
        );
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        Toast.makeText(this, "请用浏览器打开\n如果链接失效请联系QQ1953649096", Toast.LENGTH_SHORT).show();
    }

    //检查更新
    public void on_btnCheckUpdate_clicked(View view){
        Intent intent = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.lanzous.com/b720696")
        );
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        Toast.makeText(this, "请用浏览器打开\n如果发现日期大于当前版本即为更新版本\n下载安装即可，激活码重用", Toast.LENGTH_SHORT).show();
    }


    //获取永久mega版本
    public void on_btnGetMegaVersion_clicked(View view){
        Intent intent = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.lanzous.com/b653829")
        );
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        Toast.makeText(this, "请用浏览器打开\n如果链接失效请联系QQ1953649096", Toast.LENGTH_SHORT).show();
    }

    //使用教程
    public void on_btnHowToUse_clicked(View view){
        Intent intent = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.bilibili.com/video/av52133586")
        );
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        Toast.makeText(this, "请用浏览器打开\n如果链接失效请联系QQ1953649096", Toast.LENGTH_SHORT).show();
    }
}
