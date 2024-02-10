import { NativeModules, Platform, NativeEventEmitter } from 'react-native';

export type NotificationType =
  | 'notificationReceived'
  | 'deviceTokenReceived'
  | 'errorReceived';

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

  public registerForToken(): void {
    if (Platform.OS === 'ios') {
      this.module.registerForToken();
    }
  }

  public isRegisteredForRemoteNotifications(): boolean {
    if (Platform.OS === 'ios') {
      return this.module.isRegisteredForRemoteNotifications();
    }
    return false;
  }

  public addListener(
    event: NotificationType,
    callback: (data: any) => void
  ): void {
    this.bridge?.addListener(event, callback);
  }

  public removeListener(event: NotificationType): void {
    this.bridge?.removeAllListeners(event);
  }

  public async onFinish(uuid: string): Promise<void> {
    if (Platform.OS === 'ios') {
      await this.module.onFinish(uuid);
    }
  }
}

export default Push;
