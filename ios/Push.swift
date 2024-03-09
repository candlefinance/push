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
    
    static var sharedInstance: SharedPush? = {
        let instance = SharedPush()
        return instance
    }()
    
    var didInitialize = false
    var notificationCallbackDictionary: [String: () -> Void] = [:]
    @objc public var emitter: RCTEventEmitter?
    var queue: [Action] = []
    var logger: Logger?
    
    public func initPush(logger: Logger?) {
        guard !didInitialize else {
            logger?.track(event: "\(#function) didInitialize: \(didInitialize)")
            return
        }
        Self.sharedInstance = self
        didInitialize = true
        self.logger = logger
        logger?.track(event: "\(#function) didInitialize: \(true)")
    }
}

@objc(Push)
final public class Push: RCTEventEmitter {
    public lazy var shared = SharedPush.sharedInstance
    
    override public init() {
        super.init()
        shared?.emitter = self
        shared?.logger?.track(event: "\(#function)")
    }
    
    private func sendStoreAction(_ action: Action) {
        shared?.logger?.track(event: "\(#function)")
        if let emitter = shared?.emitter {
            emitter.sendEvent(withName: action.type.rawValue, body: action.payload)
        }
    }
    
    @objc public func dispatch(type: String, payload: Any!) {
        shared?.logger?.track(event: "\(#function): \(type)")
        let actionObj = Action(type: .init(rawValue: type) ?? .errorReceived, payload: payload)
        if let didInitialize = shared?.didInitialize, didInitialize {
            sendStoreAction(actionObj)
        } else {
            shared?.queue.append(actionObj)
        }
    }
    
    @objc public override func startObserving() {
        shared?.logger?.track(event: "\(#function)")
        for event in (shared?.queue ?? []) {
            sendStoreAction(event)
        }
        shared?.queue = []
    }
    
    @objc public override func stopObserving() {
        shared?.logger?.track(event: "\(#function)")
    }
    
    public override func supportedEvents() -> [String]! {
        return NotificationType.allCases.map { $0.rawValue }
    }
    
    @objc(onFinish:withResolver:withRejecter:)
    public func onFinish(uuid: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
        if let callback = shared?.notificationCallbackDictionary[uuid] {
            callback()
            shared?.notificationCallbackDictionary.removeValue(forKey: uuid)
        }
        shared?.logger?.track(event: "\(#function)")
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
        shared?.logger?.track(event: "\(#function)")
    }
    
    @objc(getAuthorizationStatus:withRejecter:)
    func getAuthorizationStatus(resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        shared?.logger?.track(event: "\(#function)")
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
        shared?.logger?.track(event: "\(#function)")
    }
    
    @objc(isRegisteredForRemoteNotifications:withRejecter:)
    func isRegisteredForRemoteNotifications(resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        DispatchQueue.main.async {
            let value = UIApplication.shared.isRegisteredForRemoteNotifications
            resolve(value)
        }
        shared?.logger?.track(event: "\(#function)")
    }
    
}

extension Push: UNUserNotificationCenterDelegate {
    
    public func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        let tokenParts = deviceToken.map { data -> String in
            return String(format: "%02x", data)
        }
        let token = tokenParts.joined()
        dispatch(type: NotificationType.deviceTokenReceived.rawValue, payload: token)
        shared?.logger?.track(event: "\(#function)")
    }
    
    public func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
        dispatch(
            type: NotificationType.errorReceived.rawValue,
            payload: error.localizedDescription
        )
        shared?.logger?.track(event: "\(#function)")
    }
    
    public func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification, withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        let uuid = UUID().uuidString
        dispatch(
            type: NotificationType.notificationReceived.rawValue,
            payload: ["payload": notification.request.content.userInfo, "uuid": uuid, "kind": "foreground"]
        )
        shared?.notificationCallbackDictionary[uuid] = {
            completionHandler([.badge, .banner, .sound, .list])
        }
        DispatchQueue.main.asyncAfter(deadline: .now() + 29) { [weak self] in
            if let callback = self?.shared?.notificationCallbackDictionary[uuid] {
                callback()
                self?.shared?.notificationCallbackDictionary.removeValue(forKey: uuid)
            }
        }
        shared?.logger?.track(event: "\(#function)")
    }
    
    public func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse) async {
        dispatch(
            type: NotificationType.notificationReceived.rawValue,
            payload: ["payload": response.notification.request.content.userInfo, "kind": "opened"]
        )
        shared?.logger?.track(event: "\(#function)")
    }
    
    public func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable: Any], fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {
        if application.applicationState == .active {
            dispatch(
                type: NotificationType.errorReceived.rawValue,
                payload: "Background notification received while the app is foregrounded state, use to handle `notificationReceived` in the foreground."
            )
            completionHandler(.newData)
            return
        }
        let uuid = UUID().uuidString
        dispatch(
            type: NotificationType.notificationReceived.rawValue,
            payload: ["payload": userInfo, "uuid": uuid, "kind": "background"]
        )
        shared?.notificationCallbackDictionary[uuid] = {
            completionHandler(.newData)
        }
        DispatchQueue.main.asyncAfter(deadline: .now() + 29) { [weak self] in
            if let callback = self?.shared?.notificationCallbackDictionary[uuid] {
                callback()
                self?.shared?.notificationCallbackDictionary.removeValue(forKey: uuid)
            }
        }
        shared?.logger?.track(event: "\(#function)")
    }
    
}
