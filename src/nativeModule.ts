import { NativeEventEmitter, NativeModules } from 'react-native';

import { LINKING_ERROR } from './constants';
import type { PushNotificationNativeModule } from './types';

export const nativeModule: PushNotificationNativeModule = NativeModules.Push
  ? NativeModules.Push
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export const nativeEventEmitter = new NativeEventEmitter(nativeModule);
