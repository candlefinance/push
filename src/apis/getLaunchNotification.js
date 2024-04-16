import { nativeModule } from '../nativeModule';
import { normalizeNativeMessage } from '../utils';
export const getLaunchNotification = async () => normalizeNativeMessage((await nativeModule.getLaunchNotification()) ?? undefined);
