export interface PushNotificationPermissions
  extends Partial<Record<string, boolean>> {
  alert?: boolean;
  badge?: boolean;
  sound?: boolean;
}

export interface PushNotificationMessage {
  title?: string;
  body?: string;
  imageUrl?: string;
  deeplinkUrl?: string;
  goToUrl?: string;
  fcmOptions?: FcmPlatformOptions;
  apnsOptions?: ApnsPlatformOptions;
  data?: Record<string, unknown>;
  custom?: any;
}

export type PushNotificationPermissionStatus =
  | 'denied'
  | 'granted'
  | 'notDetermined';

interface ApnsPlatformOptions {
  subtitle?: string;
}

interface FcmPlatformOptions {
  channelId: string;
  messageId: string;
  senderId: string;
  sendTime: Date;
}
