//
//  DbCache.m
//  NewsTongue
//
//  Created by ramonqlee on 3/17/15.
//  Copyright (c) 2015 iDreems. All rights reserved.
//

#import "DbCache.h"
#import "SQLiteManager.h"

@interface DbCache()
{
    
}

@end

@implementation DbCache
@synthesize dbName,tableName;

-(void)createDbIfNotExist:(NSString*)sql
{
    SQLiteManager* dbManager = [[SQLiteManager alloc]initWithDatabaseNamed:self.dbName];
    if (![dbManager openDatabase]) {
        //create table
        NSString *sqlCreateTable = sql;
        [dbManager doQuery:sqlCreateTable];
        [dbManager closeDatabase];
    }
}

-(void)save:(NSString*)sql
{
    SQLiteManager* dbManager = [[SQLiteManager alloc] initWithDatabaseNamed:self.dbName];

    NSError * error = [dbManager doQuery:sql];
    NSLog(@"error:%@",error);
}

-(void)remove:(NSString*)sql
{
    SQLiteManager* dbManager = [[SQLiteManager alloc] initWithDatabaseNamed:self.dbName];
    [dbManager doQuery:sql];
}

-(NSArray*)get:(NSString*)sql
{
    SQLiteManager* dbManager = [[SQLiteManager alloc] initWithDatabaseNamed:self.dbName];
    NSLog(@"query:%@",sql);

    return [dbManager getRowsForQuery:sql];
}

@end
