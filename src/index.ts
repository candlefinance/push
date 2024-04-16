import {
  addMessageEventListener,
  addTokenEventListener,
  completeNotification,
  getBadgeCount,
  getConstants,
  getLaunchNotification,
  getPermissionStatus,
  registerHeadlessTask,
  requestPermissions,
  setBadgeCount,
} from './apis';

export type {
  PushNotificationMessage,
  PushNotificationPermissionStatus,
  PushNotificationPermissions,
} from './types';

const module = {
  addMessageEventListener,
  addTokenEventListener,
  completeNotification,
  getBadgeCount,
  getConstants,
  getLaunchNotification,
  getPermissionStatus,
  registerHeadlessTask,
  requestPermissions,
  setBadgeCount,
};

export type PushNotificationModule = typeof module;
export { module };
