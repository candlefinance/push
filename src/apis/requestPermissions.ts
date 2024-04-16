import { nativeModule } from '../nativeModule';
import type { PushNotificationPermissions } from '../types';

export const requestPermissions = async (
  { alert = true, badge = true, sound = true }: PushNotificationPermissions = {
    alert: true,
    badge: true,
    sound: true,
  }
): Promise<boolean> =>
  nativeModule.requestPermissions({
    alert,
    badge,
    sound,
  });
