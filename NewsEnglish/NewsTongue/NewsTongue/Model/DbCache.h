//
//  DbCache.h
//  NewsTongue
//
//  Created by ramonqlee on 3/17/15.
//  Copyright (c) 2015 iDreems. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface DbCache : NSObject

@property(nonatomic,copy)NSString* dbName;
@property(nonatomic,copy)NSString* tableName;

-(void)createDbIfNotExist:(NSString*)sql;

-(void)save:(NSString*)sql;

-(void)remove:(NSString*)sql;

-(NSArray*)get:(NSString*)sql;
@end
