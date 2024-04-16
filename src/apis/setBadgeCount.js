import { nativeModule } from '../nativeModule';
export const setBadgeCount = (count) => nativeModule.setBadgeCount?.(count);
