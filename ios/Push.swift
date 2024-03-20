import UIKit
import NotificationCenter
import React

enum NotificationType: String, CaseIterable {
    case notificationReceived, deviceTokenReceived, errorReceived
}

struct Action {
    let type: NotificationType
    let payload: Any!
}

public protocol Logger {
    func track(event: String)
}

public class SharedPush: NSObject {
    
    static var sharedInstance: SharedPush = .init()
    
    var notificationCallbackDictionary: [String: () -> Void] = [:]
    public var emitter: RCTEventEmitter?
    public var logger: Logger?
}

@objc(Push)
final public class Push: RCTEventEmitter {
    public lazy var shared = SharedPush.sharedInstance
    
    override public init() {
        super.init()
        shared.emitter = self
    }
    
    @objc public override func startObserving() {
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleRemoteNotificationReceived),
            name: Notification.Name(NotificationType.notificationReceived.rawValue),
            object: nil
        )
        
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleRemoteNotificationsRegistered),
            name: Notification.Name(NotificationType.deviceTokenReceived.rawValue),
            object: nil
        )
        
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleRemoteNotificationRegistrationError),
            name: Notification.Name(NotificationType.errorReceived.rawValue),
            object: nil
        )
        
        shared.logger?.track(event: "\(#function)")
    }
    
    @objc public override func stopObserving() {
        shared.logger?.track(event: "\(#function)")
        NotificationCenter.default.removeObserver(self)
    }
    
    public override func supportedEvents() -> [String]! {
        return NotificationType.allCases.map { $0.rawValue }
    }
    
    @objc(onFinish:withResolver:withRejecter:)
    public func onFinish(uuid: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
        if let callback = shared.notificationCallbackDictionary[uuid] {
            callback()
            shared.notificationCallbackDictionary.removeValue(forKey: uuid)
        }
        shared.logger?.track(event: "\(#function)")
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
        shared.logger?.track(event: "\(#function)")
    }
    
    @objc(getAuthorizationStatus:withRejecter:)
    func getAuthorizationStatus(resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        shared.logger?.track(event: "\(#function)")
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
        shared.logger?.track(event: "\(#function)")
    }
    
    @objc(isRegisteredForRemoteNotifications:withRejecter:)
    func isRegisteredForRemoteNotifications(resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        DispatchQueue.main.async {
            let value = UIApplication.shared.isRegisteredForRemoteNotifications
            resolve(value)
        }
        shared.logger?.track(event: "\(#function)")
    }
    
}

extension Push {
    
    @objc func handleRemoteNotificationReceived(_ notification: Notification) {
        
        guard let payload = notification.userInfo?["payload"] as? [AnyHashable: Any] else {
            shared.logger?.track(event: "Fatal: Payload not found. \(#function)")
            return
        }
        
        guard let kind = notification.userInfo?["kind"] as? String else {
            shared.logger?.track(event: "Fatal: Kind not found. \(#function)")
            return
        }
        
        if kind == "foreground" {
            if let emitter = shared.emitter {
                emitter.sendEvent(withName: NotificationType.notificationReceived.rawValue, body: ["payload": payload, "kind": "foreground"])
            } else {
                shared.logger?.track(event: "Fatal: Emitter not found. \(#function)")
            }
        } else if kind == "background" {
            guard let completionHandler = notification.userInfo?["completionHandler"] as? (UIBackgroundFetchResult) -> Void else {
                shared.logger?.track(event: "Fatal: Completion handler not found. \(#function)")
                return
            }
            let uuid = UUID().uuidString
            shared.notificationCallbackDictionary[uuid] = {
                completionHandler(.newData)
            }
            DispatchQueue.main.asyncAfter(deadline: .now() + 29) { [weak self] in
                if let callback = self?.shared.notificationCallbackDictionary[uuid] {
                    callback()
                    self?.shared.notificationCallbackDictionary.removeValue(forKey: uuid)
                    self?.shared.logger?.track(event: "Fatal: Completion handler called w/o user doing so. \(#function)")
                }
            }
            if let emitter = shared.emitter {
                emitter.sendEvent(withName: NotificationType.notificationReceived.rawValue, body: ["payload": payload, "uuid": uuid, "kind": "background"])
            } else {
                shared.logger?.track(event: "Fatal: Emitter not found. \(#function)")
            }
        } else if kind == "opened" {
            if let emitter = shared.emitter {
                emitter.sendEvent(withName: NotificationType.notificationReceived.rawValue, body: ["payload": payload, "kind": "opened"])
            } else {
                shared.logger?.track(event: "Fatal: Emitter not found. \(#function)")
            }
        }
        
    }
    
    @objc func handleRemoteNotificationsRegistered(_ notification: Notification) {
        guard let deviceToken = notification.userInfo?["deviceToken"] as? String else {
            shared.logger?.track(event: "Fatal: Device token not found. \(#function)")
            return
        }
        
        if let emitter = shared.emitter {
            emitter.sendEvent(withName: NotificationType.deviceTokenReceived.rawValue, body: deviceToken)
        } else {
            shared.logger?.track(event: "Fatal: Emitter not found. \(#function)")
        }
    }
    
    @objc func handleRemoteNotificationRegistrationError(_ notification: Notification) {
        guard let error = notification.userInfo?["error"] as? String else {
            shared.logger?.track(event: "Fatal: error not found. \(#function)")
            return
        }
        
        if let emitter = shared.emitter {
            emitter.sendEvent(withName: NotificationType.errorReceived.rawValue, body: error)
        } else {
            shared.logger?.track(event: "Fatal: Emitter not found. \(#function)")
        }
    }
    
}

extension Push: UNUserNotificationCenterDelegate {
    
    public func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        let tokenParts = deviceToken.map { data -> String in
            return String(format: "%02x", data)
        }
        let token = tokenParts.joined()
        NotificationCenter.default.post(
            name: Notification.Name(NotificationType.deviceTokenReceived.rawValue),
            object: nil,
            userInfo: ["deviceToken": token]
        )
    }
    
    public func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
        NotificationCenter.default.post(
            name: Notification.Name(NotificationType.errorReceived.rawValue),
            object: nil,
            userInfo: ["error": error.localizedDescription]
        )
    }
    
    public func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification, withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        NotificationCenter.default.post(
            name: Notification.Name(NotificationType.notificationReceived.rawValue),
            object: nil,
            userInfo: ["payload": notification.request.content.userInfo, "kind": "foreground"]
        )
        completionHandler([.banner, .sound, .badge, .list])
    }
    
    public func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse) async {
        NotificationCenter.default.post(
            name: Notification.Name(NotificationType.notificationReceived.rawValue),
            object: nil,
            userInfo: ["payload": response.notification.request.content.userInfo, "kind": "opened"]
        )
    }
    
    public func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable: Any], fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {
        NotificationCenter.default.post(
            name: Notification.Name(NotificationType.notificationReceived.rawValue),
            object: nil,
            userInfo: ["payload": userInfo, "kind": "background", "completionHandler": completionHandler]
        )
    }
    
}
