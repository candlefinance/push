import UIKit
import React

private let expectedEventNames: Set<String> = [
    NativeEvent.tokenReceived.name,
    NativeEvent.backgroundMessageReceived.name,
    NativeEvent.foregroundMessageReceived.name,
    NativeEvent.notificationOpened.name,
    NativeEvent.launchNotificationOpened.name,
    NativeEvent.failedToRegister.name
]

@objc(Push)
final class Push: RCTEventEmitter {
    var registeredEventNames: Set<String> = []
    var hasListeners = false
    private let sharedNotificationManager: PushNotificationManager
    
    // Override the bridge setter and getter to capture the launch options
    override var bridge: RCTBridge! {
        set(bridge) {
            super.bridge = bridge
            
            if let launchOptions = bridge.launchOptions {
                sharedNotificationManager.handleLaunchOptions(launchOptions: launchOptions)
            }
        }
        get {
            return super.bridge
        }
    }
    
    override init() {
        sharedNotificationManager = PushNotificationManager.shared
        super.init()
    }
    
    func notifyFlushQueuedEvents() {
        PushEventManager.shared.setSendEvent(sendEvent: self.sendEventToJS)
    }
    
    func sendEventToJS(event: PushEvent) {
        sendEvent(withName: event.type.name, body: event.payload)
    }
    
    public override func addListener(_ eventName: String!) {
        super.addListener(eventName)
        registeredEventNames.insert(eventName)
        
        if (registeredEventNames == expectedEventNames) {
            notifyFlushQueuedEvents()
        }
    }
    
    override func supportedEvents() -> [String]! {
        return Array(expectedEventNames)
    }
    
    @objc
    func requestPermissions(
        _ permissions: [AnyHashable: Any],
        resolve: @escaping RCTPromiseResolveBlock,
        reject: @escaping RCTPromiseRejectBlock
    ) {
        sharedNotificationManager.requestPermissions(permissions, resolve: resolve, reject: reject)
    }
    
    @objc
    func getPermissionStatus(
        _ resolve: @escaping RCTPromiseResolveBlock,
        reject: @escaping RCTPromiseRejectBlock
    ) {
        sharedNotificationManager.getPermissionStatus(resolve, reject: reject)
    }
    
    @objc
    func getLaunchNotification(
        _ resolve: RCTPromiseResolveBlock,
        reject: RCTPromiseRejectBlock
    ) {
        sharedNotificationManager.getLaunchNotification(resolve, reject: reject)
    }
    
    @objc
    func setBadgeCount(_ count: Int) {
        sharedNotificationManager.setBadgeCount(count)
    }
    
    @objc
    func getBadgeCount(
        _ resolve: @escaping RCTPromiseResolveBlock,
        reject: RCTPromiseRejectBlock
    ) {
        sharedNotificationManager.getBadgeCount(resolve, reject: reject)
    }
    
    @objc
    func completeNotification(_ completionHandlerId: String) {
        sharedNotificationManager.completeNotification(completionHandlerId)
    }
    
    @objc
    func registerForToken() {
        DispatchQueue.main.async {
            UIApplication.shared.registerForRemoteNotifications()
        }
    }
    
    
    @objc
    override static func requiresMainQueueSetup() -> Bool {
        return true
    }
    
    @objc
    override func constantsToExport() -> [AnyHashable : Any]! {
        return [
            "NativeEvent": [
                NativeEvent.backgroundMessageReceived.key: NativeEvent.backgroundMessageReceived.name,
                NativeEvent.foregroundMessageReceived.key: NativeEvent.foregroundMessageReceived.name,
                NativeEvent.notificationOpened.key: NativeEvent.notificationOpened.name,
                NativeEvent.launchNotificationOpened.key: NativeEvent.launchNotificationOpened.name,
                NativeEvent.tokenReceived.key: NativeEvent.tokenReceived.name,
                NativeEvent.failedToRegister.key: NativeEvent.failedToRegister.name,
            ],
        ]
    }
    
}
