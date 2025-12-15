package com.example.afinal;

import com.example.afinal.dbclass.Question;

import java.util.ArrayList;

/**
 * Simple in-memory holder for passing a prepared question list between
 * SmartPracticeActivity and the question activities without changing
 * the existing database-driven constructors.
 *
 * This is process-local and should only be used for short-lived sessions.
 */
public final class QuestionHolder {

    private static ArrayList<Question> questions;

    private QuestionHolder() {}

    public static void setQuestions(ArrayList<Question> list) {
        questions = list;
    }

    public static ArrayList<Question> consumeQuestions() {
        ArrayList<Question> result = questions;
        questions = null;
        return result;
    }
}


