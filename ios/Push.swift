import UIKit
import NotificationCenter
import React

@objc(Push)
class Push: RCTEventEmitter {
    public static let shared = Push()

    private var notificationCallbackDictionary: [String: () -> Void] = [:]
    
    override func supportedEvents() -> [String]! {
      return ["notificationReceived", "deviceTokenReceived"]
    }
    
    @objc
    public func sendNotificationEvent(notification: UNNotification) {
        Self.shared.sendEvent(withName: "notificationReceived", body: notification.request.content.userInfo)
    }

    @objc
    func sendTokenEvent(token: String) {
        Self.shared.sendEvent(withName: "deviceTokenReceived", body: token)
    }
    
    @objc(onFinish:withResolver:withRejecter:)
    public func onFinish(uuid: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
        if let callback = notificationCallbackDictionary[uuid] {
            callback()
            notificationCallbackDictionary.removeValue(forKey: uuid)
        }
    }

    @objc
    public func addNotificationCallback(uuid: String, callback: @escaping () -> Void) {
        notificationCallbackDictionary[uuid] = callback
    }

    @objc(requestPermissions:withRejecter:)
    public func requestPermissions(resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
        DispatchQueue.main.async {
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

    @objc
    static public func registerForToken() {
        DispatchQueue.main.async {
            UIApplication.shared.registerForRemoteNotifications()
        }
    }

    @objc
    static public func isRegisteredForRemoteNotifications() -> Bool {
      return UIApplication.shared.isRegisteredForRemoteNotifications
    }

    @objc
    public func setTimer(for seconds: Double = 30, callback: @escaping () -> Void) {
        DispatchQueue.main.asyncAfter(deadline: .now() + seconds) {
            callback()
        }
    }
    
}
