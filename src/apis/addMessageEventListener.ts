import type { EmitterSubscription } from 'react-native';

import { nativeEventEmitter } from '../nativeModule';
import type { NativeMessage, PushNotificationMessage } from '../types';
import { normalizeNativeMessage } from '../utils';

export const addMessageEventListener = (
  event: string,
  listener: (
    message: PushNotificationMessage | null,
    completionHandlerId?: string
  ) => void
): EmitterSubscription =>
  nativeEventEmitter.addListener(event, (nativeMessage: NativeMessage) => {
    console.log('nativeMessage', nativeMessage);
    listener(
      normalizeNativeMessage(nativeMessage),
      nativeMessage.completionHandlerId
    );
  });
