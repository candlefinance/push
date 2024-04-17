import { AppRegistry } from 'react-native';

import type { NativeMessage, PushNotificationMessage } from '../types';
import { normalizeNativeMessage } from '../utils';

import { getConstants } from './getConstants';
import { completeNotification } from './completeNotification';

export const registerHeadlessTask = (
  task: (message: PushNotificationMessage | null) => Promise<void>
): void => {
  const { NativeHeadlessTaskKey } = getConstants();
  if (NativeHeadlessTaskKey) {
    AppRegistry.registerHeadlessTask(
      NativeHeadlessTaskKey,
      () => async (nativeMessage: NativeMessage) => {
        await task(normalizeNativeMessage(nativeMessage));
        if (nativeMessage.completionHandlerId) {
          completeNotification(nativeMessage.completionHandlerId);
        }
      }
    );
  }
};
