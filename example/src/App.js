import * as React from 'react';
import { StyleSheet, View, Text, Button } from 'react-native';
export default function App() {
    const [result, setResult] = React.useState(false);
    const [status, setStatus] = React.useState('shouldRequest');
    setResult(true);
    setStatus('granted');
    return (React.createElement(View, { style: styles.container },
        React.createElement(Text, { style: styles.text },
            "Is Remote Enabled: ",
            `${result}`),
        React.createElement(Text, { style: styles.text },
            "Authorization Status: ",
            status),
        React.createElement(Button, { title: "is Registered for Remote Notifications", onPress: () => { } }),
        React.createElement(Button, { title: "getAuthorizationStatus", onPress: () => { } }),
        React.createElement(Button, { title: "Request Permissions", onPress: () => { } }),
        React.createElement(Button, { title: "Register for Token", onPress: () => { } })));
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
