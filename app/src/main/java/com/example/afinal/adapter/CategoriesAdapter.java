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
import com.example.afinal.dbclass.Categories;

import java.util.ArrayList;

public class CategoriesAdapter extends ArrayAdapter<Categories> {
    Activity context;
    int idlayout;
    ArrayList<Categories> list;

    public CategoriesAdapter(Activity context, int idlayout, ArrayList<Categories> list) {
        super(context, idlayout,list);
        this.context = context;
        this.idlayout = idlayout;
        this.list = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater myflat=context.getLayoutInflater();
        convertView=myflat.inflate(idlayout,null);
        Categories categories=list.get(position);
        TextView name=convertView.findViewById(R.id.txtTopic_name);
        TextView num=convertView.findViewById(R.id.txtNum_question);
        name.setText(categories.getName());
        num.setText("Số lượng câu hỏi: "+String.valueOf(categories.getNum()));
        return convertView;
    }
}
