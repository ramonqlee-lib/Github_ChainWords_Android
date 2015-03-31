//
//  AppDelegate.m
//  NewsTongue
//
//  Created by ramonqlee on 1/23/15.
//  Copyright (c) 2015 iDreems. All rights reserved.
//

#import "AppDelegate.h"
#import "MobClick.h"
#import "Constants.h"
#import "UMFeedback.h"
#import "UMSocial.h"
#import "UMessage.h"

#import "CollectionViewController.h"
#import "SliderViewController.h"
#import "MainAppViewController.h"
#import "LeftViewController.h"
#import "RightViewController.h"

#define UMSYSTEM_VERSION_GREATER_THAN_OR_EQUAL_TO(v)  ([[[UIDevice currentDevice] systemVersion] compare:v options:NSNumericSearch] != NSOrderedAscending)

#define _IPHONE80_ 80000

@interface AppDelegate ()

@end

@implementation AppDelegate

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    // Override point for customization after application launch.
    [self initUmengAnalytics];
    [self initUmengFeedback];
    [self initPush:application didFinishLaunchingWithOptions:launchOptions];
    
    self.window = [[UIWindow alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
    // Override point for customization after application launch.
    self.window.backgroundColor = [UIColor whiteColor];
    
    QHMainGestureRecognizerViewController *mainViewController = [[QHMainGestureRecognizerViewController alloc] init];
    self.window.rootViewController = mainViewController;
    mainViewController.moveType = moveTypeMove;
    
    [SliderViewController sharedSliderController].LeftVC=[[LeftViewController alloc] init];
    [SliderViewController sharedSliderController].RightVC= [[RightViewController alloc] init];
    [SliderViewController sharedSliderController].MainVC = [[CollectionViewController alloc]init];//[[MainAppViewController alloc] init];
    [SliderViewController sharedSliderController].RightSContentOffset=260;
    [SliderViewController sharedSliderController].RightSContentScale=0.68;
    [SliderViewController sharedSliderController].RightSJudgeOffset=160;
    [mainViewController addViewController2Main:[SliderViewController sharedSliderController]];
    
    return YES;
}
- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken
{
    NSLog(@"%@",[deviceToken description]);
    [UMessage registerDeviceToken:deviceToken];
}

// 当 DeviceToken 获取失败时,系统会回调此⽅方法
- (void)application:(UIApplication *)application didFailToRegisterForRemoteNotificationsWithError:( NSError *)error
{
    NSLog(@"DeviceToken 获取失败,原因:%@",error);
}

- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo {
    [UMessage didReceiveRemoteNotification:userInfo];
}

- (void)applicationWillResignActive:(UIApplication *)application {
    // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
}

- (void)applicationDidEnterBackground:(UIApplication *)application {
    // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later.
    // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
}

- (void)applicationWillEnterForeground:(UIApplication *)application {
    // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
    NSLog(@"applicationWillEnterForeground");
}

- (void)applicationDidBecomeActive:(UIApplication *)application {
    // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
    [application setApplicationIconBadgeNumber:0];
}

- (void)applicationWillTerminate:(UIApplication *)application {
    // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
}

#pragma mark umeng analytics

-(void)initUmengAnalytics
{
    [MobClick startWithAppkey:UMENG_APP_KEY reportPolicy:BATCH channelId:@""];
    NSString *version = [[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleShortVersionString"];
    [MobClick setAppVersion:version];
    [MobClick setEncryptEnabled:YES];
}
-(void) initUmengFeedback
{
    [UMFeedback setAppkey:UMENG_APP_KEY];
}
-(void) initumengSocial
{
    [UMSocialData setAppKey:UMENG_APP_KEY];
}

-(void) initPush:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    //set AppKey and LaunchOptions
    [UMessage startWithAppkey:@"54d70fd8fd98c535da000661" launchOptions:launchOptions];
    
#if __IPHONE_OS_VERSION_MAX_ALLOWED >= _IPHONE80_
    if(UMSYSTEM_VERSION_GREATER_THAN_OR_EQUAL_TO(@"8.0"))
    {
        //register remoteNotification types
        UIMutableUserNotificationAction *action1 = [[UIMutableUserNotificationAction alloc] init];
        action1.identifier = @"action1_identifier";
        action1.title=@"Accept";
        action1.activationMode = UIUserNotificationActivationModeForeground;//当点击的时候启动程序
        
        UIMutableUserNotificationAction *action2 = [[UIMutableUserNotificationAction alloc] init];  //第二按钮
        action2.identifier = @"action2_identifier";
        action2.title=@"Reject";
        action2.activationMode = UIUserNotificationActivationModeBackground;//当点击的时候不启动程序，在后台处理
        action2.authenticationRequired = YES;//需要解锁才能处理，如果action.activationMode = UIUserNotificationActivationModeForeground;则这个属性被忽略；
        action2.destructive = YES;
        
        UIMutableUserNotificationCategory *categorys = [[UIMutableUserNotificationCategory alloc] init];
        categorys.identifier = @"category1";//这组动作的唯一标示
        [categorys setActions:@[action1,action2] forContext:(UIUserNotificationActionContextDefault)];
        
        UIUserNotificationSettings *userSettings = [UIUserNotificationSettings settingsForTypes:UIUserNotificationTypeBadge|UIUserNotificationTypeSound|UIUserNotificationTypeAlert
                                                                                     categories:[NSSet setWithObject:categorys]];
        [UMessage registerRemoteNotificationAndUserNotificationSettings:userSettings];
        
    } else{
        //register remoteNotification types
        [UMessage registerForRemoteNotificationTypes:UIRemoteNotificationTypeBadge
         |UIRemoteNotificationTypeSound
         |UIRemoteNotificationTypeAlert];
    }
#else
    
    //register remoteNotification types
    [UMessage registerForRemoteNotificationTypes:UIRemoteNotificationTypeBadge
     |UIRemoteNotificationTypeSound
     |UIRemoteNotificationTypeAlert];
    
#endif
    
    //for log
    [UMessage setLogEnabled:NO];
}
@end
