package com.webengage.segmentandroidtest;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.segment.analytics.Analytics;
import com.segment.analytics.Options;
import com.segment.analytics.Properties;
import com.segment.analytics.Traits;
import com.webengage.sdk.android.WebEngage;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends Activity implements View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {
    Button login, track, setAttr, logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        login = (Button) findViewById(R.id.login);
        track = (Button) findViewById(R.id.track);
        setAttr = (Button) findViewById(R.id.setAttribute);
        logout = (Button) findViewById(R.id.logout);
        login.setOnClickListener(this);
        track.setOnClickListener(this);
        setAttr.setOnClickListener(this);
        logout.setOnClickListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        Analytics.with(this.getApplicationContext()).screen("MainScreen", new Properties().putValue("abc", 1).putValue("discount", true));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 102);
        }

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login:
                String cuid = UUID.randomUUID().toString();
                Analytics.with(this).identify(cuid);
                Map<String, Object> webEngageOptions = new HashMap<>();
                webEngageOptions.put("we_hashed_email", "123");
                webEngageOptions.put("we_hashed_phone", "321");
                webEngageOptions.put("we_push_opt_in", true);
                webEngageOptions.put("we_sms_opt_in", true);
                webEngageOptions.put("we_email_opt_in", false);
                Analytics.with(this.getApplicationContext()).identify(cuid, null, new Options().setIntegrationOptions("WebEngage", webEngageOptions));
                break;

            case R.id.track:
                Analytics.with(this.getApplicationContext()).track("BigPictureNotification", new Properties().putValue("price", 200), new Options().setIntegration("Mixpanel", true).setIntegration("KISSmetrics", true));

                Analytics.with(this.getApplicationContext()).track("CheckoutStarted", new Properties().putValue("price", 100).putValue("currency", "INR"));
                Analytics.with(this.getApplicationContext()).track("CheckoutCompleted", new Properties().putValue("price", 100).putValue("discount", 50));

                break;


            case R.id.setAttribute:
                Analytics.with(this.getApplicationContext()).identify(new Traits().putFirstName("alexa").putLastName("board").putGender("male").putPhone("8888888888").putAge(24).putEmail("abc@gmail.com").putBirthday(new Date(701682609000L)));
                Analytics.with(this.getApplicationContext()).identify(new Traits().putValue("isA", true).putValue("b", 10).putAddress(new Traits.Address().putCity("Mumbai").putState("Maharashtra").putCountry("India").putPostalCode("400072").putStreet("AK road")));


                break;

            case R.id.logout:

                Analytics.with(this.getApplicationContext()).reset();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 102:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    WebEngage.get().setLocationTracking(true);
                }

                break;
        }
    }
}
