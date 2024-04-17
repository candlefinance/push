import type { EmitterSubscription } from 'react-native';

import { nativeEventEmitter } from '../nativeModule';

export const addErrorListener = (
  event: string,
  listener: (message: string) => void
): EmitterSubscription =>
  nativeEventEmitter.addListener(event, ({ message }: { message: string }) => {
    listener(message);
  });
