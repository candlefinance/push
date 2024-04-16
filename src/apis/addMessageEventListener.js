import { nativeEventEmitter } from '../nativeModule';
import { normalizeNativeMessage } from '../utils';
export const addMessageEventListener = (event, listener) => nativeEventEmitter.addListener(event, (nativeMessage) => {
    listener(normalizeNativeMessage(nativeMessage), nativeMessage.completionHandlerId);
});
