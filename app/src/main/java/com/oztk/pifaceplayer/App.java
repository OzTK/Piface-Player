package com.oztk.pifaceplayer;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseInstallation;

import java.util.Properties;

/**
 * @author Paul Duguet
 * @version 1
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Parse.initialize(this, BuildConfig.PARSE_API_KEY, BuildConfig.PARSE_API_SECRET);
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }
}
