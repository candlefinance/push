import * as React from 'react';

import { StyleSheet, View, Text, Button } from 'react-native';
import type { PushNotificationPermissionStatus } from '@candlefinance/push';
import { module as Push } from '@candlefinance/push';

Push.registerHeadlessTask(async (message) => {
  console.log('Headless Task', message);
});

export default function App() {
  const [permissionStatus, setPermissionStatus] = React.useState<
    PushNotificationPermissionStatus | undefined
  >(undefined);
  const [isGranted, setIsGranted] = React.useState<boolean>(false);

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
  }, [isGranted]);

  return (
    <View style={styles.container}>
      <Text style={styles.text}>Authorization Status: {permissionStatus}</Text>
      <Text style={styles.text}>isGranted: {isGranted}</Text>
      <Button
        title="Get Status"
        onPress={() => {
          Push.getPermissionStatus().then(
            (status: PushNotificationPermissionStatus) => {
              console.log(status);
              setPermissionStatus(status);
            }
          );
        }}
      />
      <Button
        title="Request Permissions"
        onPress={() => {
          Push.requestPermissions().then((granted) => {
            console.log(granted);
            setIsGranted(granted);
            if (granted) {
              Push.registerForToken();
            }
          });
        }}
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
