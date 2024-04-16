import Foundation
import UserNotifications

#if canImport(WatchKit)
import WatchKit
#elseif canImport(UIKit)
import UIKit
typealias Application = UIApplication
#elseif canImport(AppKit)
import AppKit
typealias Application = NSApplication
#endif

@available(iOSApplicationExtension, unavailable)
@available(watchOSApplicationExtension, unavailable)
@available(tvOSApplicationExtension, unavailable)
@available(macCatalystApplicationExtension, unavailable)
@available(OSXApplicationExtension, unavailable)
/// Provides convenience methods for requesting and checking notifications permissions.
public class AUNotificationPermissions {
    
    /// Check if notifications are allowed
    public static var allowed: Bool {
        get async {
            await status == .authorized ? true : false
        }
    }
    
    /// Check the notification permission status
    public static var status: UNAuthorizationStatus {
        get async {
            await withCheckedContinuation { continuation in
                UNUserNotificationCenter.current().getNotificationSettings { settings in
                    continuation.resume(returning: settings.authorizationStatus)
                }
            }
        }
    }
    
    /// Request notification permissions
    /// - Parameter options: Requested notification options
    @discardableResult
    public static func request(_ options: UNAuthorizationOptions? = nil) async throws -> Bool {
        let options = options ?? [.badge, .alert, .sound]
        let notificationsAllowed = try await UNUserNotificationCenter.current().requestAuthorization(
            options: options
        )
        
        return notificationsAllowed
    }
    
    /// Register device with APNs
    public static func registerForRemoteNotifications() async {
        await MainActor.run {
            #if canImport(WatchKit)
            WKExtension.shared().registerForRemoteNotifications()
            #else
            Application.shared.registerForRemoteNotifications()
            #endif
        }
    }
}
