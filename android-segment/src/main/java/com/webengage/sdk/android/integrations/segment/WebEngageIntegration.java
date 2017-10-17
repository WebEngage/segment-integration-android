package com.webengage.sdk.android.integrations.segment;

import android.app.Activity;

import com.segment.analytics.Analytics;
import com.segment.analytics.Traits;
import com.segment.analytics.ValueMap;
import com.segment.analytics.integrations.IdentifyPayload;
import com.segment.analytics.integrations.Integration;
import com.segment.analytics.integrations.Logger;
import com.segment.analytics.integrations.ScreenPayload;
import com.segment.analytics.integrations.TrackPayload;
import com.segment.analytics.internal.Utils;
import com.webengage.sdk.android.Channel;
import com.webengage.sdk.android.UserProfile;
import com.webengage.sdk.android.WebEngage;
import com.webengage.sdk.android.WebEngageConfig;
import com.webengage.sdk.android.utils.Gender;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;


public class WebEngageIntegration extends Integration<WebEngage> {


    private static final String KEY = "WebEngage";
    private static final String BIRTHDAY_KEY = "birthday";
    private static final String FIRST_NAME_KEY = "firstName";
    private static final String GENDER_KEY = "gender";
    private static final String LAST_NAME_KEY = "lastName";
    private static final String PHONE_KEY = "phone";
    private static final String EMAIL_KEY = "email";
    private static final String INDUSTRY_KEY = "industry";
    private static final String NAME_KEY = "name";
    private static final String ADDRESS_KEY = "address";
    private static final String USER_ID_KEY = "userId";
    private static final String ANONYMOUS_ID_KEY = "anonymousId";


    private static final String HASHED_EMAIL_KEY = "we_hashed_email";
    private static final String HASHED_PHONE_KEY = "we_hashed_phone";
    private static final String PUSH_OPT_IN_KEY = "we_push_opt_in";
    private static final String SMS_OPT_IN_KEY = "we_sms_opt_in";
    private static final String EMAIL_OPT_IN_KEY = "we_email_opt_in";

    private static final String LICENSE_CODE_KEY = "licenseCode";
    private Logger segmentLogger;


    public static final Factory FACTORY = new Factory() {
        @Override
        public Integration<?> create(ValueMap settings, Analytics analytics) {
            Logger logger = analytics.logger(KEY);
            String licenseCode = settings.getString(LICENSE_CODE_KEY);
            if (Utils.isNullOrEmpty(licenseCode)) {
                logger.info("Unable to initialize WebEngage through Segment Integration, Reason: license code is null");
                return null;
            }
            WebEngageConfig webEngageConfig = new WebEngageConfig.Builder()
                    .setWebEngageKey(licenseCode)
                    .build();
            logger.verbose("Started WebEngage SDK initialization through Segment Integration, license code: %s", licenseCode);
            WebEngage.engage(analytics.getApplication(), webEngageConfig);
            return new WebEngageIntegration(logger);
        }

        @Override
        public String key() {
            return KEY;
        }
    };

    WebEngageIntegration(Logger logger) {
        this.segmentLogger = logger;
    }


    @Override
    public void onActivityStarted(Activity activity) {
        super.onActivityStarted(activity);
        WebEngage.get().analytics().start(activity);
        segmentLogger.verbose("WebEngage.get().analytics().start(%s)", activity.getClass().getCanonicalName());
    }


    @Override
    public void onActivityStopped(Activity activity) {
        super.onActivityStopped(activity);
        WebEngage.get().analytics().stop(activity);
        segmentLogger.verbose("WebEngage.get().analytics().stop(%s)", activity.getClass().getCanonicalName());
    }

