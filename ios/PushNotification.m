//
//  PushNotification.m
//  candlefinance-push
//
//  Created by Gary Tokman on 4/16/24.
//

#import "PushNotification.h"
#import "candlefinance_push-Swift.h"

@implementation PushNotification

+ (void) didRegisterForRemoteNotificationsWithDeviceToken:(NSData*)deviceToken {
    [PushNotificationAppDelegateHelper didRegisterForRemoteNotificationsWithDeviceToken:deviceToken];
}

+ (void) didFailToRegisterForRemoteNotificationsWithError:(NSError*)error {
    [PushNotificationAppDelegateHelper didFailToRegisterForRemoteNotificationsWithError:error];
}

+ (void) didReceiveRemoteNotification:(NSDictionary*)userInfo withCompletionHandler:(void (^)(UIBackgroundFetchResult))completionHandler {
    [PushNotificationAppDelegateHelper didReceiveRemoteNotificationWithUserInfo:userInfo completionHandler:completionHandler];
}

@end
