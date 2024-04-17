import {
  addMessageEventListener,
  addTokenEventListener,
  addErrorListener,
  completeNotification,
  getBadgeCount,
  getConstants,
  getLaunchNotification,
  getPermissionStatus,
  registerHeadlessTask,
  requestPermissions,
  setBadgeCount,
  registerForToken,
  removeListeners,
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
  removeListeners,
  addErrorListener,
};

export type PushNotificationModule = typeof module;
export { module };
