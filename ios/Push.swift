import UIKit
import NotificationCenter
import React

@objc(Push)
final public class Push: RCTEventEmitter {
    
    private var notificationCallbackDictionary: [String: () -> Void] = [:]
    private static var isInitialized = false
    @objc public static var emitter: RCTEventEmitter?
    private static var queue: [Action] = []
   
    override public init() {
      super.init()
      Self.emitter = self
    }
    
    enum NotificationType: String, CaseIterable {
        case notificationReceived, deviceTokenReceived, errorReceived
    }
    
    struct Action {
        let type: NotificationType
        let payload: Any!
    }
    
    private static func sendStoreAction(_ action: Action) {
        if let emitter = self.emitter {
            emitter.sendEvent(withName: action.type.rawValue, body: action.payload)
        }
    }
    
    @objc public static func dispatch(type: String, payload: Any!) {
        let actionObj = Action(type: .init(rawValue: type) ?? .errorReceived, payload: payload)
        if isInitialized {
            sendStoreAction(actionObj)
        } else {
            queue.append(actionObj)
        }
    }
    
    @objc public override func startObserving() {
          Self.isInitialized = true
          for event in Self.queue {
              Self.sendStoreAction(event)
          }
          Self.queue = []
      }
      
      @objc public override func stopObserving() {
          Self.isInitialized = false
      }
    
    public override func supportedEvents() -> [String]! {
        return NotificationType.allCases.map { $0.rawValue }
    }
    
    @objc(onFinish:withResolver:withRejecter:)
    public func onFinish(uuid: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
        if let callback = notificationCallbackDictionary[uuid] {
            callback()
            notificationCallbackDictionary.removeValue(forKey: uuid)
        }
    }
    
    @objc(requestPermissions:withRejecter:)
    func requestPermissions(resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        DispatchQueue.main.async {
            UIApplication.shared.registerForRemoteNotifications()
            UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { granted, error in
                if let error = error {
                    reject("permission_error", error.localizedDescription, error)
                } else if granted {
                    resolve(true)
                } else {
                    reject("permission_error", "Permission denied", nil)
                }
            }
        }
    }
    
    @objc(getAuthorizationStatus:withRejecter:)
    func getAuthorizationStatus(resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        DispatchQueue.main.async {
            UNUserNotificationCenter.current().getNotificationSettings { settings in
                resolve(settings.authorizationStatus.rawValue)
            }
        }
    }
    
    @objc(registerForToken:withRejecter:)
    func registerForToken(resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        DispatchQueue.main.async {
            UIApplication.shared.registerForRemoteNotifications()
            resolve(true)
        }
    }
    
    @objc(isRegisteredForRemoteNotifications:withRejecter:)
    func isRegisteredForRemoteNotifications(resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        DispatchQueue.main.async {
            let value = UIApplication.shared.isRegisteredForRemoteNotifications
            resolve(value)
        }
    }
    
}

extension Push: UNUserNotificationCenterDelegate {
    
    public func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        let tokenParts = deviceToken.map { data -> String in
            return String(format: "%02x", data)
        }
        let token = tokenParts.joined()
        Self.dispatch(type: NotificationType.deviceTokenReceived.rawValue, payload: token)
    }
    
    public func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
        Self.dispatch(
            type: NotificationType.errorReceived.rawValue,
            payload: error.localizedDescription
        )
    }
    
    public func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification, withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        let uuid = UUID().uuidString
        Self.dispatch(
            type: NotificationType.notificationReceived.rawValue,
            payload: ["payload": notification.request.content.userInfo, "uuid": uuid, "kind": "foreground"]
        )
        notificationCallbackDictionary[uuid] = {
            completionHandler([.badge, .banner, .sound, .list])
        }
        DispatchQueue.main.asyncAfter(deadline: .now() + 29) {
            if let callback = self.notificationCallbackDictionary[uuid] {
                callback()
                self.notificationCallbackDictionary.removeValue(forKey: uuid)
            }
        }
    }
    
    public func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse) async {
        Self.dispatch(
            type: NotificationType.notificationReceived.rawValue,
            payload: ["payload": response.notification.request.content.userInfo, "kind": "opened"]
        )
    }
    
    public func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable: Any], fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {
        if application.applicationState == .active {
            Self.dispatch(
                type: NotificationType.errorReceived.rawValue,
                payload: "Background notification received while the app is foregrounded state, use to handle `notificationReceived` in the foreground."
            )
            completionHandler(.newData)
            return
        }
        let uuid = UUID().uuidString
        Self.dispatch(
            type: NotificationType.notificationReceived.rawValue,
            payload: ["payload": userInfo, "uuid": uuid, "kind": "background"]
        )
        notificationCallbackDictionary[uuid] = {
            completionHandler(.newData)
        }
        DispatchQueue.main.asyncAfter(deadline: .now() + 29) {
            if let callback = self.notificationCallbackDictionary[uuid] {
                callback()
                self.notificationCallbackDictionary.removeValue(forKey: uuid)
            }
        }
    }
    
}
