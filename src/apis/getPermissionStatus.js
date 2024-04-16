import { nativeModule } from '../nativeModule';
import { normalizeNativePermissionStatus } from '../utils';
export const getPermissionStatus = async () => normalizeNativePermissionStatus(await nativeModule.getPermissionStatus());
