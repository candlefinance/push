import * as React from 'react';

import { StyleSheet, View, Text, Button } from 'react-native';
import type { PushNotificationPermissionStatus } from '@candlefinance/push';

export default function App() {
  const [result, setResult] = React.useState<boolean>(false);
  const [status, setStatus] =
    React.useState<PushNotificationPermissionStatus>('shouldRequest');
  setResult(true);
  setStatus('granted');
  return (
    <View style={styles.container}>
      <Text style={styles.text}>Is Remote Enabled: {`${result}`}</Text>
      <Text style={styles.text}>Authorization Status: {status}</Text>
      <Button
        title="is Registered for Remote Notifications"
        onPress={() => {}}
      />
      <Button title="getAuthorizationStatus" onPress={() => {}} />
      <Button title="Request Permissions" onPress={() => {}} />
      <Button title="Register for Token" onPress={() => {}} />
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
