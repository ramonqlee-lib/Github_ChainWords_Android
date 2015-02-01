//
//  WordsRepository.m
//  气泡
//
//  Created by ramonqlee on 6/22/14.
//  Copyright (c) 2014 zzy. All rights reserved.
//

#import "WordsRepository.h"

//consts
static NSString* kWordsFileName = @"英语四级单词完整版";
static NSString* kWordsFileSuffix = @"txt";


@interface WordsRepository()
{
    NSDictionary* classifiedWords;//alphabeta as key, words array as value
    NSMutableArray* originalWords;// word list from file
}
@end

@implementation WordsRepository

Impl_Singleton(WordsRepository)

-(id)init
{
    self = [super init];
    if (self) {
        [self loadRep:kWordsFileName];
    }
    return self;
}

-(NSString*)next:(NSString*)current duplicate:(BOOL)same
{
    if (!current || !current.length || !classifiedWords || !classifiedWords.count) {
        return @"";
    }
    
    //:: get next word from NSDictionary
    [self loadRep:kWordsFileName];
    
    //1. position with alphabet
    //:: get last or whatever word
    NSString* key = [[current substringFromIndex:current.length-1]uppercaseString];
    
    //2. get one word randomly
    NSArray* wordsArray = [classifiedWords objectForKey:key];
    if (!wordsArray || !wordsArray.count) {
        return @"";
    }
    
    NSInteger pos = arc4random()%wordsArray.count;
    NSString* ret = [wordsArray objectAtIndex:pos];
    
    // remove this word if duplicate return is not allowed
    if (!same) {
        //remove this one
        if ([wordsArray isKindOfClass:[NSMutableArray class]]) {
            [((NSMutableArray*)wordsArray) removeObjectAtIndex:pos];
        }
    }
    
    return ret;
}

//: load words into memory
-(void)loadRep:(NSString*)fileName
{
    if (originalWords) {
        return;
    }
    
    //TODO:: 将文本文件读入内存
    NSError *error;
    NSString *textFileContents = [NSString
                                  stringWithContentsOfFile:[[NSBundle mainBundle] pathForResource:kWordsFileName ofType:kWordsFileSuffix]
                                  encoding:NSUTF8StringEncoding
                                  error: & error];
    
    // If there are no results, something went wrong
    if (textFileContents == nil) {
        // an error occurred
        NSLog(@"Error reading text file. %@", [error localizedFailureReason]);
    }
    NSArray* temp = [textFileContents componentsSeparatedByString:@"\n"];
    NSLog(@"Number of lines in the file:%d", [temp count] );
    
    // remove space of words
    originalWords = [NSMutableArray arrayWithCapacity:temp.count];
    for (NSString* word in temp) {
        NSString* newWord = [word stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
        if (!newWord.length) {
            continue;
        }
        [originalWords addObject:newWord];
    }
     NSLog(@"Number of lines in the file:%d", [temp count] );
}

//:: classify words
-(void)applyClassifier:(id<Classifier>)classifier
{
    if (!originalWords || !originalWords.count || classifiedWords || !classifier) {
        return;
    }
    
    classifiedWords = [classifier run:originalWords];
}


@end
