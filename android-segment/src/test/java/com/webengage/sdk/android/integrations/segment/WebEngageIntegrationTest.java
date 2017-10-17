package com.webengage.sdk.android.integrations.segment;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Looper;

import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;
import com.segment.analytics.Traits;
import com.segment.analytics.ValueMap;
import com.segment.analytics.integrations.IdentifyPayload;
import com.segment.analytics.integrations.Logger;
import com.segment.analytics.integrations.ScreenPayload;
import com.segment.analytics.integrations.TrackPayload;
import com.webengage.sdk.android.Channel;
import com.webengage.sdk.android.User;
import com.webengage.sdk.android.UserProfile;
import com.webengage.sdk.android.WebEngage;
import com.webengage.sdk.android.WebEngageConfig;
import com.webengage.sdk.android.utils.Gender;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({WebEngage.class, Looper.class,Logger.class})
public class WebEngageIntegrationTest {
    private Analytics analytics;
    private WebEngageIntegration webEngageIntegration;
    private Activity activity;
    private Bundle bundle;
    private WebEngage webEngage;
    private com.webengage.sdk.android.Analytics weAnalytics;
    private User user;
    private Application application;


    @Before
    public void setUp() {
        PowerMockito.mockStatic(Looper.class);
        PowerMockito.mockStatic(WebEngage.class);
        PowerMockito.mockStatic(Logger.class);
        application = mock(Application.class);
        Context context = mock(Context.class);
        analytics = mock(Analytics.class);
        activity = mock(Activity.class);
        bundle = mock(Bundle.class);
        webEngage = mock(WebEngage.class);
        weAnalytics = mock(com.webengage.sdk.android.Analytics.class);
        user = mock(User.class);
        when(Looper.getMainLooper()).thenReturn(mock(Looper.class));
        when(analytics.getApplication()).thenReturn(application);
        when(application.getApplicationContext()).thenReturn(context);
        when(WebEngage.get()).thenReturn(webEngage);
        when(webEngage.analytics()).thenReturn(weAnalytics);
        when(webEngage.user()).thenReturn(user);
        Logger logger = mock(Logger.class);
        when(analytics.logger("WebEngage")).thenReturn(logger);
        webEngageIntegration = new WebEngageIntegration(logger);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void initializeSuccessTest() {
        ValueMap valueMap = new ValueMap().putValue("licenseCode", "~lic1");
        WebEngageIntegration.FACTORY.create(valueMap, analytics);
        ArgumentCaptor<WebEngageConfig> argumentCaptor = ArgumentCaptor.forClass(WebEngageConfig.class);
        PowerMockito.verifyStatic(times(1));
        WebEngage.engage(eq(application), argumentCaptor.capture());
        WebEngageConfig webEngageConfig = argumentCaptor.getValue();
        if (webEngageConfig != null) {
            assertEquals("~lic1", webEngageConfig.getWebEngageKey());
        }
        verifyNoMoreInteractionWithWebEngage();
    }

    @Test
    public void initializeFailTest() {
        WebEngageIntegration webEngageIntegration = (WebEngageIntegration) WebEngageIntegration.FACTORY.create(new ValueMap(), analytics);
        assertNull(webEngageIntegration);
        verifyNoMoreInteractionWithWebEngage();
    }

    @Test
    public void activityCreatedTest() {
        webEngageIntegration.onActivityCreated(activity, bundle);
        verifyNoMoreInteractionWithWebEngage();
    }

    @Test
    public void activityStartedTest() {
        webEngageIntegration.onActivityStarted(activity);
        PowerMockito.verifyStatic(times(1));
        WebEngage.get();
        verify(webEngage, times(1)).analytics();
        verify(weAnalytics, times(1)).start(eq(activity));
        verifyNoMoreInteractionWithWebEngage();
    }

    @Test
    public void activityResumedTest() {
        webEngageIntegration.onActivityResumed(activity);
        verifyNoMoreInteractionWithWebEngage();
    }

    @Test
    public void activityPausedTest() {
        webEngageIntegration.onActivityPaused(activity);
        verifyNoMoreInteractionWithWebEngage();
    }

    @Test
    public void activityStoppedTest() {
        webEngageIntegration.onActivityStopped(activity);
        PowerMockito.verifyStatic(times(1));
        WebEngage.get();
        verify(webEngage, times(1)).analytics();
        verify(weAnalytics, times(1)).stop(eq(activity));
        verifyNoMoreInteractionWithWebEngage();
    }

    @Test
    public void activitySavedInstanceState() {
        webEngageIntegration.onActivitySaveInstanceState(activity, bundle);
        verifyNoMoreInteractionWithWebEngage();
    }

    @Test
    public void activityDestroyedTest() {
        webEngageIntegration.onActivityDestroyed(activity);
        verifyNoMoreInteractionWithWebEngage();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void identifyTest() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date birthDate = null;
        try {
            birthDate = simpleDateFormat.parse("2000-12-01");
        } catch (ParseException e) {
            Logger.with(Analytics.LogLevel.INFO).info("date string cannot be parsed to yyyy-MM-dd");
        }

        Traits traits = new Traits();
        traits.putBirthday(birthDate);
        traits.putFirstName("Hello");
        traits.putLastName("World");
        traits.putIndustry("WebEngage");
        traits.putEmail("test@xyz.com");
        traits.putGender("male");
        traits.putPhone("1111111111");
        traits.putAddress(new Traits.Address().putStreet("street1").putCity("city1").putCountry("country1").putState("state1"));
        traits.putAge(25);
        traits.putValue("isLucky", true);

        Map<String,Object> webengageOptions = new HashMap<>();
        webengageOptions.put("we_hashed_email", "ABC");
        webengageOptions.put("we_hashed_phone", "111");
        webengageOptions.put("we_push_opt_in", true);
        webengageOptions.put("we_sms_opt_in", false);
        webengageOptions.put("we_email_opt_in", true);



        IdentifyPayload identifyPayload = new IdentifyPayload.Builder()
                .userId("fanatic")
                .anonymousId("anon1")
                .traits(traits)
                .integration("WebEngage",webengageOptions)
                .build();

        UserProfile.Builder userProfileBuilder = mock(UserProfile.Builder.class);
        WebEngageIntegration spy = spy(webEngageIntegration);
        doReturn(userProfileBuilder).when(spy).newUserProfileBuilder();

        spy.identify(identifyPayload);

        PowerMockito.verifyStatic(times(3));
        WebEngage.get();
        verify(webEngage, times(3)).user();
        verify(user, times(1)).login(eq("fanatic"));

        verify(userProfileBuilder, times(1)).setBirthDate(eq(2000), eq(12), eq(1));
        verify(userProfileBuilder, times(1)).setFirstName(eq("Hello"));
        verify(userProfileBuilder, times(1)).setLastName(eq("World"));
        verify(userProfileBuilder, times(1)).setCompany(eq("WebEngage"));
        verify(userProfileBuilder, times(1)).setEmail(eq("test@xyz.com"));
        verify(userProfileBuilder, times(1)).setGender(eq(Gender.MALE));
        verify(userProfileBuilder, times(1)).setPhoneNumber(eq("1111111111"));
        verify(userProfileBuilder, times(1)).setHashedEmail(eq("ABC"));
        verify(userProfileBuilder, times(1)).setHashedPhoneNumber(eq("111"));
        verify(userProfileBuilder, times(1)).setOptIn(eq(Channel.PUSH), eq(true));
        verify(userProfileBuilder, times(1)).setOptIn(eq(Channel.SMS), eq(false));
        verify(userProfileBuilder, times(1)).setOptIn(eq(Channel.EMAIL), eq(true));

        ArgumentCaptor<Map> customAttributesArgumentCaptor = ArgumentCaptor.forClass(Map.class);
        verify(user, times(1)).setAttributes(customAttributesArgumentCaptor.capture());

        Map result = customAttributesArgumentCaptor.getValue();

        assertEquals(6, result.size());
        assertEquals(25, result.get("age"));
        assertEquals(true, result.get("isLucky"));
        assertEquals("street1", result.get("street"));
        assertEquals("city1", result.get("city"));
        assertEquals("country1", result.get("country"));
        assertEquals("state1", result.get("state"));

        verifyNoMoreInteractionWithWebEngage();
    }


    @Test
    public void groupTest() {
        webEngageIntegration.group(null);
        verifyNoMoreInteractionWithWebEngage();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void trackTest() {
        Properties properties = new Properties()
                .putDiscount(12.1)
                .putOrderId("~123")
                .putTotal(1000.1);

        properties.put("inStock", new Date(1497192143));

        TrackPayload trackPayload = new TrackPayload.Builder()
                .event("Purchase")
                .anonymousId("anon1")
                .properties(properties)
                .build();
        webEngageIntegration.track(trackPayload);

        PowerMockito.verifyStatic(times(1));
        WebEngage.get();
        verify(webEngage, times(1)).analytics();
        ArgumentCaptor<Map> propertiesArgumentCaptor = ArgumentCaptor.forClass(Map.class);
        verify(weAnalytics, times(1)).track(eq("Purchase"), propertiesArgumentCaptor.capture());

        Map capturedProperties = propertiesArgumentCaptor.getValue();
        if (capturedProperties != null) {
            assertEquals(4, capturedProperties.size());
            assertEquals(12.1, capturedProperties.get("discount"));
            assertEquals("~123", capturedProperties.get("orderId"));
            assertEquals(1000.1, capturedProperties.get("total"));
            assertEquals(new Date(1497192143), capturedProperties.get("inStock"));
        }
        verifyNoMoreInteractionWithWebEngage();
    }

    @Test
    public void aliasTest() {
        webEngageIntegration.alias(null);
        verifyNoMoreInteractionWithWebEngage();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void screenTest() {
        Properties properties = new Properties();
        properties.put("a", 1);
        ScreenPayload screenPayload = new ScreenPayload.Builder()
                .name("PurchaseScreen")
                .properties(properties)
                .anonymousId("anon1")
                .build();

        webEngageIntegration.screen(screenPayload);
        PowerMockito.verifyStatic(times(1));
        WebEngage.get();
        verify(webEngage, times(1)).analytics();
        ArgumentCaptor<Map> propertiesArgumentCaptor = ArgumentCaptor.forClass(Map.class);
        verify(weAnalytics, times(1)).screenNavigated(eq("PurchaseScreen"), propertiesArgumentCaptor.capture());

        Map capturedProperties = propertiesArgumentCaptor.getValue();

        if (capturedProperties != null) {
            assertEquals(1, capturedProperties.size());
            assertEquals(1, capturedProperties.get("a"));
        }

        verifyNoMoreInteractionWithWebEngage();


    }

    @Test
    public void resetTest() {
        webEngageIntegration.reset();
        PowerMockito.verifyStatic(times(1));
        WebEngage.get();
        verify(webEngage, times(1)).user();
        verify(user, times(1)).logout();
        verifyNoMoreInteractionWithWebEngage();
    }


    private void verifyNoMoreInteractionWithWebEngage() {
        PowerMockito.verifyNoMoreInteractions(WebEngage.class);
        verifyNoMoreInteractions(webEngage);
    }
}