package com.example.afinal.DAO;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


import com.example.afinal.dbclass.Categories;
import com.example.afinal.dbclass.Level;

import java.util.ArrayList;

public class CategoriesDAO extends DAO{
    public CategoriesDAO(SQLiteDatabase database) {
        super(database);
    }

    public  ArrayList<Level> getAllLevel(){
        ArrayList<Level> arrayList=new ArrayList<>();
        Cursor cursor = database.query("level",null,null,null,null,null,null);
        if(cursor.moveToFirst()){
            while (!cursor.isAfterLast()){
                arrayList.add(new Level(cursor.getInt(0),cursor.getString(1),
                        cursor.getInt(2),cursor.getInt(3),cursor.getInt(4)));
                cursor.moveToNext();
            }
        }
        cursor.close();
        return arrayList;
    }
    public  ArrayList<Categories> getAllCategories(){
        ArrayList<Categories> arrayList=new ArrayList<>();
        Cursor cursor = database.query("categories",null,null,null,null,null,null);
        if(cursor.moveToFirst()) {
            while (!cursor.isAfterLast()){
                arrayList.add(new Categories(cursor.getInt(0),
                        cursor.getString(1),cursor.getInt(2),cursor.getInt(3),cursor.getInt(4)));
                cursor.moveToNext();
            }

        }
        return  arrayList;
    }
}
