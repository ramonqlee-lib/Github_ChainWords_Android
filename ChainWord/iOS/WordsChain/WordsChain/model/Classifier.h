//
//  Classifier.h
//  WordsChain
//
//  Created by ramonqlee on 6/26/14.
//  Copyright (c) 2014 zzy. All rights reserved.
//

#import <Foundation/Foundation.h>

@protocol Classifier <NSObject>

@required
-(NSDictionary*)run:(NSArray*)data;

@end
