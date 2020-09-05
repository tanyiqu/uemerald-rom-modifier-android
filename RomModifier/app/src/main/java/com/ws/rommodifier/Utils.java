package com.ws.rommodifier;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Utils {
    public static final String INI_FILE_PATH = "/data/data/com.ws.rommodifier/info.ini";
    public static long OFFSET_POKE;     //宝可梦偏移
    public static long OFFSET_SKILL;    //技能偏移

    /**
     * 获取设备SD卡的根目录
     * @return
     */
    public static String getSDCardPath(){
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    /**
     * 把字节数B转化为KB、MB、GB
     * @param size
     * @return
     */
    public static String byteToSize(long size){
        //如果字节数少于1024，则直接以B为单位，否则先除于1024，后3位因太少无意义
        if (size < 1024) {
            return String.valueOf(size) + "B";
        } else {
            size = size / 1024;
        }
        //如果原字节数除于1024之后，少于1024，则可以直接以KB作为单位
        //因为还没有到达要使用另一个单位的时候
        //接下去以此类推
        if (size < 1024) {
            return String.valueOf(size) + "KB";
        } else {
            size = size / 1024;
        }
        if (size < 1024) {
            //因为如果以MB为单位的话，要保留最后1位小数，
            //因此，把此数乘以100之后再取余
            size = size * 100;
            return String.valueOf((size / 100)) + "."
                    + String.valueOf((size % 100)) + "MB";
        } else {
            //否则如果要以GB为单位的，先除于1024再作同样的处理
            size = size * 100 / 1024;
            return String.valueOf((size / 100)) + "."
                    + String.valueOf((size % 100)) + "GB";
        }
    }

    /**
     * 列出path 的所有文件
     * @param path
     * @return
     */
    public static List<FileInfo> getListData(String path){
        List<FileInfo> list = new ArrayList<FileInfo>();
        File pfile = new File(path);// 文件对象
        File[] files = null;// 声明了一个文件对象数组
        FileInfo fileInfo = null;
        if (pfile.exists()) {// 判断路径是否存在
            Log.i("MyApp",path + " 目录存在");
            files = pfile.listFiles();// 该文件对象下所属的所有文件和文件夹列表
        }else{
            Log.i("MyApp", path + " 目录不存在");
        }

        if (files != null && files.length > 0) {
            Log.i("MyApp",path + " 目录不空");
            for (File f : files) {// foreach循环遍历
                if(!f.canRead() || f.isHidden()){
                    continue;// 跳过隐藏文件
                }
                fileInfo = new FileInfo();
                if(f.isDirectory()) fileInfo.type = FileInfo.DIR;
                else fileInfo.type = FileInfo.FILE;
                switch (fileInfo.type){
                    case FileInfo.DIR:  //目录
                        //图标
                        fileInfo.icon = R.drawable.dir;
                        //大小
                        fileInfo.fileSize = "文件夹";
                        break;
                    case FileInfo.FILE: //文件
                        if(("gba").equals(getFileExt(f.getName())))
                            fileInfo.icon = R.drawable.gba_file;
                        else
                            fileInfo.icon = R.drawable.file;
                        fileInfo.fileSize = Utils.byteToSize(f.length());
                        break;
                }
                //其他共有
                //文件名
                fileInfo.fileName = f.getName();
                //最后修改时间
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date data = new Date(f.lastModified());
                fileInfo.fileTime = sdf.format(data);
                //路径
                fileInfo.filePath = f.getPath();

                //添加进列表
                list.add(fileInfo);
            }
        }
        else{
            Log.i("MyApp",path + " 目录为空");
        }
        if(list.size() == 0){
            Log.i("MyApp",path + " 为空文件夹");
        }
        return list;
    }

    /**
     * 获取文件的扩展名（小写）
     * @param fileName
     * @return
     */
    public static String getFileExt(String fileName){
        String ext = "";
        int pos = fileName.lastIndexOf(".");
        ext = fileName.substring(pos+1,fileName.length());
        return ext.toLowerCase();
    }

    /**
     * 同步ini文件的路径
     * @param filePath
     */
    public static void syncIniFile(String filePath) {
        String isActive = "false";
        try{
            File file = new File(Utils.INI_FILE_PATH);
            //保存数据
            FileReader reader = new FileReader(file);
            BufferedReader br = new BufferedReader(reader);
            br.readLine();br.readLine();br.readLine();
            isActive = br.readLine();
            br.close();
            //写入数据
            FileWriter writer = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(writer);
            bw.write("[last_open]");bw.newLine();
            bw.write(filePath);bw.newLine();
            bw.write("[is_active]");bw.newLine();
            bw.write(isActive);
            bw.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * 从读取ROM的偏移
     * @param filePath
     */
    public static void setRomOffset(String filePath){
        OFFSET_POKE = 0xF186E0;
        //获取技能的地址
        byte[] addr = new byte[4];
        addr[0] = Utils.ReadByte(filePath, 0x1cc);
        addr[1] = Utils.ReadByte(filePath, 0x1cd);
        addr[2] = Utils.ReadByte(filePath, 0x1ce);
        addr[3] = 0x01;
        OFFSET_SKILL = addr[0] & 0xFF | (addr[1] & 0xFF) << 8 | (addr[2] & 0xFF) << 16 | (addr[3] & 0xFF) << 24;
    }

    /**
     * 返回filePath文件 第offset位置的1字节的数值
     * @param filePath
     * @param offset
     * @return
     */
    public static byte ReadByte(String filePath,long offset) {
        byte[] buf = new byte[] {(byte)0xFF};
        File file = new File(filePath);
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(file, "r");
            raf.seek(offset);
            raf.read(buf,0,1);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("文件不存在");
        }
        catch (IOException e) {
            e.printStackTrace();
            System.out.println("Seek失败");
        }
        finally {
            try {
                raf.close();
            }
            catch (IOException e) {
                e.printStackTrace();
                System.out.println("关闭输入流失败");
            }
        }
        return buf[0];
    }

    /**
     * 将value的低8位覆盖写入文件filePath的offset偏移中
     * @param filePath
     * @param offset
     * @param value
     */
    public static void WriteByte(String filePath,long offset,int value) {

        //构造缓冲区
        byte[] buf = new byte[] {(byte)value};
        //建立文件联系
        File file = new File(filePath);
        //声明随机读写流
        RandomAccessFile raf = null;
        try {
            //实例化raf
            raf = new RandomAccessFile(file,"rw");
            raf.seek(offset);
            raf.write(buf, 0, 1);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("文件不存在");
        }
        catch (IOException e) {
            e.printStackTrace();
            System.out.println("Seek失败");
        }
        finally {
            try {
                raf.close();
            }
            catch (IOException e) {
                e.printStackTrace();
                System.out.println("输出流打开失败");
            }
        }
    }

    /**
     * 根据ini文件判断是否激活
     * @return
     */
    public static String activeString(){
        File file = new File(INI_FILE_PATH);
        try {
            FileReader reader = new FileReader(file);
            BufferedReader br = new BufferedReader(reader);
            br.readLine();br.readLine();br.readLine();
            String isActive = br.readLine();
            br.close();
            return isActive;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "false";
    }


    /**
     * 提示未激活
     * @param context
     */
    public static void toastToActive(Context context){
        Toast.makeText(context, "未激活版本只能修改属性作为测试\n请检查属性是否修改成功", Toast.LENGTH_SHORT).show();
    }





}

