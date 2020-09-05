package com.ws.rommodifier;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FileSelectActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {



    ListView lv;
    List<FileInfo> list;
    FileAdapter adapter;
    String currPath;
    String parentPath;
    TextView lblEmptyFolder;
    ImageView picEmptyFolder;
    Comparator<FileInfo> nameComparator = new Comparator<FileInfo>() {
        @Override
        public int compare(FileInfo lhs, FileInfo rhs) {
            return lhs.fileName.toLowerCase().compareTo(rhs.fileName.toLowerCase());
        }
    };

    final String ROOT = Utils.getSDCardPath();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("MyApp","FileSelectActivity 启动");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("选择Rom");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_select);
        init();
        updateData(ROOT);
    }


    private void init() {
        Log.i("MyApp","init()");
        lv = findViewById(R.id.listView);
        adapter = new FileAdapter(this);

        list = Utils.getListData(ROOT);
        adapter.setList(list);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(this);
        lv.setOnItemLongClickListener(this);
    }

    private void updateData(String path){
        Log.i("MyApp","updateData()");
        currPath = path;//记录当前目录
        File file = new File(path);
        parentPath = file.getParent();
        Log.i("MyApp","当前目录 :" + currPath);
        lblEmptyFolder = findViewById(R.id.lblEmptyFolder);
        picEmptyFolder = findViewById(R.id.picEmptyFolder);
        lblEmptyFolder.setVisibility(View.GONE);
        picEmptyFolder.setVisibility(View.GONE);
        list = Utils.getListData(path);
        if (list.size() == 0) {//list没有元素 即 文件夹里面没有文件
//            Toast.makeText(this, "空文件夹", Toast.LENGTH_SHORT).show();
            lblEmptyFolder.setVisibility(View.VISIBLE);
            picEmptyFolder.setVisibility(View.VISIBLE);
        }
        list = groupSort(list);//排序
        adapter.setList(list);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TextView t = view.findViewById(R.id.fileName);
        FileInfo item = (FileInfo)parent.getItemAtPosition(position) ;
        Log.i("MyApp",item.filePath);
        if(item.type == FileInfo.DIR) {//进入文件夹
            updateData(item.filePath);
        }else{
            if(("gba").equals(Utils.getFileExt(item.fileName))){
                Toast.makeText(this, "已选择 " + item.fileName, Toast.LENGTH_SHORT).show();
                //启动修改界面
                Intent intent = new Intent(this,TabActivity.class);
                Bundle bundle = new Bundle();
                bundle.putCharSequence("filePath",item.filePath);
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
            }else if(("zip").equals(Utils.getFileExt(item.fileName))){
                Toast.makeText(this, "压缩包需要解压出来！", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "这个文件不是ROM哦！", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 分组排序
     * @param list
     * @return
     */
    public List<FileInfo> groupSort(List<FileInfo> list){
        //1.文件和文件夹进行分类
        List<FileInfo> dirs = new ArrayList<FileInfo>();
        List<FileInfo> files = new ArrayList<FileInfo>();
        for(FileInfo item:list){
            if(item.type == FileInfo.DIR){
                dirs.add(item);
            }else{
                files.add(item);
            }
        }
        //2.对两个集合分别排序
        Collections.sort(dirs,nameComparator);
        Collections.sort(files,nameComparator);
        //3.两个集合合并
        dirs.addAll(files);
        return dirs;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        TextView t = view.findViewById(R.id.fileName);
        Toast.makeText(this,"长按 " + t.getText(), Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override//返回键
    public void onBackPressed() {
        //返回上级目录
        if(currPath.equals(ROOT)) {
            super.onBackPressed();
        }
        else{
            updateData(parentPath);
        }
    }
}
