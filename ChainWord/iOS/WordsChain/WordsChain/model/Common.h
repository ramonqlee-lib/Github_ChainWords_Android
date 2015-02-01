//
//  Common.h
//  WordsChain
//
//  Created by ramonqlee on 6/22/14.
//  Copyright (c) 2014 zzy. All rights reserved.
//

#import <Foundation/Foundation.h>

#define Decl_Singleton(className) +(className*)sharedInstance;

#define Impl_Singleton(className) static className* s##className;\
+(className*)sharedInstance\
{\
if(!s##className)\
{\
s##className = [[className alloc]init];\
}\
return s##className;\
}

@interface Common : NSObject

@end
