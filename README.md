<br/>
<div align="center">
<a href="https://www.npmjs.com/package/@candlefinance/push">
  <img src="https://img.shields.io/npm/dm/@candlefinance/push" alt="npm downloads" />
</a>
  <a alt="discord users online" href="https://discord.gg/qnAgjxhg6n" 
  target="_blank"
  rel="noopener noreferrer">
    <img alt="discord users online" src="https://img.shields.io/discord/986610142768406548?label=Discord&logo=discord&logoColor=white&cacheSeconds=3600"/>
</div>

<br/>
<div align="center">
    <img src="https://github.com/candlefinance/haptics/assets/12258850/86470cfc-fe84-4159-adcd-dbb659778619.png" alt="bell" width="200"/>
</div>

<h1 align="center">
 Push for React Native
</h1>

<br/>

## Installation

```sh
yarn add @candlefinance/push
```

The motivation to write this came from the unmaintained and outdated libraries that exist today. This implementation is written in Swift in less than 200 lines of code.

Android support is coming soon. Check out [#1](https://github.com/candlefinance/push/issues/1) if you want to help.

## Usage

### iOS

- [x] Request permissions
- [x] Register for APNS token
- [x] Remote push notifications
  - [x] Foreground
  - [x] Background
  - [x] Opened by tapping on the notification
- [ ] Local push notifications

#### Setup

1. You'll need to update your `AppDelegate.swift` to handle push check the example app [here](./example/ios/AppDelegate.swift) for an example.
2. If your AppDelegate is in Objective-C (`.mm|.m|.h`), create a new `AppDelegate.swift` file and bridging header, then delete the Objective-C AppDelegate and main.m file. Finally, copy the contents of the example app's [AppDelegate.swift](./example/ios/AppDelegate.swift) and [bridge header](./example/ios/PushExample-Bridging-Header.h) to your project.
3. Make sure you're on `iOS 15` or later.
4. You can also use Objective-C just add bridging header and import the module.
5. `UNUserNotificationCenterDelegate` set in AppDelegate.

### Android

- [x] Request permissions
- [x] Register for FCM token
- [x] Remote push notifications
  - [x] Foreground
  - [x] Background + Headless JS
  - [x] Opened by tapping on the notification
- [ ] Local push notifications

#### Setup

1. Add permissions in [AndroidManifest.xml](./example/android/app/src/main/AndroidManifest.xml)
2. Add `google-services.json` in `android/app` directory from Firebase console.

## API

<br>
The following code is used to handle push notifications on the React Native side:

```js
import type { PushNotificationPermissionStatus } from '@candlefinance/push';
import { module as Push } from '@candlefinance/push';

// Shows dialog to request permission to send push notifications, gets APNS token
const isGranted = await push.requestPermissions();

// Get the APNS token w/o showing permission, useful if you want silent push notifications
push.registerForToken();

// Check permission status: 'granted', 'denied', or 'notDetermined'
const status = await push.getAuthorizationStatus();

// Listeners
React.useEffect(() => {
  const { NativeEvent, NativeHeadlessTaskKey } = Push.getConstants();
  console.log(NativeEvent, NativeHeadlessTaskKey);
  Push.addTokenEventListener(NativeEvent.TOKEN_RECEIVED, (token) => {
    console.log('TOKEN_RECEIVED:', token);
  });
  Push.addMessageEventListener(
    NativeEvent.BACKGROUND_MESSAGE_RECEIVED,
    (message, id) => {
      console.log('BACKGROUND_MESSAGE_RECEIVED:', message);
      if (id !== undefined) {
        console.log('Completing notification:', id);
        Push.completeNotification(id);
      }
    }
  );
  Push.addErrorListener(NativeEvent.FAILED_TO_REGISTER, (message) => {
    console.log('FAILED_TO_REGISTER:', message);
  });
  Push.addMessageEventListener(NativeEvent.NOTIFICATION_OPENED, (message) => {
    console.log('NOTIFICATION_OPENED:', message);
  });
  Push.addMessageEventListener(
    NativeEvent.FOREGROUND_MESSAGE_RECEIVED,
    (message) => {
      console.log('FOREGROUND_MESSAGE_RECEIVED:', message);
    }
  );
  Push.addMessageEventListener(
    NativeEvent.LAUNCH_NOTIFICATION_OPENED,
    (message) => {
      console.log('LAUNCH_NOTIFICATION_OPENED:', message);
    }
  );
  return () => {
    Push.removeListeners(NativeEvent.TOKEN_RECEIVED);
    Push.removeListeners(NativeEvent.BACKGROUND_MESSAGE_RECEIVED);
    Push.removeListeners(NativeEvent.NOTIFICATION_OPENED);
    Push.removeListeners(NativeEvent.FOREGROUND_MESSAGE_RECEIVED);
    Push.removeListeners(NativeEvent.LAUNCH_NOTIFICATION_OPENED);
  };
}, []);
```

## Testing

If you run the example app, you can test push notifications by running the following command:

```sh
yarn push
```

This will use the [payload.json](./example/payload.json) file to send a push notification to the device. You can modify the payload to test different scenarios.

Apple also has a new [console](https://developer.apple.com/notifications/push-notifications-console/) to test push notifications. If you print out the token from `deviceTokenReceived` listener, you can use it to send a push notification from the console.

## SNS

If you're using AWS SNS, you can use the following code to send a push notification

```
 const message = // apns
        os === 'ios' ? JSON.stringify({ APNS: JSON.stringify(payload) })
          : // fcm
            JSON.stringify({
              GCM: JSON.stringify({
                      data: {
                        title: title,
                        body: body,
                        custom: customData,
                        data: customData,
                        priority: '1',
                        imageUrl:
                          'https://logo.png',
                        targetClass: 'com.yourapp.candle.MainActivity',
                      },
                    })
            })
```

## Contributing

We are open to contributions. Please read our [Contributing Guide](CONTRIBUTING.md) for more information.

## License

This project is licensed under the terms of the [MIT license](LICENSE).

## Discord

Post in #oss channel in our [Discord](https://discord.gg/Qm7ZPUhBWV) if you have any questions or want to contribute.
