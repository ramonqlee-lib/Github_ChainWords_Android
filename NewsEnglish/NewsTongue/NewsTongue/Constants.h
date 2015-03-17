//
//  Constants.h
//  NewsTongue
//
//  Created by ramonqlee on 2/8/15.
//  Copyright (c) 2015 iDreems. All rights reserved.
//

#import <Foundation/Foundation.h>

//#define NSLog(...)
#define SUPPORT_IOS8 1
#define BAIDU_AD_APPID @"f84efff4"

#define UMENG_APP_KEY @"54d70fd8fd98c535da000661"

// umeng event
#define CHANGE_FONT_SIZE_AT_READ_MODE_PAGE @"CHANGE_FONT_SIZE_AT_READ_MODE_PAGE"


#define SCREEN_WIDTH ([UIScreen mainScreen].bounds.size.width)
#define SCREEN_HEIGHT ([UIScreen mainScreen].bounds.size.height)

#define kAdViewPortraitRect CGRectMake( (SCREEN_WIDTH-kBaiduAdViewSizeDefaultWidth)/2, 0, kBaiduAdViewSizeDefaultWidth, kBaiduAdViewSizeDefaultHeight)

#define SYSTEM_VERSION_LESS_THAN(v) ([[[UIDevice currentDevice] systemVersion] compare:v options:NSNumericSearch] == NSOrderedAscending)//用来获取手机的系统，判断系统是多少

#define kNewsCacheDbName @"newscache.sqlite"
#define kNewsCacheTableName @"news"
#define kTitle @"title"
#define kSummary @"summary"
#define kContent @"content"
#define kThumbnail @"thumbnail"
#define kUpdated @"updated"
#define kCategory @"category"