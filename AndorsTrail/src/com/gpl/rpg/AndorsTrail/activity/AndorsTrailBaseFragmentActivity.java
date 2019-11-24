package com.gpl.rpg.AndorsTrail.activity;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.gpl.rpg.AndorsTrail.AndorsTrailApplication;
import com.gpl.rpg.AndorsTrail.util.ThemeHelper;

public abstract class AndorsTrailBaseFragmentActivity extends FragmentActivity {
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
