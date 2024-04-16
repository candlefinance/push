import { nativeModule } from '../nativeModule';

export const completeNotification = (completionHandlerId: string): void =>
  nativeModule.completeNotification?.(completionHandlerId);
