import { nativeModule } from '../nativeModule';
export const requestPermissions = async ({ alert = true, badge = true, sound = true } = {
    alert: true,
    badge: true,
    sound: true,
}) => nativeModule.requestPermissions({
    alert,
    badge,
    sound,
});
