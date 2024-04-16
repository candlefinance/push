import { nativeModule } from '../nativeModule';

export const getBadgeCount = (): void | Promise<number | null> =>
  nativeModule.getBadgeCount?.();
