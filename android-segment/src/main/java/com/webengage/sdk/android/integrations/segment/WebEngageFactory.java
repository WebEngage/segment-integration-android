package com.webengage.sdk.android.integrations.segment;

import com.segment.analytics.Analytics;
import com.segment.analytics.ValueMap;
import com.segment.analytics.integrations.Integration;
import com.segment.analytics.integrations.Logger;
import com.segment.analytics.internal.Utils;
import com.webengage.sdk.android.WebEngage;
import com.webengage.sdk.android.WebEngageConfig;

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
        if (Utils.isNullOrEmpty(licenseCode)) {
            logger.info("Unable to initialize WebEngage through Segment Integration, Reason: license code is null");
            return null;
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
}
