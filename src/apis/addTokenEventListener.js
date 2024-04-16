import { nativeEventEmitter } from '../nativeModule';
export const addTokenEventListener = (event, listener) => nativeEventEmitter.addListener(event, ({ token }) => {
    listener(token);
});
