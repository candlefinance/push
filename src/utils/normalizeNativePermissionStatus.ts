import type {
  NativePermissionStatus,
  PushNotificationPermissionStatus,
} from '../types';

/**
 * @internal
 */
export const normalizeNativePermissionStatus = (
  nativeStatus: NativePermissionStatus
): PushNotificationPermissionStatus => {
  switch (nativeStatus) {
    case 'ShouldRequest':
    case 'NotDetermined':
    case 'ShouldExplainThenRequest':
      return 'notDetermined';
    case 'Authorized':
    case 'Granted':
      return 'granted';
    case 'Denied':
      return 'denied';
  }
};
