import { NativeEventEmitter, NativeModules } from 'react-native';
import { LINKING_ERROR } from './constants';
export const nativeModule = NativeModules.AmplifyRTNPushNotification
    ? NativeModules.AmplifyRTNPushNotification
    : new Proxy({}, {
        get() {
            throw new Error(LINKING_ERROR);
        },
    });
export const nativeEventEmitter = new NativeEventEmitter(nativeModule);
