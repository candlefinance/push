import { NativeEventEmitter, NativeModules, Platform } from 'react-native';

export type NotificationType =
  | 'notificationReceived'
  | 'deviceTokenReceived'
  | 'errorReceived';

export type AuthorizationStatus =
  | 'authorized'
  | 'denied'
  | 'ephemeral'
  | 'notDetermined'
  | 'provisional';

export type NotificationReceivedData = {
  kind: 'foreground' | 'background' | 'opened';
  payload: {
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
  };
  uuid?: string;
};

type DeviceTokenReceivedData = string;
type ErrorReceivedData = string;

interface NotificationCallbacks {
  notificationReceived: (data: NotificationReceivedData) => void;
  deviceTokenReceived: (data: DeviceTokenReceivedData) => void;
  errorReceived: (data: ErrorReceivedData) => void;
}

class Push {
  module: any;
  private bridge?: NativeEventEmitter;

  public constructor() {
    if (Platform.OS === 'ios') {
      const module = NativeModules.Push;
      this.module = module;
      this.bridge = new NativeEventEmitter(module);
    }
  }

  public async requestPermissions(): Promise<boolean> {
    if (Platform.OS === 'ios') {
      const result = await this.module.requestPermissions();
      return result;
    }
    return false;
  }

  public async registerForToken(): Promise<boolean> {
    if (Platform.OS === 'ios') {
      return this.module.registerForToken();
    }
    return false;
  }

  public async isRegisteredForRemoteNotifications(): Promise<boolean> {
    if (Platform.OS === 'ios') {
      return this.module.isRegisteredForRemoteNotifications();
    }
    return false;
  }

  public async onFinish(uuid: string): Promise<void> {
    if (Platform.OS === 'ios') {
      await this.module.onFinish(uuid);
    }
  }

  // https://developer.apple.com/documentation/usernotifications/unauthorizationstatus?ref=createwithswift.com
  public async getAuthorizationStatus(): Promise<AuthorizationStatus> {
    if (Platform.OS === 'ios') {
      const value: number = await this.module.getAuthorizationStatus();
      switch (value) {
        case 0:
          return 'notDetermined';
        case 1:
          return 'denied';
        case 2:
          return 'authorized';
        case 3:
          return 'provisional';
        case 4:
          return 'ephemeral';
        default:
          return 'notDetermined';
      }
    }
    return 'denied';
  }

  public addListener<T extends keyof NotificationCallbacks>(
    event: T,
    callback: NotificationCallbacks[T]
  ): void {
    if (Platform.OS === 'ios') {
      this.bridge?.addListener(event, callback);
    }
  }

  public removeListener<T extends keyof NotificationCallbacks>(event: T): void {
    if (Platform.OS === 'ios') {
      this.bridge?.removeAllListeners(event);
    }
  }
}

const push = new Push();
export default push;
