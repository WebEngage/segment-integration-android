package com.webengage.segmentandroidtest;

import android.app.Application;
import android.content.Context;

import com.segment.analytics.Analytics;
import com.webengage.sdk.android.WebEngageConfig;
import com.webengage.sdk.android.actions.render.PushNotificationData;
import com.webengage.sdk.android.callbacks.PushNotificationCallbacks;
import com.webengage.sdk.android.integrations.segment.WebEngageIntegration;


public class MyApplication extends Application implements PushNotificationCallbacks {

    @Override
    public void onCreate() {
        super.onCreate();
        WebEngageConfig webEngageConfig = new WebEngageConfig.Builder()
                .setDebugMode(true)
                .setPushLargeIcon(R.mipmap.ic_launcher)
                .setPushSmallIcon(R.mipmap.ic_launcher_round)
                .build();
        Analytics analytics = new Analytics.Builder(this, "SEGMENT_WRITE_KEY")
                .trackApplicationLifecycleEvents() // Enable this to record certain application events automatically!
                .logLevel(Analytics.LogLevel.VERBOSE)
                .use(WebEngageIntegration.FACTORY.withWebEngageConfig(webEngageConfig))
                .build();
        Analytics.setSingletonInstance(analytics);
    }

    @Override
    public PushNotificationData onPushNotificationReceived(Context context, PushNotificationData pushNotificationData) {
        pushNotificationData.getBigPictureStyleData().getSummary();
        return null;
    }

    @Override
    public void onPushNotificationShown(Context context, PushNotificationData pushNotificationData) {

    }

    @Override
    public boolean onPushNotificationClicked(Context context, PushNotificationData pushNotificationData) {
        return false;
    }

    @Override
    public void onPushNotificationDismissed(Context context, PushNotificationData pushNotificationData) {

    }

    @Override
    public boolean onPushNotificationActionClicked(Context context, PushNotificationData pushNotificationData, String buttonId) {
        return false;
    }
}
