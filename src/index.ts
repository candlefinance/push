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
  registerForToken,
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
  registerForToken,
};

export type PushNotificationModule = typeof module;
export { module };
