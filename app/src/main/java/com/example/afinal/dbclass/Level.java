package com.example.afinal.dbclass;
import java.lang.String;
public class Level {
    private int level_id;
    private String name;
    private int totalQuestion,minRequired,time;



    public Level(int level_id, String name, int totalQuestion, int minRequired, int time) {
        this.level_id = level_id;
        this.name = name;
        this.totalQuestion = totalQuestion;
        this.minRequired = minRequired;
        this.time = time;
    }


    public int getLevel_id() {
        return level_id;
    }

    public void setLevel_id(int level_id) {
        this.level_id = level_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMinRequired() {
        return minRequired;
    }

    public void setMinRequired(int minRequired) {
        this.minRequired = minRequired;
    }

    public int getTotalQuestion() {
        return totalQuestion;
    }

    public void setTotalQuestion(int totalQuestion) {
        this.totalQuestion = totalQuestion;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}
