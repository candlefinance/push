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

// 4
extension AppDelegate: UNUserNotificationCenterDelegate {
  
  override func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
    PushNotificationAppDelegateHelper.didRegisterForRemoteNotificationsWithDeviceToken(deviceToken)
  }
  
  override func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: any Error) {
    PushNotificationAppDelegateHelper.didFailToRegisterForRemoteNotificationsWithError(error)
  }
  
  override func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable: Any], fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {
    PushNotificationAppDelegateHelper.didReceiveRemoteNotification(userInfo: userInfo, completionHandler: completionHandler)
  }

}
