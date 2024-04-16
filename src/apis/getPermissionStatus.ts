import { nativeModule } from '../nativeModule';
import type { PushNotificationPermissionStatus } from '../types';
import { normalizeNativePermissionStatus } from '../utils';

export const getPermissionStatus =
  async (): Promise<PushNotificationPermissionStatus> =>
    normalizeNativePermissionStatus(await nativeModule.getPermissionStatus());
