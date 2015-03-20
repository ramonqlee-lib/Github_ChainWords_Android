//
//  SentenceManager.m
//  NewsTongue
//
//  Created by ramonqlee on 2/2/15.
//  Copyright (c) 2015 iDreems. All rights reserved.
//

#import "SentenceManager.h"
#import "AFNetworking.h"
#import "SQLiteManager.h"
#import "Base64Simple.h"
#import "NSString+HTML.h"

/**
 翻译api
 --------------------------------
 
 调用方式：
 
 http://checknewversion.duapp.com/trans.php?from=xx&to=xx&q=xx
 参数：
 q: 待翻译句子(urlencoded)
 from: 当前单词语言
 to:目标语言
 
 --------------------------------
 
 返回结果:
 --------------------------------
 {
 "from": "en",
 "to": "zh",
 "trans_result": [
 {
 "src": "how do you do",
 "dst": "你好吗"
 }
 ]
 }
 */


static NSString* SENTENCES_DB_NAME = @"sentenceRepo.sqlite";
const static NSString* SENTENCES_TABLE_NAME = @"sentenceTable";
const static NSString* SENTENCE_MD5_KEY = @"md5";
const static NSString* FROM_LANG_KEY = @"fromLang";
const static NSString* TO_LANG_KEY = @"toLang";
static NSString* DATA_KEY = @"data";

static SentenceManager* sSentenceManager;

@interface SentenceManager()
{
    id<SentenceQueryResult> sentenceQueryCallback;
}
@end

@implementation SentenceManager

+(id)sharedInstance
{
    if(nil != sSentenceManager)
    {
        return sSentenceManager;
    }
    sSentenceManager = [[SentenceManager alloc]init];
    return sSentenceManager;
}

-(BOOL)query:(NSString*)sent from:(NSString*)fromLang to:(NSString*)toLang response:(id<SentenceQueryResult>)callback
{
    // querying,just return
    if (nil != sentenceQueryCallback) {
        return NO;
    }
    
    sentenceQueryCallback = callback;
    //  翻译单词
    // 1. 首先尝试从本地DB缓存中提取
    // 2. 然后尝试从网络获取
    // 2.1 缓存到本地文件DB
    NSDictionary* queriedResult = [self getFromLocal:[sent md5] from:fromLang to:toLang];
    
    if (nil != queriedResult && nil != sentenceQueryCallback) {
        [sentenceQueryCallback handleSentenceResponse:queriedResult];
        sentenceQueryCallback = nil;
        NSLog(@"find cached result: %@",sent);
        return YES;
    }
    
    NSString *string = [NSString stringWithFormat:@"http://checknewversion.duapp.com/trans.php?from=%@&to=%@&q=%@", fromLang,toLang,[sent stringByURLEncodingStringParameter]];
    NSURL *url = [NSURL URLWithString:string];
    NSURLRequest *request = [NSURLRequest requestWithURL:url];
    
    // 2
    AFHTTPRequestOperation *operation = [[AFHTTPRequestOperation alloc] initWithRequest:request];
    operation.responseSerializer = [AFJSONResponseSerializer serializer];
    
    [operation setCompletionBlockWithSuccess:^(AFHTTPRequestOperation *operation, id responseObject) {
        
        NSDictionary* dict = (NSDictionary *)responseObject;
        //        NSLog(@"%@",dict);
        // 保存到本地
        [self setAsLocal:[sent md5] result:dict from:fromLang to:toLang];
        
        if (nil != sentenceQueryCallback) {
            // 在主线程中更新
            [sentenceQueryCallback handleSentenceResponse:dict];
            sentenceQueryCallback = nil;
        }
    } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
        
        if (nil != sentenceQueryCallback) {
            // 在主线程中更新
            [sentenceQueryCallback handleSentenceResponse:nil];
            sentenceQueryCallback = nil;
        }
        
        UIAlertView *alertView = [[UIAlertView alloc] initWithTitle:@"Error Retrieving Word"
                                                            message:[error localizedDescription]
                                                           delegate:nil
                                                  cancelButtonTitle:@"Ok"
                                                  otherButtonTitles:nil];
        [alertView show];
    }];
    
    // 5
    [operation start];
    
    return YES;
}


#pragma mark 本地缓存接口

