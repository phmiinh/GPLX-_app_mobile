package com.example.afinal.analytics;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.UUID;

public class UserIdentity {
    private static final String PREFS = "analytics_prefs";
    private static final String KEY_ANON_ID = "anon_user_id";

    public static String getUserId(Context context) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return user.getUid();
        }
        SharedPreferences sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String id = sp.getString(KEY_ANON_ID, null);
        if (id == null) {
            id = UUID.randomUUID().toString();
            sp.edit().putString(KEY_ANON_ID, id).apply();
        }
        return id;
    }
}


