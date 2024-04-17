import * as React from 'react';

import { StyleSheet, View, Text, Button } from 'react-native';
import type { PushNotificationPermissionStatus } from '@candlefinance/push';
import { module as Push } from '@candlefinance/push';

export default function App() {
  return (
    <View style={styles.container}>
      <Text style={styles.text}>Authorization Status</Text>
      <Button
        title="Request Permissions"
        onPress={() => {
          Push.getPermissionStatus().then(
            (status: PushNotificationPermissionStatus) => {
              console.log(status);
            }
          );
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
