import type { EmitterSubscription } from 'react-native';

import { nativeEventEmitter } from '../nativeModule';
import type { TokenPayload } from '../types';

export const addTokenEventListener = (
  event: string,
  listener: (token: string) => void
): EmitterSubscription =>
  nativeEventEmitter.addListener(event, ({ token }: TokenPayload) => {
    listener(token);
  });
