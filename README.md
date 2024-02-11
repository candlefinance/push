<br/>
<div align="center">
  <!-- <a alt="npm" href="https://www.npmjs.com/package/@candlefinace/push">
      <img alt="npm downloads" src="https://img.shields.io/npm/dm/%40candlefinance%2@candlefinance/push"/>
  </a> -->
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

The motivation to write this came from the unmaintained and outdatated libraries that exist today. This implementation is written in Swift in less than 200 lines of code.

Andriod support is coming soon. Checkout [#1](https://github.com/candlefinance/push/issues/1) if you want to help.

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
2. If your AppDelegate is in Objective-C (`.mm|.m|.h`), create a new `AppDelegate.swift` file and bridging header then delete the Objective-C AppDelegate and main.m file. Finally copy the contents of the example app's [AppDelegate.swift](./example/ios/AppDelegate.swift) and [bridge header](./example/ios/PushExample-Bridging-Header.h) to your project.
3. Make sure you're on `iOS 15` or later.

<br>
The following code is used to handle push notifications on the React Native side:

```js
import push from '@candlefinance/push';

// Shows dialog to request permission to send push notifications, gets APNS token
const isGranted = await push.requestPermissions();

// Get the APNS token w/o showing permission, useful if you want silent push notifications
await push.registerForToken();

// Check permission status: 'granted', 'denied', or 'notDetermined'
const status = await push.getAuthorizationStatus();

// Check if APNS token is registered
const isRegistered = await push.isRegisteredForRemoteNotifications();

// Listeners
push.addListener('notificationReceived', (data) => {
  const uuid = data.uuid;
  const kind = data.kind; // foreground, background, or opened
  const payload = data.payload;
  if (uuid) {
    // Required to tell iOS that the push was received, if not called, the library will call this in 30 seconds
    await push.onFinish(uuid);
  }
});

push.addListener('deviceTokenReceived', (token) => {});
push.addListener('errorReceived', (error) => {});

// Remove listeners
push.removeListener('notificationReceived');
push.removeListener('deviceTokenReceived');
push.removeListener('errorReceived');
```

## Testing

If you run the example app, you can test push notifications by running the following command:

```sh
yarn push
```

This will use the [payload.json](./example/payload.json) file to send a push notification to the device. You can modify the payload to test different scenarios.

## Contributing

We are open to contributions. Please read our [Contributing Guide](CONTRIBUTING.md) for more information.

## License

This project is licensed under the terms of the [MIT license](LICENSE).

## Discord

Post in #oss channel in our [Discord](https://discord.gg/Qm7ZPUhBWV) if you have any questions or want to contribute.
