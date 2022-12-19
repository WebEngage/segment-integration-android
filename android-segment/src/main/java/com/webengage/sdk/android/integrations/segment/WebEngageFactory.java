package com.webengage.sdk.android.integrations.segment;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;

import com.segment.analytics.Analytics;
import com.segment.analytics.ValueMap;
import com.segment.analytics.integrations.Integration;
import com.segment.analytics.integrations.Logger;
import com.segment.analytics.internal.Utils;
import com.webengage.sdk.android.WebEngage;
import com.webengage.sdk.android.WebEngageConfig;
import com.webengage.sdk.android.utils.WebEngageConstant;

public class WebEngageFactory implements Integration.Factory {

    private WebEngageConfig webEngageConfig = null;


    public WebEngageFactory() {

    }
    public WebEngageFactory(WebEngageConfig webEngageConfig) {
        this.webEngageConfig = webEngageConfig;
    }

    @Override
    public Integration<?> create(ValueMap settings, Analytics analytics) {
        Logger logger = analytics.logger(WebEngageIntegration.KEY);
        String licenseCode = settings.getString(WebEngageIntegration.LICENSE_CODE_KEY);
        Bundle applicationBundle = getApplicationMetaData(analytics.getApplication());
        String overrideLC = "";
        if (null != applicationBundle) {
            if (applicationBundle.containsKey("com.webengage.sdk.android.key")) {
                overrideLC = applicationBundle.getString("com.webengage.sdk.android.key");
            }
        }
        if (TextUtils.isEmpty(overrideLC)) {
            if (Utils.isNullOrEmpty(licenseCode)) {
                logger.info("Unable to initialize WebEngage through Segment Integration, Reason: license code is null");
                return null;
            }
        } else {
            logger.info("WebEngage is getting initialised with an overridden LC, " +
                    "received from analytics: " + licenseCode + " but configured LC " + overrideLC +". If you wish to use "
                    + licenseCode + " from analytics, kindly remove `com.webengage.sdk.android.key` from AndroidManifest.xml");
            licenseCode = overrideLC;
        }
        WebEngageConfig.Builder builder = null;
        if(webEngageConfig != null) {
            builder = webEngageConfig.getCurrentState();
        } else {
            builder = new WebEngageConfig.Builder();
        }

        WebEngageConfig mergedConfig = builder.setWebEngageKey(licenseCode).build();
        logger.verbose("Started WebEngage SDK initialization through Segment Integration, license code: %s", licenseCode);
        WebEngage.engage(analytics.getApplication(), mergedConfig);
        return new WebEngageIntegration(logger);
    }

    @Override
    public String key() {
        return WebEngageIntegration.KEY;
    }

    public WebEngageFactory withWebEngageConfig(WebEngageConfig webEngageConfig) {
        this.webEngageConfig = webEngageConfig;
        return this;
    }

    private Bundle getApplicationMetaData(Context context) {
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;

            return bundle;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
