package com.ws.rommodifier;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class FileAdapter extends BaseAdapter {

    List<FileInfo> list;
    LayoutInflater inflater;

    public class ViewHolder{
        ImageView icon ;
        TextView name;
        TextView size;
        TextView time;
    }

    public FileAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
    }

    public void setList(List<FileInfo> list) {
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        if(convertView == null) {
            convertView = inflater.inflate(R.layout.file_item, null);
            holder = new ViewHolder();
            holder.icon = convertView.findViewById(R.id.fileIcon);
            holder.name = convertView.findViewById(R.id.fileName);
            holder.size = convertView.findViewById(R.id.fileSize);
            holder.time = convertView.findViewById(R.id.fileTime);
            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder)convertView.getTag();
        }

        FileInfo fileInfo = list.get(position);
        holder.icon.setImageResource(fileInfo.icon);
        holder.name.setText(fileInfo.fileName);
        holder.size.setText(fileInfo.fileSize);
        holder.time.setText(fileInfo.fileTime);

        return convertView;
    }
}
