package com.thermostat.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MyAdapter2 extends BaseAdapter {
    private LayoutInflater inflater;  
    private String[] data;
    private Context context;
    private int cur_pos = 0;
  
    public MyAdapter2(Context context, int cur_pos, String[] data) {  
        inflater = (LayoutInflater) context  
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
        this.context = context;
        this.cur_pos = cur_pos;
        this.data = data;
    }  

    @Override  
    public int getCount() {  
        return data.length;  
    }  

    @Override  
    public Object getItem(int position) {  
        return data[position];  
    }  

    @Override  
    public long getItemId(int position) {  
        return position;  
    }  

    @Override  
    public View getView(int position, View convertView, ViewGroup parent) {  
        convertView = inflater.inflate(R.layout.list_item, null, false);  
        TextView tv = (TextView) convertView  
                .findViewById(R.id.tv_item);
        tv.setText(data[position]);  
        return convertView;  
    }  
    
    public void updateCurPos(int position)
    {
    	cur_pos = position;
    }
    
}
