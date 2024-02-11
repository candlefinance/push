//
//  AppDelegate.swift
//  PushExample
//
//  Created by Gary Tokman on 2/11/24.
//

import Foundation
import UIKit
import React
import candlefinance_push // 0
import NotificationCenter // 1

@UIApplicationMain
class AppDelegate: RCTAppDelegate {
  
  // 2
  let push = Push()
  
  var isDarkMode: Bool {
    return UITraitCollection.current.userInterfaceStyle == .dark
  }
  
  override func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
    self.moduleName = "PushExample"
    UNUserNotificationCenter.current().delegate = self // 3
    let result = super.application(application, didFinishLaunchingWithOptions: launchOptions)
   
    return result
  }
  
  override func sourceURL(for bridge: RCTBridge!) -> URL! {
//#if DEBUG
    print("DEBUG")
    return RCTBundleURLProvider.sharedSettings().jsBundleURL(forBundleRoot: "index")
//#else
//    print("PROD")
//    return Bundle.main.url(forResource: "main", withExtension: "jsbundle")
//#endif
  }
  
}

extension AppDelegate: UNUserNotificationCenterDelegate {
  
  override func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
    push.application(application, didRegisterForRemoteNotificationsWithDeviceToken: deviceToken)
  }
  
  override func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
    push.application(application, didFailToRegisterForRemoteNotificationsWithError: error)
  }
  
  public func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification, withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
    push.userNotificationCenter(center, willPresent: notification, withCompletionHandler: completionHandler)
  }
  
  public func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse) async {
    await push.userNotificationCenter(center, didReceive: response)
  }
  
  override func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable: Any], fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {
    push.application(application, didReceiveRemoteNotification: userInfo, fetchCompletionHandler: completionHandler)
  }

}
