# Segment-Integration-Android
[ ![Download](https://api.bintray.com/packages/webengage/maven/android-segment/images/download.svg) ](https://bintray.com/webengage/maven/android-segment/_latestVersion)

WebEngage integration for [analytics-android](https://github.com/segmentio/analytics-android)

## Installation

To install the WebEngage-Segment integration, simply add this line to your app build.gradle file:

```
compile 'com.webengage:android-segment:2.+'
```

## Usage

After adding the dependency, you must register the WebEngage's destination in your Segment's Analytics instance.  To do this, import the WebEngage integration:


```
import com.webengage.sdk.android.integrations.segment.WebEngageIntegration;

```

And add the following line:

```
analytics = new Analytics.Builder(this, "write_key")
                .use(WebEngageIntegration.FACTORY)
                .build();
```

## Push Notifications
Please follow our [android push notification documentation](https://docs.webengage.com/docs/android-push-messaging)

## In-App Notifications
No further action is required to integrate in-app messages.

## Proguard
Please include the following rules in your proguard file.

``` groovy
-keep class com.webengage.sdk.android.**{*;}
-dontwarn com.webengage.sdk.android.**
```


## Advanced
For advanced integration options such as attribution and location tracking, please visit the advanced [section](https://docs.webengage.com/docs/android-advanced) of our Android documentation.

## Sample App
WebEngage has created a sample Android application that integrates WebEngage via Segment. Check it out at our [Gitub repo](https://github.com/WebEngage/segment-integration-android-example).