    @Override
    public void identify(IdentifyPayload identify) {
        super.identify(identify);
        Map<String, Object> traits = new HashMap<>(identify.traits());
        traits.remove(ANONYMOUS_ID_KEY);
        if (identify.userId() != null) {
            WebEngage.get().user().login(identify.userId());
            segmentLogger.verbose("WebEngage.get().user().login(%s)", identify.userId());
            traits.remove(USER_ID_KEY);
        }

        UserProfile.Builder userProfileBuilder = newUserProfileBuilder();
        Object birthDateObj = traits.get(BIRTHDAY_KEY);
        if (birthDateObj != null) {
            Date birthDate = null;
            try {
                birthDate = Utils.toISO8601Date((String) birthDateObj);
            } catch (Exception e) {
                segmentLogger.error(e, "Unable to parse birth date %s to date object", birthDateObj);
            }
            if (birthDate != null) {
                Calendar gregorianCalendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
                gregorianCalendar.setTime(birthDate);
                userProfileBuilder.setBirthDate(gregorianCalendar.get(Calendar.YEAR), gregorianCalendar.get(Calendar.MONTH) + 1, gregorianCalendar.get(Calendar.DAY_OF_MONTH));
                traits.remove(BIRTHDAY_KEY);
            }
        }
        if (traits.get(FIRST_NAME_KEY) == null && traits.get(LAST_NAME_KEY) == null) {
            String name = (String) traits.get(NAME_KEY);
            if (name != null) {
                String[] components = name.split(" ");
                userProfileBuilder.setFirstName(components[0]);
                if (components.length > 1) {
                    userProfileBuilder.setLastName(components[components.length - 1]);
                }
                traits.remove(NAME_KEY);
            }
        }
        if (traits.get(FIRST_NAME_KEY) != null) {
            userProfileBuilder.setFirstName((String) traits.get(FIRST_NAME_KEY));
            traits.remove(FIRST_NAME_KEY);
        }
        if (traits.get(LAST_NAME_KEY) != null) {
            userProfileBuilder.setLastName((String) traits.get(LAST_NAME_KEY));
            traits.remove(LAST_NAME_KEY);
        }
        if (traits.get(INDUSTRY_KEY) != null) {
            userProfileBuilder.setCompany((String) traits.get(INDUSTRY_KEY));
            traits.remove(INDUSTRY_KEY);
        }
        if (traits.get(EMAIL_KEY) != null) {
            userProfileBuilder.setEmail((String) traits.get(EMAIL_KEY));
            traits.remove(EMAIL_KEY);
        }
        if (traits.get(GENDER_KEY) != null) {
            userProfileBuilder.setGender(Gender.valueByString((String) traits.get(GENDER_KEY)));
            traits.remove(GENDER_KEY);
        }
        if (traits.get(PHONE_KEY) != null) {
            userProfileBuilder.setPhoneNumber((String) traits.get(PHONE_KEY));
            traits.remove(PHONE_KEY);
        }
        if (traits.get(ADDRESS_KEY) != null) {
            Traits.Address address = (Traits.Address) traits.get(ADDRESS_KEY);
            traits.remove(ADDRESS_KEY);
            traits.putAll(address);
        }
        Map<String, Object> webengageOptions = identify.integrations().getValueMap(KEY);
        if (webengageOptions != null) {
            if (webengageOptions.get(HASHED_EMAIL_KEY) != null) {
                userProfileBuilder.setHashedEmail((String) webengageOptions.get(HASHED_EMAIL_KEY));
            }
            if (webengageOptions.get(HASHED_PHONE_KEY) != null) {
                userProfileBuilder.setHashedPhoneNumber((String) webengageOptions.get(HASHED_PHONE_KEY));
            }
            if (webengageOptions.get(PUSH_OPT_IN_KEY) != null) {
                userProfileBuilder.setOptIn(Channel.PUSH, Boolean.valueOf(String.valueOf(webengageOptions.get(PUSH_OPT_IN_KEY))));
            }
            if (webengageOptions.get(SMS_OPT_IN_KEY) != null) {
                userProfileBuilder.setOptIn(Channel.SMS, Boolean.valueOf(String.valueOf(webengageOptions.get(SMS_OPT_IN_KEY))));
            }
            if (webengageOptions.get(EMAIL_OPT_IN_KEY) != null) {
                userProfileBuilder.setOptIn(Channel.EMAIL, Boolean.valueOf(String.valueOf(webengageOptions.get(EMAIL_OPT_IN_KEY))));
            }
        }
        WebEngage.get().user().setUserProfile(userProfileBuilder.build());
        segmentLogger.verbose("WebEngage.get().user().setUserProfile()");
        WebEngage.get().user().setAttributes(traits);
        segmentLogger.verbose("WebEngage.get().user().setAttribute(%s)", traits);

    }

    @Override
    public void track(TrackPayload track) {
        super.track(track);
        WebEngage.get().analytics().track(track.event(), track.properties());
        segmentLogger.verbose("WebEngage.get().analytics().track(%s, %s)", track.event(), track.properties().toJsonObject());
    }

    @Override
    public void screen(ScreenPayload screen) {
        super.screen(screen);
        if (screen.name() != null) {
            Map<String, Object> properties = new HashMap<>();
            properties.putAll(screen.properties());
            WebEngage.get().analytics().screenNavigated(screen.name(), properties);
            segmentLogger.verbose("WebEngage.get().analytics().screenNavigated(%s, %s)", screen.name(), screen.properties().toJsonObject());
        }
    }

    @Override
    public void reset() {
        super.reset();
        WebEngage.get().user().logout();
        segmentLogger.verbose("WebEngage.get().user().logout()");
    }


    @Override
    public WebEngage getUnderlyingInstance() {
        return (WebEngage) WebEngage.get();
    }

    UserProfile.Builder newUserProfileBuilder() {
        return new UserProfile.Builder();
    }
}

