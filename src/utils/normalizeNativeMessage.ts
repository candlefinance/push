function isEmpty(obj: Record<string, unknown>): boolean {
  for (let key in obj) {
    if (obj.hasOwnProperty(key)) return false;
  }
  return true;
}

import type {
  ApnsMessage,
  FcmMessage,
  NativeMessage,
  NormalizedValues,
  PushNotificationMessage,
} from '../types';

/**
 * @internal
 */
export const normalizeNativeMessage = (
  nativeMessage?: NativeMessage
): PushNotificationMessage | null => {
  let normalized: NormalizedValues;
  if (isApnsMessage(nativeMessage)) {
    normalized = normalizeApnsMessage(nativeMessage);
  } else if (isFcmMessage(nativeMessage)) {
    normalized = normalizeFcmMessage(nativeMessage);
  } else {
    return null;
  }
  const { body, imageUrl, title, options, data, custom } = normalized;

  return {
    body,
    data,
    imageUrl,
    title,
    custom,
    ...options,
  };
};

const normalizeApnsMessage = (apnsMessage: ApnsMessage): NormalizedValues => {
  const { aps, data, custom } = apnsMessage;
  const { body, title } = aps.alert ?? {};
  const options = getApnsOptions(apnsMessage);

  return { body, title, options, data, custom: custom };
};

const normalizeFcmMessage = (fcmMessage: FcmMessage): NormalizedValues => {
  const { body, imageUrl, rawData: data, title, subtitle, custom } = fcmMessage;
  const options = getFcmOptions(fcmMessage);

  return { body, imageUrl, title, options, data, custom, subtitle };
};

const getApnsOptions = ({
  aps,
}: ApnsMessage): Pick<PushNotificationMessage, 'apnsOptions'> => {
  const { subtitle } = aps.alert ?? {};
  const apnsOptions = { ...(subtitle && { subtitle }) };

  return { ...(!isEmpty(apnsOptions) && { apnsOptions }) };
};

const getFcmOptions = ({
  channelId = '',
  messageId = '',
  senderId = '',
  sendTime = new Date().getTime(),
}: FcmMessage): Pick<PushNotificationMessage, 'fcmOptions'> => {
  const fcmOptions = {
    channelId,
    messageId,
    senderId,
    sendTime: new Date(sendTime),
  };

  return { ...(!isEmpty(fcmOptions) && { fcmOptions }) };
};

const isApnsMessage = (
  nativeMessage?: NativeMessage
): nativeMessage is ApnsMessage => !!nativeMessage?.aps;

const isFcmMessage = (
  nativeMessage?: NativeMessage
): nativeMessage is FcmMessage => !!nativeMessage?.rawData;
