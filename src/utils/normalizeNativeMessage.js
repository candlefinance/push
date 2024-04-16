function isEmpty(obj) {
    for (let key in obj) {
        if (obj.hasOwnProperty(key))
            return false;
    }
    return true;
}
/**
 * @internal
 */
export const normalizeNativeMessage = (nativeMessage) => {
    let normalized;
    if (isApnsMessage(nativeMessage)) {
        normalized = normalizeApnsMessage(nativeMessage);
    }
    else if (isFcmMessage(nativeMessage)) {
        normalized = normalizeFcmMessage(nativeMessage);
    }
    else {
        return null;
    }
    const { body, imageUrl, title, options, data } = normalized;
    return {
        body,
        data,
        imageUrl,
        title,
        ...options,
    };
};
const normalizeApnsMessage = (apnsMessage) => {
    const { aps, data, custom } = apnsMessage;
    const { body, title } = aps.alert ?? {};
    const options = getApnsOptions(apnsMessage);
    return { body, title, options, data, custom };
};
const normalizeFcmMessage = (fcmMessage) => {
    const { body, imageUrl, rawData: data, title, subtitle, custom } = fcmMessage;
    const options = getFcmOptions(fcmMessage);
    return { body, imageUrl, title, options, data, custom, subtitle };
};
const getApnsOptions = ({ aps, }) => {
    const { subtitle } = aps.alert ?? {};
    const apnsOptions = { ...(subtitle && { subtitle }) };
    return { ...(!isEmpty(apnsOptions) && { apnsOptions }) };
};
const getFcmOptions = ({ channelId = '', messageId = '', senderId = '', sendTime = new Date().getTime(), }) => {
    const fcmOptions = {
        channelId,
        messageId,
        senderId,
        sendTime: new Date(sendTime),
    };
    return { ...(!isEmpty(fcmOptions) && { fcmOptions }) };
};
const isApnsMessage = (nativeMessage) => !!nativeMessage?.aps;
const isFcmMessage = (nativeMessage) => !!nativeMessage?.rawData;
