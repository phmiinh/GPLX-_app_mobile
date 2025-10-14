package com.example.afinal.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.afinal.QuestionActivityLast;
import com.example.afinal.R;
import com.example.afinal.dbclass.Question;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class QuestionAdapter extends ArrayAdapter<Question> {
    private  Activity context;
    private int idlayout;
    private ArrayList<Question> list;

    public QuestionAdapter(Activity context, int idlayout, ArrayList<Question> list) {
        super(context,idlayout, list);
        this.context = context;
        this.idlayout = idlayout;
        this.list = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater myflat=context.getLayoutInflater();
        convertView =myflat.inflate(idlayout,null);
        Question question=list.get(position);
        TextView a,b,c,d,content,explain;
        a=convertView.findViewById(R.id.txtLLRa);
        b=convertView.findViewById(R.id.txtLLRb);
        c=convertView.findViewById(R.id.txtLLRc);
        d=convertView.findViewById(R.id.txtLLRd);
        content=convertView.findViewById(R.id.txtLLRcontent);
        explain=convertView.findViewById(R.id.txtLLRexplain);
        ImageView img=convertView.findViewById(R.id.imgLLR);
        if(question.getC()==null) c.setVisibility(View.GONE);
        else {
            c.setVisibility(View.VISIBLE);
            c.setText("C. "+question.getC());
        }
        if(question.getD()==null) d.setVisibility(View.GONE);
        else {
            d.setVisibility(View.VISIBLE);
            d.setText("D. "+question.getD());
        }
        a.setText("A. "+question.getA());
        b.setText("B. "+question.getB());
        content.setText("Câu "+question.getId()+": "+question.getContent());
        explain.setText("\n Giải thích: "+question.getExplain());
        String img_url="";
        if(question.getImg_url()!=null){
            img.setVisibility(View.VISIBLE);
            img_url="img/"+question.getImg_url()+".png";
            try{
                InputStream inputStream=context.getAssets().open(img_url);
                Drawable drawable=Drawable.createFromStream(inputStream,null);
                inputStream.close();
                img.setImageDrawable(drawable);
            }
            catch (IOException e){
                Toast.makeText(context, "Không thể tải ảnh", Toast.LENGTH_SHORT).show();
            }
        }
        else img.setVisibility(View.GONE);
        String ans=question.getAnswer();

        if(ans.equals(question.getA())){
            a.setBackground(context.getDrawable(R.drawable.bg_true));
        }
        else if(ans.equals(question.getB())){
            b.setBackground(context.getDrawable(R.drawable.bg_true));
        }
        if(ans.equals(question.getC())){
            c.setBackground(context.getDrawable(R.drawable.bg_true));
        }
        if(ans.equals(question.getD())){
            d.setBackground(context.getDrawable(R.drawable.bg_true));
        }
        if(!ans.equals(question.getUserChoice())){
            if(question.getUserChoice().equals(question.getA())){
                a.setBackground(context.getDrawable(R.drawable.bg_false));
            }
            else if(question.getUserChoice().equals(question.getB())){
                b.setBackground(context.getDrawable(R.drawable.bg_false));
            }
            if(question.getUserChoice().equals(question.getC())){
                c.setBackground(context.getDrawable(R.drawable.bg_false));
            }
            if(question.getUserChoice().equals(question.getD())){
                d.setBackground(context.getDrawable(R.drawable.bg_false));
            }
        }
        return convertView;
    }
}
