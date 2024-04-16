import Foundation

@objc(PushNotificationAppDelegateHelper)
public class PushNotificationAppDelegateHelper: NSObject {
    @objc
    static public func didRegisterForRemoteNotificationsWithDeviceToken(_ deviceToken: Data) {
        PushNotificationManager
            .shared
            .didRegisterForRemoteNotificationsWithDeviceToken(deviceToken: deviceToken)
    }

    @objc
    static public func didFailToRegisterForRemoteNotificationsWithError(_ error: Error) {
        PushNotificationManager
            .shared
            .didFailToRegisterForRemoteNotificationsWithError(error: error)
    }

    @objc
    static public func didReceiveRemoteNotification(
        userInfo: [AnyHashable: Any],
        completionHandler: @escaping (UIBackgroundFetchResult) -> Void
    ) {
        PushNotificationManager
            .shared
            .didReceiveRemoteNotification(userInfo: userInfo, completionHandler: completionHandler)
    }
}
