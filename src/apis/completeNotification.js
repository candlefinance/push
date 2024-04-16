import { nativeModule } from '../nativeModule';
export const completeNotification = (completionHandlerId) => nativeModule.completeNotification?.(completionHandlerId);
