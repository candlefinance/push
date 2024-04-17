import { nativeEventEmitter } from '../nativeModule';

export const removeListeners = (event: string) => {
  nativeEventEmitter.removeAllListeners(event);
};
