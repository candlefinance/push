#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface RCT_EXTERN_MODULE(Push, RCTEventEmitter)

RCT_EXTERN_METHOD(supportedEvents)

RCT_EXTERN_METHOD(requestPermissions: (RCTPromiseResolveBlock)resolve rejecter: (RCTPromiseRejectBlock)reject)
RCT_EXTERN_METHOD(onFinish:(NSString *)uuid withResolver:(RCTPromiseResolveBlock)resolve withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXPORT_SYNCHRONOUS_TYPED_METHOD(BOOL, isRegisteredForRemoteNotifications) {
    return [[Push class] isRegisteredForRemoteNotifications];
}

RCT_EXPORT_SYNCHRONOUS_TYPED_METHOD(id, registerForToken) {
    [[Push class] registerForToken];
    return @(YES);
}

+ (BOOL)requiresMainQueueSetup
{
  return NO;
}

@end
