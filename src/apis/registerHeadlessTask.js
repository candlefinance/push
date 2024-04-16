import { AppRegistry } from 'react-native';
import { normalizeNativeMessage } from '../utils';
import { getConstants } from './getConstants';
export const registerHeadlessTask = (task) => {
    const { NativeHeadlessTaskKey } = getConstants();
    if (NativeHeadlessTaskKey) {
        AppRegistry.registerHeadlessTask(NativeHeadlessTaskKey, () => async (nativeMessage) => {
            await task(normalizeNativeMessage(nativeMessage));
        });
    }
};
