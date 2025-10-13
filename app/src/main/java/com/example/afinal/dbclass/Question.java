package com.example.afinal.dbclass;

public class Question {
    private int id,topic_id,is_critical;
    private String content,img_url,A,B,C,Answer;

    public Question() {
    }

    public Question(int id, int topic_id, int is_critical, String content, String img_url, String a, String b, String c, String answer) {
        this.id = id;
        this.topic_id = topic_id;
        this.is_critical = is_critical;
        this.content = content;
        this.img_url = img_url;
        A = a;
        B = b;
        C = c;
        Answer = answer;
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

    public String getA() {
        return A;
    }

    public void setA(String a) {
        A = a;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
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

    public String getAnswer() {
        return Answer;
    }

    public void setAnswer(String answer) {
        Answer = answer;
    }

}
