package com.webengage.segmentandroidtest;

import android.app.Application;

import com.segment.analytics.Analytics;
import com.webengage.sdk.android.integrations.segment.WebEngageIntegration;


public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Analytics analytics = new Analytics.Builder(this, "UQNe5yhVnTtyVi6RGRFAlvG9WFEVagA6")
                .trackApplicationLifecycleEvents() // Enable this to record certain application events automatically!
                .logLevel(Analytics.LogLevel.VERBOSE)
                .use(WebEngageIntegration.FACTORY)
                .build();
        Analytics.setSingletonInstance(analytics);
    }
}
