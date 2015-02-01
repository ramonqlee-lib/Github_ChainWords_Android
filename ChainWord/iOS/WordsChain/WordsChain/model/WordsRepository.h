//
//  WordsRepository.h
//  气泡
//
//  Created by ramonqlee on 6/22/14.
//  Copyright (c) 2014 zzy. All rights reserved.
//  负责单词的管理
//  1.全部单词的加载
//  2.给定一个单词，获得下一个接龙单词（必须是随机单词，并且已经采用的单词，不能重复出现）
//

#import <Foundation/Foundation.h>
#import "Common.h"
#import "Classifier.h"

@interface WordsRepository : NSObject
Decl_Singleton(WordsRepository)

// apply a classifier before retrieve words
-(void)applyClassifier:(id<Classifier>)classifier;

-(NSString*)next:(NSString*)current duplicate:(BOOL)same;

@end
