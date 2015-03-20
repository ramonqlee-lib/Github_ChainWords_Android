//
//  WordManager.h
//  NewsTongue
//  管理单词的翻译，有缓存能力
//  Created by ramonqlee on 1/31/15.
//  Copyright (c) 2015 iDreems. All rights reserved.
//

#import <Foundation/Foundation.h>

@protocol WordQueryResponse

-(void)handleWordResponse:(NSDictionary*)result;// 异步回调

@end

@interface WordManager : NSObject

+(id)sharedInstance;

-(BOOL)query:(NSString*)word from:(NSString*)fromLang to:(NSString*)toLang response:(id<WordQueryResponse>)result;

//utils
+(NSString*)getReadableMeaning:(NSDictionary*)value;// 获取单词的意义
+(NSArray*)getProns:(NSDictionary*)value;// 获取读音url
+(NSArray*)getExamples:(NSDictionary*)value;// 获取使用举例

@end
