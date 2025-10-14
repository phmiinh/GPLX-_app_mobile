package com.example.afinal;

import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class AnswerColorHelper {
    
    public static void showAnswerWithColors(RadioButton a, RadioButton b, RadioButton c, RadioButton d, 
                                          RadioGroup radioGroup, String correctAnswer) {
        // Reset all answers to normal state first
        resetAnswerColors(a, b, c, d);
        
        // Get the selected answer
        int selectedId = radioGroup.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = radioGroup.findViewById(selectedId);
        String selectedAnswer = "";
        if (selectedRadioButton != null) {
            selectedAnswer = selectedRadioButton.getText().toString();
        }
        
        // Set colors based on correctness
        if (a.getText().toString().equals(correctAnswer)) {
            // Option A is correct
            a.setBackgroundResource(R.drawable.answer_correct);
            a.setTextColor(a.getContext().getResources().getColor(R.color.answer_text_correct));
        } else if (a.getText().toString().equals(selectedAnswer)) {
            // Option A is selected but incorrect
            a.setBackgroundResource(R.drawable.answer_incorrect);
            a.setTextColor(a.getContext().getResources().getColor(R.color.answer_text_incorrect));
        }
        
        if (b.getText().toString().equals(correctAnswer)) {
            // Option B is correct
            b.setBackgroundResource(R.drawable.answer_correct);
            b.setTextColor(b.getContext().getResources().getColor(R.color.answer_text_correct));
        } else if (b.getText().toString().equals(selectedAnswer)) {
            // Option B is selected but incorrect
            b.setBackgroundResource(R.drawable.answer_incorrect);
            b.setTextColor(b.getContext().getResources().getColor(R.color.answer_text_incorrect));
        }
        
        if (c.getVisibility() == View.VISIBLE) {
            if (c.getText().toString().equals(correctAnswer)) {
                // Option C is correct
                c.setBackgroundResource(R.drawable.answer_correct);
                c.setTextColor(c.getContext().getResources().getColor(R.color.answer_text_correct));
            } else if (c.getText().toString().equals(selectedAnswer)) {
                // Option C is selected but incorrect
                c.setBackgroundResource(R.drawable.answer_incorrect);
                c.setTextColor(c.getContext().getResources().getColor(R.color.answer_text_incorrect));
            }
        }
        
        if (d.getVisibility() == View.VISIBLE) {
            if (d.getText().toString().equals(correctAnswer)) {
                // Option D is correct
                d.setBackgroundResource(R.drawable.answer_correct);
                d.setTextColor(d.getContext().getResources().getColor(R.color.answer_text_correct));
            } else if (d.getText().toString().equals(selectedAnswer)) {
                // Option D is selected but incorrect
                d.setBackgroundResource(R.drawable.answer_incorrect);
                d.setTextColor(d.getContext().getResources().getColor(R.color.answer_text_incorrect));
            }
        }
    }
    
    public static void resetAnswerColors(RadioButton a, RadioButton b, RadioButton c, RadioButton d) {
        // Reset all radio buttons to normal state
        a.setBackgroundResource(R.drawable.answer_state);
        a.setTextColor(a.getContext().getResources().getColor(R.color.text_primary));
        
        b.setBackgroundResource(R.drawable.answer_state);
        b.setTextColor(b.getContext().getResources().getColor(R.color.text_primary));
        
        if (c.getVisibility() == View.VISIBLE) {
            c.setBackgroundResource(R.drawable.answer_state);
            c.setTextColor(c.getContext().getResources().getColor(R.color.text_primary));
        }
        
        if (d.getVisibility() == View.VISIBLE) {
            d.setBackgroundResource(R.drawable.answer_state);
            d.setTextColor(d.getContext().getResources().getColor(R.color.text_primary));
        }
    }
}


