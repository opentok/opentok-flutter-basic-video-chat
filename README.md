# opentok-android-sdk-basic-video-chat-flutter

This repository is providing Basic video chat sample using OpenTok SDK and flutter.

## Setup flutter SDK

Download, extract and confgure [flutter SDK](https://flutter.dev/docs/get-started/install).

Fortunately, flutter comes with a tool that allows us to verify if SDK and all required "components" are present and configured correctly. Run this command:

Check if everything is configured correctly:

```cmd
flutter doctor
```

## Run the app

There are multiple ways to run the app. The easiest way is to open the whole project in the [Android Studio](https://developer.android.com/studio) with [Flutter plugin](https://flutter.dev/docs/development/tools/android-studio) installed. Open Flutter project (the root of this repo) and run app targetting one of the platforms.

Fill `OpenTokConfig`

Optionally you can open iOS project (`ios/Runner.xcworkspace` folder) in Xcode and run iOS app or Android project in Android Studio (`android` folder) and run Android App. This is possible because Flutter project consists of iOS and Android projects.

## Set up credentials

You will need a valid [TokBox account](https://tokbox.com/account/user/signup) for most of the sample projects. OpenTok credentials (`API_KEY`, `SESSION_ID`, `TOKEN`) are stored inside `OpenTokConfig` class (inside `main.dart` file). For these sample applications, credentials can be retrieved from the [Dashboard](https://dashboard.tokbox.com/projects) and hardcoded in the application, however for a production environment server should provide these credentials (check [Basic-Video-Chat](/Basic-Video-Chat) project). 

> Note: To facilitate testing connect to the same session using [OpenTok Playground](https://tokbox.com/developer/tools/playground/) (web client).
## Known issues

When hosting the Native Android/iOS view in the Flutter app the [PlatformViewFactory](https://api.flutter.dev/javadoc/io/flutter/plugin/platform/PlatformViewFactory.html) must be used to create instance of the native view ([PlatformView](https://flutter.dev/docs/development/platform-integration/platform-views)). However Opentok SDK is creating views for video streams by itself. This means that we need a hack to be able to display opentok videos within the Flutter app. Instead of creating native view (using the `PlatformViewFactory`), app creates a container view (also using the `PlatformViewFactory`) and latter attaches Opentok video streams to this container. Unfortunately this solution requires to store view in the static property.

## Development and Contributing

Feel free to copy and modify the source code herein for your projects. Please consider sharing your modifications with us, especially if they might benefit other developers using the OpenTok Android SDK. See the [License](LICENSE) for more information.

Interested in contributing? You :heart: pull requests! See the 
[Contribution](CONTRIBUTING.md) guidelines.

## Getting Help

You love to hear from you so if you have questions, comments or find a bug in the project, let us know! You can either:

- Open an issue on this repository
- See <https://support.tokbox.com/> for support options
- Tweet at us! We're [@VonageDev](https://twitter.com/VonageDev) on Twitter
- Or [join the Vonage Developer Community Slack](https://developer.nexmo.com/community/slack)


## References

- [Basic Video chat Android](https://github.com/opentok/opentok-android-sdk-samples/tree/main/Basic-Video-Chat)
- [Basic Video chat iOS](https://github.com/opentok/opentok-ios-sdk-samples/tree/main/Basic-Video-Chat)
- [Basic Video chat Web](- [Basic Video chat Web](https://github.com/opentok/opentok-ios-sdk-samples/tree/main/Basic-Video-Chat))
