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
} & (
  | {
      kind: 'opened';
    }
  | { kind: 'background'; uuid: string }
  | { kind: 'foreground' }
);

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
    const module = NativeModules.Push;
    this.module = module;
    this.bridge = new NativeEventEmitter(module);
  }

  public async requestPermissions(): Promise<boolean> {
    const result = await this.module.requestPermissions();
    return result;
  }

  public async registerForToken(): Promise<boolean> {
    return this.module.registerForToken();
  }

  public async isRegisteredForRemoteNotifications(): Promise<boolean> {
    return this.module.isRegisteredForRemoteNotifications();
  }

  public async onFinish(uuid: string): Promise<void> {
    if (Platform.OS === 'ios') {
      await this.module.onFinish(uuid);
    }
  }

  public async getAuthorizationStatus(): Promise<AuthorizationStatus> {
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

  public addListener<T extends keyof NotificationCallbacks>(
    event: T,
    callback: NotificationCallbacks[T]
  ): void {
    this.bridge?.addListener(event, callback);
  }

  public removeListener<T extends keyof NotificationCallbacks>(event: T): void {
    this.bridge?.removeAllListeners(event);
  }
}

const push = new Push();
export default push;
