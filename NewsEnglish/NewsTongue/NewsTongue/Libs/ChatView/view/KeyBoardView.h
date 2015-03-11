//
//  KeyBordVIew.h
//  气泡
//
//  Created by zzy on 14-5-13.
//  Copyright (c) 2014年 zzy. All rights reserved.
//

#import <UIKit/UIKit.h>

@class KeyBoardView;

@protocol KeyBoardViewDelegate <NSObject>

-(void)KeyBoardView:(KeyBoardView *)keyBoardView textFieldReturn:(UITextField *)textField;
-(void)KeyBordView:(KeyBoardView *)keyBoardView textFleldBegin:(UITextField *)textField;
-(void)beginRecord;
-(void)finishRecord;
@end

@interface KeyBoardView : UIView
@property (nonatomic,assign) id<KeyBoardViewDelegate>delegate;
@end
