import UIKit
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

struct NotificationPayload {
    let kind: NotificationKind
    let userInfo: [AnyHashable: Any]
}

public class SharedPush: NSObject {
    static var sharedInstance: SharedPush = .init()
    var isObserving: Bool = false
    var notificationCallbackDictionary: [String: () -> Void] = [:]
    public var emitter: RCTEventEmitter?
    public var logger: Logger?
    var queue: [NotificationPayload] = []
}

@objc(Push)
final public class Push: RCTEventEmitter {
    public lazy var shared = SharedPush.sharedInstance
    
    override public init() {
        super.init()
        shared.emitter = self
    }
    
    @objc public override func startObserving() {
        super.startObserving()
        shared.isObserving = true
        if !shared.queue.isEmpty {
            shared.queue.forEach(handleRemoteNotificationReceived(payload:))
            shared.queue.removeAll()
        }
        shared.logger?.track(event: #function)
    }
    
    @objc public override func stopObserving() {
        super.stopObserving()
        shared.isObserving = false
        shared.logger?.track(event: #function)
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
        shared.logger?.track(event: #function)
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
        shared.logger?.track(event: #function)
    }
    
    @objc(getAuthorizationStatus:withRejecter:)
    func getAuthorizationStatus(resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        shared.logger?.track(event: #function)
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
        shared.logger?.track(event: #function)
    }
    
    @objc(isRegisteredForRemoteNotifications:withRejecter:)
    func isRegisteredForRemoteNotifications(resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        DispatchQueue.main.async {
            let value = UIApplication.shared.isRegisteredForRemoteNotifications
            resolve(value)
        }
        shared.logger?.track(event: #function)
    }
    
}

enum NotificationKind {
    case opened, foreground
    case background((UIBackgroundFetchResult) -> Void)
    
    var rawValue: String {
        switch self {
        case .foreground: "foreground"
        case .opened: "opened"
        case .background: "background"
        }
    }
}

extension Push {
    
    func handleRemoteNotificationReceived(
        payload: NotificationPayload
    ) {
        guard shared.isObserving else {
            let message = "Warning: Not observing for kind: \(payload.kind.rawValue). \(#function). Adding to the queue."
            shared.queue.append(payload)
            shared.logger?.track(event: message)
            return
        }
        let kind = payload.kind
        let userInfo = payload.userInfo

        switch kind {
        case .foreground:
            if let emitter = shared.emitter {
                emitter.sendEvent(withName: NotificationType.notificationReceived.rawValue, body: ["payload": userInfo, "kind": kind.rawValue])
            } else {
                shared.logger?.track(event: "Fatal: Emitter not found for kind: \(kind). \(#function)")
            }
        case .background(let completion):
            let uuid = UUID().uuidString
            shared.notificationCallbackDictionary[uuid] = {
                completion(.newData)
            }
            DispatchQueue.main.asyncAfter(deadline: .now() + 29) { [weak self] in
                if let callback = self?.shared.notificationCallbackDictionary[uuid] {
                    callback()
                    self?.shared.notificationCallbackDictionary.removeValue(forKey: uuid)
                    self?.shared.logger?.track(event: "Fatal: Completion handler called w/o user doing so. \(#function)")
                }
            }
            if let emitter = shared.emitter {
                emitter.sendEvent(withName: NotificationType.notificationReceived.rawValue, body: ["payload": userInfo, "uuid": uuid, "kind": kind.rawValue])
            } else {
                shared.logger?.track(event: "Fatal: Emitter not found for kind: \(kind.rawValue). \(#function)")
            }
        case .opened:
            if let emitter = shared.emitter {
                emitter.sendEvent(withName: NotificationType.notificationReceived.rawValue, body: ["payload": userInfo, "kind": kind.rawValue])
            } else {
                shared.logger?.track(event: "Fatal: Emitter not found for kind: \(kind.rawValue). \(#function)")
            }
        }
        
    }
    
    func handleRemoteNotificationsRegistered(deviceToken: String) {
        if let emitter = shared.emitter {
            emitter.sendEvent(withName: NotificationType.deviceTokenReceived.rawValue, body: deviceToken)
        } else {
            shared.logger?.track(event: "Fatal: Emitter not found. \(#function)")
        }
    }
    
    func handleRemoteNotificationRegistrationError(error: String) {
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
        shared.logger?.track(event: "Device token received: \(token)")
        handleRemoteNotificationsRegistered(deviceToken: token)
    }
    
    public func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
        shared.logger?.track(event: "Error registering for remote notifications: \(error.localizedDescription)")
        handleRemoteNotificationRegistrationError(error: error.localizedDescription)
    }
    
    public func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification, withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        handleRemoteNotificationReceived(payload: .init(kind: .foreground, userInfo: notification.request.content.userInfo))
        completionHandler([.banner, .sound, .badge, .list])
    }
    
    public func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse) async {
        handleRemoteNotificationReceived(
            payload: .init(kind: .opened,
                           userInfo: response.notification.request.content.userInfo)
        )
    }
    
    public func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable: Any], fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {
        handleRemoteNotificationReceived(payload:
                .init(
                    kind: .background(completionHandler),
                    userInfo: userInfo
                )
        )
    }
    
}
