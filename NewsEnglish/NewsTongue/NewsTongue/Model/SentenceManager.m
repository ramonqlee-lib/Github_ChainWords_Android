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
#import "Base64.h"

/**
 翻译api
 --------------------------------
 
 调用方式：
 
 http://checknewversion.duapp.com/trans.php?from=xx&to=xx&w=xx
 参数：
 w: 待查单词
 from: 当前单词语言
 to:目标语言
 
 --------------------------------
 
 返回结果:
 --------------------------------
 
 {
 "@attributes": {
 "num": "219",
 "id": "219",
 "name": "219"
 },
 "key": "help",
 "ps": [
 "help",
 "hɛlp"
 ],
 "pron": [
 "http://res.iciba.com/resource/amp3/oxford/0/4d/53/4d53fe900cb10b4d0c73db20c148d2aa.mp3",
 "http://res.iciba.com/resource/amp3/1/0/65/7f/657f8b8da628ef83cf69101b6817150a.mp3"
 ],
 "pos": [
 "vt.& vi.",
 "vt.",
 "n.",
 "vi.",
 "int."
 ],
 "acceptation": [
 "帮助；有助于， 有利于；\n",
 "治疗；避免；招待（客人）；给…盛（饭、菜）；\n",
 "帮助；助手；补救办法；有用；\n",
 "（在餐桌旁）招待，侍应，作仆人（或店员、服务员等）；\n",
 "[呼救语]救命！；\n"
 ],
 "sent": [
 {
 "orig": "\nFinancial assistance is available for eligible students through the OS - HELP Scheme of Monash Abroad.\n",
 "trans": "\n符合有关条件的同学可以通过OS -HELP 计划向蒙纳士留学办公室申请经济援助.\n"
 },
 {
 "orig": "\nOut of memory. Type HELP MEMORY for your options.\n",
 "trans": "\n内存不足. 键入help内存,您的选择.\n"
 },
 {
 "orig": "\nPeople really need help but may attack youyou do help them. Help people anyway.\n",
 "trans": "\n别人急需帮忙,你帮了忙以后竟然被他们攻击. ——不管怎样,还是要助人.\n"
 },
 {
 "orig": "\nOnline help: Get help fast using the online help feature.\n",
 "trans": "\n在线帮助: 快速获得帮助使用联机帮助功能.\n"
 },
 {
 "orig": "\nPeople really need help but may attack you ig you do help them. Help people anyway.\n",
 "trans": "\n人闪需要帮助,可当你向他们伸出援助之手时,可能会反被其伤,但是不管怎样,还是要帮助他们.\n"
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
    //http://dict-co.iciba.com/api/dictionary.php?key=FF0C453A324EC924681AF23BCAF64521&type=json&w=help
    // 2.1 缓存到本地文件DB
    NSDictionary* queriedResult = [self getFromLocal:sent from:fromLang to:toLang];
    
    if (nil != queriedResult && nil != sentenceQueryCallback) {
        [sentenceQueryCallback handleResponse:queriedResult];
        sentenceQueryCallback = nil;
        NSLog(@"find cached word: %@",sent);
        return YES;
    }
    
    // 转移到服务器端，封装一个自己的词典api
    // 输入参数见如下，输出为一段文本即可
    NSString *string = [NSString stringWithFormat:@"http://checknewversion.duapp.com/trans.php?from=%@&to=%@&q=%@", fromLang,toLang,sent];
    NSURL *url = [NSURL URLWithString:string];
    NSURLRequest *request = [NSURLRequest requestWithURL:url];
    
    // 2
    AFHTTPRequestOperation *operation = [[AFHTTPRequestOperation alloc] initWithRequest:request];
    operation.responseSerializer = [AFJSONResponseSerializer serializer];
    
    [operation setCompletionBlockWithSuccess:^(AFHTTPRequestOperation *operation, id responseObject) {
        
        NSDictionary* dict = (NSDictionary *)responseObject;
        //        NSLog(@"%@",dict);
        // 保存到本地
        [self setAsLocal:sent result:dict from:fromLang to:toLang];
        
        if (nil != sentenceQueryCallback) {
            // 在主线程中更新
            [sentenceQueryCallback handleResponse:dict];
            sentenceQueryCallback = nil;
        }
    } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
        sentenceQueryCallback = nil;
        // 4
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
-(NSDictionary*)getFromLocal:(NSString*)word from:(NSString*)fromLang to:(NSString*)toLang
{
    // 获取，并作base64解码
    SQLiteManager* dbManager = [[SQLiteManager alloc] initWithDatabaseNamed:SENTENCES_DB_NAME];
    NSString* query = [NSString stringWithFormat:@"SELECT * FROM %@ where %@ = '%@' AND %@ = '%@' AND %@ = '%@'",SENTENCES_TABLE_NAME,SENTENCE_MD5_KEY,word,FROM_LANG_KEY,fromLang,TO_LANG_KEY,toLang];
    
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
-(void)setAsLocal:(NSString*)word result:(NSDictionary*)dict from:(NSString*)fromLang to:(NSString*)toLang
{
    // 首先做base64处理
    [SentenceManager makeSureDBExist];
    [SentenceManager removeRecord:word];
    SQLiteManager* dbManager = [[SQLiteManager alloc] initWithDatabaseNamed:SENTENCES_DB_NAME];
    
    // TODO json to string
    NSData* data = [SentenceManager toJSONData:dict];
    NSString* json = [[NSString alloc] initWithData:data  encoding:NSUTF8StringEncoding];
    json = [json base64EncodedString];
    //    NSLog(@"toJson :%d",json.length);
    NSString *sql = [NSString stringWithFormat:
                     @"INSERT INTO '%@' ('%@', '%@', '%@', '%@') VALUES ('%@', '%@', '%@', '%@')",
                     SENTENCES_TABLE_NAME, SENTENCE_MD5_KEY, FROM_LANG_KEY, TO_LANG_KEY,DATA_KEY,word,fromLang,toLang,json];
    NSError * error = [dbManager doQuery:sql];
    
    NSLog(@"save as local for %@ and error:%@",word,error);
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
@end
