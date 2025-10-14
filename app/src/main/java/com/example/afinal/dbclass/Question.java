package com.example.afinal.dbclass;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Question {
    private int id,topic_id,is_critical;
    private String content,img_url,explain,A,B,C,D,Answer,UserChoice;

    public Question() {
    }


    public Question(int id, int topic_id, int is_critical, String content, String img_url, String explain, String a, String b, String c, String d, String answer, String userChoice) {
        this.id = id;
        this.topic_id = topic_id;
        this.is_critical = is_critical;
        this.content = content;
        this.img_url = img_url;
        this.explain = explain;
        A = a;
        B = b;
        C = c;
        D = d;
        Answer = answer;
        UserChoice = userChoice;
    }

    public String getExplain() {
        return explain;
    }

    public void setExplain(String explain) {
        this.explain = explain;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTopic_id() {
        return topic_id;
    }

    public void setTopic_id(int topic_id) {
        this.topic_id = topic_id;
    }

    public int getIs_critical() {
        return is_critical;
    }

    public void setIs_critical(int is_critical) {
        this.is_critical = is_critical;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public String getA() {
        return A;
    }

    public void setA(String a) {
        A = a;
    }

    public String getB() {
        return B;
    }

    public void setB(String b) {
        B = b;
    }

    public String getC() {
        return C;
    }

    public void setC(String c) {
        C = c;
    }

    public String getD() {
        return D;
    }

    public void setD(String d) {
        D = d;
    }

    public String getAnswer() {
        return Answer;
    }

    public void setAnswer(String answer) {
        Answer = answer;
    }

    public String getUserChoice() {
        return UserChoice;
    }

    public void setUserChoice(String userChoice) {
        UserChoice = userChoice;
    }
}
