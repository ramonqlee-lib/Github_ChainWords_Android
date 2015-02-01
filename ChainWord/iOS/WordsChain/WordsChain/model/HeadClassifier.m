//
//  HeadClassifier.m
//  WordsChain
//
//  Created by ramonqlee on 6/26/14.
//  Copyright (c) 2014 zzy. All rights reserved.
//

#import "HeadClassifier.h"
 
@implementation HeadClassifier

-(NSDictionary*)run:(NSArray*)data
{
    NSMutableDictionary* dict = [NSMutableDictionary dictionaryWithCapacity:26];
    
    // get word one by one and add to list
    // 1. init if not set yet for dict
    for (NSString* word in data) {
        if (!word || !word.length) {
            continue;
        }
        
        NSString* headLetter = [[word substringToIndex:1]uppercaseString];
        //A-Z?
        NSString *regex = @"[A-Z]";
        NSPredicate *predicate = [NSPredicate predicateWithFormat:@"SELF MATCHES %@", regex];
        if ([predicate evaluateWithObject:headLetter] == NO) {
            continue;
        }
        
        id array = [dict objectForKey:headLetter];
        
        //map this letter?
        if (!array) {
            array = [NSMutableArray arrayWithCapacity:1];
            [dict setObject:array forKey:headLetter];
        }
        
        //add to this chain,duplicate allowed right now
        [array addObject:word];
    }
    return dict;
}

@end
