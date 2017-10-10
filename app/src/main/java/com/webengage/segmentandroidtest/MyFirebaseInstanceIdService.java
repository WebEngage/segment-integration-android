package com.webengage.segmentandroidtest;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.webengage.sdk.android.WebEngage;

/**
 * Created by shahrukhimam on 10/10/17.
 */

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        WebEngage.get().setRegistrationID(FirebaseInstanceId.getInstance().getToken());
    }
}
