import * as React from 'react';

import { StyleSheet, View, Text, Button } from 'react-native';
// import type { PushNotificationPermissionStatus } from '@candlefinance/push';

export default function App() {
  return (
    <View style={styles.container}>
      <Text style={styles.text}>Authorization Status:</Text>
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
