package com.ws.rommodifier;

public class FileInfo {
    public static final int DIR = 0;
    public static final int FILE = 1;
    //需要显示
    public int icon;
    public String fileName;
    public String fileSize;
    public String fileTime;
    //其他
    public String filePath;
    public int type;

    @Override
    public String toString() {
        return "\nFileInfo{" +
                "icon=" + icon +
                ", fileName='" + fileName + '\'' +
                ", fileSize='" + fileSize + '\'' +
                ", fileTime='" + fileTime + '\'' +
                ", filePath='" + filePath + '\'' +
                '}';
    }
}
