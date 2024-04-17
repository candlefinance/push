import type { NativeModule } from 'react-native';

import type {
  PushNotificationMessage,
  PushNotificationPermissions,
} from './module';

export interface PushNotificationNativeModule extends NativeModule {
  completeNotification?(completionHandlerId: string): void;
  getConstants(): {
    NativeEvent: {
      BACKGROUND_MESSAGE_RECEIVED: string;
      FOREGROUND_MESSAGE_RECEIVED: string;
      LAUNCH_NOTIFICATION_OPENED: string;
      NOTIFICATION_OPENED: string;
      TOKEN_RECEIVED: string;
      FAILED_TO_REGISTER: string;
    };
    NativeHeadlessTaskKey?: string;
  };
  getLaunchNotification(): Promise<NativeMessage | null>;
  getBadgeCount?(): Promise<number>;
  setBadgeCount?(count: number): void;
  getPermissionStatus(): Promise<NativePermissionStatus>;
  requestPermissions(
    permissions: PushNotificationPermissions
  ): Promise<boolean>;
  registerForToken: () => void;
}

export interface NativeAction {
  deeplink?: string;
  url?: string;
}

export type NativeMessage = (ApnsMessage | FcmMessage) & {
  token?: never;
};

export type NativePermissionStatus =
  | AndroidPermissionStatus
  | IosPermissionStatus;

export interface NormalizedValues {
  body?: string;
  imageUrl?: string;
  title?: string;
  subtitle?: string;
  options?: Pick<PushNotificationMessage, 'apnsOptions' | 'fcmOptions'>;
  data?: Record<string, unknown>;
  custom?: any;
}

// iOS
export interface ApnsMessage {
  aps: {
    'alert'?: {
      title?: string;
      body?: string;
      subtitle?: string;
    };
    'sound'?: string;
    'badge'?: number;
    'content-available'?: number;
    'category'?: string;
  };
  custom?: any;
  rawData?: never;
  completionHandlerId?: string;
  data?: Record<string, unknown>;
}

export type IosPermissionStatus = 'NotDetermined' | 'Authorized' | 'Denied';

// Android
export interface FcmMessage {
  action?: NativeAction;
  aps?: never;
  body?: string;
  imageUrl?: string;
  rawData?: Record<string, unknown>;
  title?: string;
  subtitle?: string;
  channelId?: string;
  messageId?: string;
  senderId?: string;
  sendTime?: number;
  completionHandlerId?: never;
  custom?: any;
}

export type AndroidPermissionStatus =
  | 'ShouldRequest'
  | 'ShouldExplainThenRequest'
  | 'Granted'
  | 'Denied';

export interface TokenPayload {
  token: string;
}
