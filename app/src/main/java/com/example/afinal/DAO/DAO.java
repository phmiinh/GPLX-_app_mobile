package com.example.afinal.DAO;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.HashMap;

public class DAO {
    protected SQLiteDatabase database=null;

    public DAO(SQLiteDatabase database) {
        this.database=database;
    }
    protected HashMap<Integer,Integer> getRule(int level){
        HashMap<Integer,Integer>rule=new HashMap<>();
        Cursor cursor1=database.query("rules_exam",null,"level_id=?",new String[]{String.valueOf(level)},null,null,null);
        if(cursor1.moveToFirst()){
            while (!cursor1.isAfterLast()){
                rule.put(cursor1.getInt(1),cursor1.getInt(2));
                cursor1.moveToNext();
            }
        }
        cursor1.close();
        return rule;
    }
}
