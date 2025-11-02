package com.example.afinal.DAO;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


import com.example.afinal.dbclass.Question;

import java.util.ArrayList;
import java.util.HashMap;

public class QuestionDAO extends DAO{
    public QuestionDAO(SQLiteDatabase database) {
        super(database);
    }
    public Question getQuestionFromCursor(Cursor cursor){
        Question x=new Question();
        x.setId(cursor.getInt(0));
        x.setContent(cursor.getString(2));
        x.setImg_url(cursor.getString(3));
        x.setExplain(cursor.getString(5));
        x.setA(cursor.getString(6));
        x.setB(cursor.getString(7));
        x.setC(cursor.getString(8));
        x.setD(cursor.getString(9));
        x.setAnswer(cursor.getString(10));
        x.setIs_critical(cursor.getInt(4));
        return x;
    }
    public ArrayList<Question> getQuestionInRange(int start, int end){
        Cursor cursor = database.query("Questions",null,"question_id BETWEEN ? AND ?",new String[]{String.valueOf(start),String.valueOf(end)},null,null,null);
        ArrayList<Question> arrayList=new ArrayList<>();
        if(cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                arrayList.add(getQuestionFromCursor(cursor));
                cursor.moveToNext();
            }
        }
        cursor.close();
        return arrayList;
    }
    public ArrayList<Question> getCriticalQuestionInRange(int start, int end){
        ArrayList<Question> arrayList=new ArrayList<>();
        Cursor cursor = database.query("Questions",null,"is_critical=?",new String[]{"1"},null,null,"question_id asc",String.valueOf(end));

        if(cursor.moveToFirst()){
            int cnt=1;
            while(true){
                if(cnt==start) break;
                cnt++;
                cursor.moveToNext();
            }
            while(!cursor.isAfterLast()){
                arrayList.add(getQuestionFromCursor(cursor));
                cursor.moveToNext();
            }
        }
        cursor.close();
        return arrayList;
    }
    public ArrayList<Question> getQuestionOfLevel(int levelID){
        ArrayList<Question>arrayList=new ArrayList<>();
        HashMap<Integer,Integer>rule=this.getRule(levelID);
        String sql="SELECT * from(\n" +
                "\tselect * from(\n" +
                "\t\tSELECT * from Questions\n" +
                "\t\twhere is_critical=1\n" +
                "\t\torder by random()\n" +
                "\t\tlimit 1\n" +
                "\t)\n" +
                "\tunion all\n" +
                "\tselect * from(\n" +
                "\t\tSELECT * from Questions\n" +
                "\t\twhere category_id=1 and is_critical=0\n" +
                "\t\torder by random()\n" +
                "\t\tlimit " + rule.getOrDefault(1,1)+
                "\t)\n" +
                "\tunion all\n" +
                "\tselect * from(\n" +
                "\t\tSELECT * from Questions\n" +
                "\t\twhere category_id=2 and is_critical=0\n" +
                "\t\torder by random()\n" +
                "\t\tlimit " + rule.getOrDefault(2,1)+
                "\t)\n" +
                "\tunion all\n" +
                "\tselect * from(\n" +
                "\t\tSELECT * from Questions\n" +
                "\t\twhere category_id=3 and is_critical=0\n" +
                "\t\torder by random()\n" +
                "\t\tlimit " + rule.getOrDefault(3,1)+
                "\t)\n" +
                "\tunion all\n" +
                "\tselect * from(\n" +
                "\t\tSELECT * from Questions\n" +
                "\t\twhere category_id=4 and is_critical=0\n" +
                "\t\torder by random()\n" +
                "\t\tlimit " + rule.getOrDefault(4,1)+
                "\t)\n" +
                "\tunion all\n" +
                "\tselect * from(\n" +
                "\t\tSELECT * from Questions\n" +
                "\t\twhere category_id=5 and is_critical=0\n" +
                "\t\torder by random()\n" +
                "\t\tlimit " + rule.getOrDefault(5,1)+
                "\t)\n" +
                "\tunion all\n" +
                "\tselect * from(\n" +
                "\t\tSELECT * from Questions\n" +
                "\t\twhere category_id=6 and is_critical=0\n" +
                "\t\torder by random()\n" +
                "\t\tlimit " + rule.getOrDefault(6,1)+
                "\t)\n" +
                "\n" +
                ")\n" +
                "order by random()";
        Cursor cursor=database.rawQuery(sql,null);
        if(cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                arrayList.add(getQuestionFromCursor(cursor));
                cursor.moveToNext();
            }
        }
        cursor.close();
        return  arrayList;
    }

}
