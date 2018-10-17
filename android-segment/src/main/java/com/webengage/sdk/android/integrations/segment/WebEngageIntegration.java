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


    protected static final String KEY = "WebEngage";
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

    protected static final String LICENSE_CODE_KEY = "licenseCode";
    private Logger segmentLogger;


    public static final WebEngageFactory FACTORY = new WebEngageFactory();

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
        if (!traits.isEmpty()) {
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
                    int year = gregorianCalendar.get(Calendar.YEAR);
                    int month = gregorianCalendar.get(Calendar.MONTH) + 1;
                    int day = gregorianCalendar.get(Calendar.DAY_OF_MONTH);
                    userProfileBuilder.setBirthDate(year, month, day);
                    segmentLogger.verbose("Setting birth date: %s-%s-%s", year, month, day);
                    traits.remove(BIRTHDAY_KEY);
                }
            }
            if (traits.get(FIRST_NAME_KEY) == null && traits.get(LAST_NAME_KEY) == null) {
                String name = (String) traits.get(NAME_KEY);
                if (name != null) {
                    String[] components = name.split(" ");
                    userProfileBuilder.setFirstName(components[0]);
                    segmentLogger.verbose("Setting first name: %s", components[0]);
                    if (components.length > 1) {
                        userProfileBuilder.setLastName(components[components.length - 1]);
                        segmentLogger.verbose("Setting last name: %s", components[components.length - 1]);
                    }
                    traits.remove(NAME_KEY);
                }
            }
            if (traits.get(FIRST_NAME_KEY) != null) {
                userProfileBuilder.setFirstName((String) traits.get(FIRST_NAME_KEY));
                segmentLogger.verbose("Setting first name: %s", traits.get(FIRST_NAME_KEY));
                traits.remove(FIRST_NAME_KEY);
            }
            if (traits.get(LAST_NAME_KEY) != null) {
                userProfileBuilder.setLastName((String) traits.get(LAST_NAME_KEY));
                segmentLogger.verbose("Setting last name: %s", traits.get(LAST_NAME_KEY));
                traits.remove(LAST_NAME_KEY);
            }
            if (traits.get(INDUSTRY_KEY) != null) {
                userProfileBuilder.setCompany((String) traits.get(INDUSTRY_KEY));
                segmentLogger.verbose("Setting industry: %s", traits.get(INDUSTRY_KEY));
                traits.remove(INDUSTRY_KEY);
            }
            if (traits.get(EMAIL_KEY) != null) {
                userProfileBuilder.setEmail((String) traits.get(EMAIL_KEY));
                segmentLogger.verbose("Setting email: %s", traits.get(EMAIL_KEY));
                traits.remove(EMAIL_KEY);
            }
            if (traits.get(GENDER_KEY) != null) {
                Gender gender = Gender.valueByString((String) traits.get(GENDER_KEY));
                userProfileBuilder.setGender(gender);
                segmentLogger.verbose("Setting gender: %s", gender);
                traits.remove(GENDER_KEY);
            }
            if (traits.get(PHONE_KEY) != null) {
                userProfileBuilder.setPhoneNumber((String) traits.get(PHONE_KEY));
                segmentLogger.verbose("Setting phone number: %s", traits.get(PHONE_KEY));
                traits.remove(PHONE_KEY);
            }
            if (traits.get(ADDRESS_KEY) != null) {
                Traits.Address address = (Traits.Address) traits.get(ADDRESS_KEY);
                traits.putAll(address);
                segmentLogger.verbose("Setting address: %s", traits.get(ADDRESS_KEY));
                traits.remove(ADDRESS_KEY);
            }
        }
        Map<String, Object> webengageOptions = identify.integrations().getValueMap(KEY);
        if (webengageOptions != null) {
            if (webengageOptions.get(HASHED_EMAIL_KEY) != null) {
                userProfileBuilder.setHashedEmail((String) webengageOptions.get(HASHED_EMAIL_KEY));
                segmentLogger.verbose("Setting hashed email: %s", webengageOptions.get(HASHED_EMAIL_KEY));
            }
            if (webengageOptions.get(HASHED_PHONE_KEY) != null) {
                userProfileBuilder.setHashedPhoneNumber((String) webengageOptions.get(HASHED_PHONE_KEY));
                segmentLogger.verbose("Setting hashed phone number: %s", webengageOptions.get(HASHED_PHONE_KEY));
            }
            if (webengageOptions.get(PUSH_OPT_IN_KEY) != null) {
                Boolean pushOptIn = Boolean.valueOf(String.valueOf(webengageOptions.get(PUSH_OPT_IN_KEY)));
                userProfileBuilder.setOptIn(Channel.PUSH, pushOptIn);
                segmentLogger.verbose("Setting push opt in: %s", pushOptIn);
            }
            if (webengageOptions.get(SMS_OPT_IN_KEY) != null) {
                Boolean smsOptIn = Boolean.valueOf(String.valueOf(webengageOptions.get(SMS_OPT_IN_KEY)));
                userProfileBuilder.setOptIn(Channel.SMS, smsOptIn);
                segmentLogger.verbose("Setting sms opt in: %s", smsOptIn);
            }
            if (webengageOptions.get(EMAIL_OPT_IN_KEY) != null) {
                Boolean emailOptIn = Boolean.valueOf(String.valueOf(webengageOptions.get(EMAIL_OPT_IN_KEY)));
                userProfileBuilder.setOptIn(Channel.EMAIL, emailOptIn);
                segmentLogger.verbose("Setting email opt in: %s", emailOptIn);
            }
        }
        WebEngage.get().user().setUserProfile(userProfileBuilder.build());
        if (!traits.isEmpty()) {
            WebEngage.get().user().setAttributes(traits);
            segmentLogger.verbose("WebEngage.get().user().setAttributes(%s)", traits);
        }
    }

    @Override
    public void track(TrackPayload track) {
        super.track(track);
        WebEngage.get().analytics().track(track.event(), track.properties());
        segmentLogger.verbose("WebEngage.get().analytics().track(%s, %s)", track.event(), track.properties());
    }

    @Override
    public void screen(ScreenPayload screen) {
        super.screen(screen);
        if (screen.name() != null) {
            WebEngage.get().analytics().screenNavigated(screen.name(), screen.properties());
            segmentLogger.verbose("WebEngage.get().analytics().screenNavigated(%s, %s)", screen.name(), screen.properties());
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

