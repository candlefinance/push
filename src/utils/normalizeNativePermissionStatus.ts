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
      return 'shouldRequest';
    case 'NotDetermined':
    case 'ShouldExplainThenRequest':
      return 'shouldExplainThenRequest';
    case 'Authorized':
    case 'Granted':
      return 'granted';
    case 'Denied':
      return 'denied';
  }
};
