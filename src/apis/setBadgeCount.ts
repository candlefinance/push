import { nativeModule } from '../nativeModule';

export const setBadgeCount = (count: number): void =>
  nativeModule.setBadgeCount?.(count);