//  返回本地缓存的数据
-(NSDictionary*)getFromLocal:(NSString*)sentenceMD5 from:(NSString*)fromLang to:(NSString*)toLang
{
    // 获取，并作base64解码
    SQLiteManager* dbManager = [[SQLiteManager alloc] initWithDatabaseNamed:SENTENCES_DB_NAME];
    NSString* query = [NSString stringWithFormat:@"SELECT * FROM %@ where %@ = '%@' AND %@ = '%@' AND %@ = '%@'",SENTENCES_TABLE_NAME,SENTENCE_MD5_KEY,sentenceMD5,FROM_LANG_KEY,fromLang,TO_LANG_KEY,toLang];
    
    NSLog(@"query:%@",query);
    
    NSArray* items =  [dbManager getRowsForQuery:query];
    if (nil == items || 0 == items.count) {
        return nil;
    }
    NSString* obj = [[items objectAtIndex:0]valueForKey:DATA_KEY];
    // TODO string to json
    obj = [obj base64DecodedString];
    return [SentenceManager jsonWithData:obj];
}


//  保存到本地
-(void)setAsLocal:(NSString*)sentenceMD5 result:(NSDictionary*)dict from:(NSString*)fromLang to:(NSString*)toLang
{
    // 首先做base64处理
    [SentenceManager makeSureDBExist];
    [SentenceManager removeRecord:sentenceMD5];
    SQLiteManager* dbManager = [[SQLiteManager alloc] initWithDatabaseNamed:SENTENCES_DB_NAME];
    
    // TODO json to string
    NSData* data = [SentenceManager toJSONData:dict];
    NSString* json = [[NSString alloc] initWithData:data  encoding:NSUTF8StringEncoding];
    json = [json base64EncodedString];
    //    NSLog(@"toJson :%d",json.length);
    NSString *sql = [NSString stringWithFormat:
                     @"INSERT INTO '%@' ('%@', '%@', '%@', '%@') VALUES ('%@', '%@', '%@', '%@')",
                     SENTENCES_TABLE_NAME, SENTENCE_MD5_KEY, FROM_LANG_KEY, TO_LANG_KEY,DATA_KEY,sentenceMD5,fromLang,toLang,json];
    NSError * error = [dbManager doQuery:sql];
    
    NSLog(@"save as local for %@ and error:%@",sentenceMD5,error);
}
+(void)removeRecord:(NSString*)key
{
    SQLiteManager* dbManager = [[SQLiteManager alloc] initWithDatabaseNamed:SENTENCES_DB_NAME];
    NSString *sql = [NSString stringWithFormat:@"delete from %@ where %@ = '%@'",SENTENCES_TABLE_NAME,SENTENCE_MD5_KEY,key];
    [dbManager doQuery:sql];
}

// db utils
+(void)makeSureDBExist
{
    SQLiteManager* dbManager = [[SQLiteManager alloc]initWithDatabaseNamed:SENTENCES_DB_NAME];
    if (![dbManager openDatabase]) {
        //create table
        
        NSString *sqlCreateTable = [NSString stringWithFormat:@"CREATE TABLE IF NOT EXISTS %@ (%@ TEXT, %@ TEXT, %@ TEXT, %@ TEXT)",SENTENCES_TABLE_NAME, SENTENCE_MD5_KEY,FROM_LANG_KEY, TO_LANG_KEY,DATA_KEY];
        [dbManager doQuery:sqlCreateTable];
        
        [dbManager closeDatabase];
    }
}

#pragma mark json utils
// 将字典或者数组转化为JSON串
+ (NSData *)toJSONData:(id)theData{
    
    NSError *error = nil;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:theData
                                                       options:NSJSONWritingPrettyPrinted
                                                         error:&error];
    
    if ([jsonData length] > 0 && error == nil){
        return jsonData;
    }else{
        return nil;
    }
}

+(NSDictionary*)jsonWithData:(NSString*)data
{
    NSDictionary* responseObject = nil;
    if (data && ![data isEqualToString:@" "]) {
        // Workaround for a bug in NSJSONSerialization when Unicode character escape codes are used instead of the actual character
        // See http://stackoverflow.com/a/12843465/157142
        
        if (data) {
            if ([data length] > 0) {
                responseObject = [NSJSONSerialization JSONObjectWithData:[data dataUsingEncoding:NSUTF8StringEncoding] options:NSJSONReadingMutableContainers error:nil];
            }
        }
    }
    return responseObject;
}

#pragma mark utils
+(NSString*)getTranslate:(NSDictionary*)val
{
    if (!val) {
        return @"";
    }
    id dict = [val objectForKey:@"trans_result"];
    if (!dict || ![dict isKindOfClass:[NSArray class]]) {
        return @"";
    }
    NSArray* arr = (NSArray*)dict;
    if (!arr.count) {
        return @"";
    }
    
    NSDictionary* result = (NSDictionary*)[arr objectAtIndex:0];
    return (NSString*)[result objectForKey:@"dst"];
}
@end
