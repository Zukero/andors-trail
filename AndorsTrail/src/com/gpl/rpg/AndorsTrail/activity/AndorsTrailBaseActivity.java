package com.gpl.rpg.AndorsTrail.activity;

import android.app.Activity;
import android.os.Bundle;

import com.gpl.rpg.AndorsTrail.AndorsTrailApplication;

public abstract class AndorsTrailBaseActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndorsTrailApplication app = AndorsTrailApplication.getApplicationFromActivity(this);
        app.setLocale(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AndorsTrailApplication app = AndorsTrailApplication.getApplicationFromActivity(this);
        app.setLocale(this);
    }
}

