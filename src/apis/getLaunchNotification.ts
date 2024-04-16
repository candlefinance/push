import { nativeModule } from '../nativeModule';
import type { PushNotificationMessage } from '../types';
import { normalizeNativeMessage } from '../utils';

export const getLaunchNotification =
  async (): Promise<PushNotificationMessage | null> =>
    normalizeNativeMessage(
      (await nativeModule.getLaunchNotification()) ?? undefined
    );
