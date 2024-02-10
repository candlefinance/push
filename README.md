<br/>
<div align="center">
  <a alt="npm" href="https://www.npmjs.com/package/@candlefinace/push">
      <img alt="npm downloads" src="https://img.shields.io/npm/dm/%40candlefinance%2@candlefinance/push"/>
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
 Push for iOS
</h1>

<br/>

## Installation

```sh
yarn add @candlefinance/push
```

## Usage

There is no native code required, that is all abstracted away. Here is a simple example of how to use the package:

```js
import push from '@candlefinance/push';

// Shows dialog to request permission to send push notifications, gets APNS token
const isGranted = await push.requestPermissions();

// Get the APNS token w/o showing permission, useful if you want silent push notifications
await registerForToken();

// Check permission status: 'granted', 'denied', or 'notDetermined'
const status = await getAuthorizationStatus();

// Listen for push APNS token
push.addListener('notificationReceived', (data) => {
  console.log('notificationReceived', data);
  const uuid = data.uuid;
  const kind = data.kind; // foreground, background, or opened
  const payload = data.payload;
  if (uuid) {
    // Required push notification to tell iOS that the push was received, if not called, we will call this in 30 seconds
    await push.onFinish(uuid);
  }
});

push.addListener('deviceTokenReceived', (token) => {
  const token: string = token;
});

push.addListener('errorReceived', (data) => {
  console.log('errorReceived', data);
});

return () => {
  push.removeListener('notificationReceived');
  push.removeListener('deviceTokenReceived');
  push.removeListener('errorReceived');
};
```

## Contributing

We are open to contributions. Please read our [Contributing Guide](CONTRIBUTING.md) for more information.
