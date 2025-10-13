package com.example.afinal.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.afinal.R;
import com.example.afinal.dbclass.Level;

import java.util.ArrayList;

public class LevelAdapter extends ArrayAdapter<Level> {
    private Activity context;
    private int idlayout;
    private ArrayList<Level> list;

    public LevelAdapter( Activity context, int idlayout, ArrayList<Level> list) {
        super(context, idlayout,list);
        this.context = context;
        this.idlayout = idlayout;
        this.list = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater myflat= context.getLayoutInflater();
        convertView=myflat.inflate(idlayout,null);
        Level level=list.get(position);
        TextView name=convertView.findViewById(R.id.txtLLLlevel_name);
        TextView info=convertView.findViewById(R.id.txtLLLinfo);
        TextView time=convertView.findViewById(R.id.txtLLLtime);
        name.setText(level.getName());
        time.setText("Thời gian: "+level.getTime()+" phút.");
        info.setText("Cần đúng ít nhất: "+level.getMinRequired()+"/"+level.getTotalQuestion()+".");
        return convertView;
    }
}
