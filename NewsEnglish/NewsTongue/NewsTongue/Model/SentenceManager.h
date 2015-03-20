//
//  SentenceManager.h
//  NewsTongue
//
//  Created by ramonqlee on 2/2/15.
//  Copyright (c) 2015 iDreems. All rights reserved.
//

#import <Foundation/Foundation.h>

@protocol SentenceQueryResult

-(void)handleSentenceResponse:(NSDictionary*)result;// 异步回调

@end

@interface SentenceManager : NSObject

+(id)sharedInstance;

-(BOOL)query:(NSString*)sent from:(NSString*)fromLang to:(NSString*)toLang response:(id<SentenceQueryResult>)result;

+(NSString*)getTranslate:(NSDictionary*)val;
@end
