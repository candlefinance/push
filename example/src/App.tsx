import * as React from 'react';

import { StyleSheet, View, Text, Button } from 'react-native';
import push, { type AuthorizationStatus } from '@candlefinance/push';

export default function App() {
  const [result, setResult] = React.useState<boolean>(false);
  const [status, setStatus] =
    React.useState<AuthorizationStatus>('notDetermined');

  React.useEffect(() => {
    push.addListener('notificationReceived', async (data) => {
      console.log('notificationReceived', data);
      switch (data.kind) {
        case 'opened':
          console.log('opened');
          break;
        case 'foreground':
        case 'background':
          console.log('foreground/background');
          const { uuid } = data;
          await push.onFinish(uuid);
          break;
        // return { badge: 0, sound: '', alert: '' };
      }
    });

    push.addListener('deviceTokenReceived', (data) => {
      console.log('deviceTokenReceived', data);
    });

    push.addListener('errorReceived', (data) => {
      console.log('errorReceived', data);
    });

    return () => {
      push.removeListener('notificationReceived');
      push.removeListener('deviceTokenReceived');
      push.removeListener('errorReceived');
    };
  }, []);

  return (
    <View style={styles.container}>
      <Text style={styles.text}>Is Remote Enabled: {`${result}`}</Text>
      <Text style={styles.text}>Authorization Status: {status}</Text>
      <Button
        title="is Registered for Remote Notifications"
        onPress={() => {
          push.isRegisteredForRemoteNotifications().then((res) => {
            console.log('isRegisteredForRemoteNotifications', res);
            setResult(res);
          });
        }}
      />
      <Button
        title="getAuthorizationStatus"
        onPress={() => {
          push.getAuthorizationStatus().then((res) => {
            console.log('isRegisteredForRemoteNotifications', res);
            setStatus(res);
          });
        }}
      />
      <Button
        title="Request Permissions"
        onPress={() => push.requestPermissions()}
      />
      <Button
        title="Register for Token"
        onPress={() => push.registerForToken()}
      />
      <Button
        title="Get FCM Token"
        onPress={() =>
          push
            .getToken()
            .then((token) => {
              console.log('Token :', token);
            })
            .catch((e) => {
              console.log('Error while fetching deivce FCM token', e);
            })
        }
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
  text: {
    fontSize: 17,
    fontWeight: '400',
    marginBottom: 20,
  },
});
