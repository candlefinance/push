import { NativeModules, Platform, NativeEventEmitter } from 'react-native';

class Push {
  module = NativeModules.Push;
  private bridge?: NativeEventEmitter;

  public constructor() {
    if (Platform.OS === 'ios') {
      this.module = NativeModules.Push;
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

  public addListener(event: string, callback: (data: any) => void): void {
    this.bridge?.addListener(event, callback);
  }

  public removeListener(event: string): void {
    this.bridge?.removeAllListeners(event);
  }
}

export default new Push();
