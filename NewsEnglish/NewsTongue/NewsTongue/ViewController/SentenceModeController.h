//
//  ReadModeController.h
//  NewsTongue
//
//  Created by ramonqlee on 1/23/15.
//  Copyright (c) 2015 iDreems. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "UITextView+Extras.h"

@interface SentenceModeController : UIViewController

-(void)setText:(NSString*)value;
-(void)setTapGranality:(UITextGranularity)value;

-(IBAction)return2ParentAction:(id)sender;
-(IBAction)sliderValueChangeAction:(UISlider*)sender;
-(IBAction)changeReviewFontSizeButtonAction:(id)sender;
-(IBAction)startTranslation:(id)sender;// 开始修正翻译
-(IBAction)finishTranslation:(id)sender;// 结束修正翻译
-(IBAction)submitTranslate:(id)sender;// 提交修正后的翻译结果
-(IBAction)switch2WordMode:(id)sender;

-(IBAction)speakOrigin:(id)sender;// 原句阅读
-(IBAction)recordOrigin:(id)sender;// 原句跟读
-(IBAction)playback:(id)sender;// 跟读回放

@property(nonatomic,readwrite,assign)IBOutlet UIButton* recordButton;
@property(nonatomic,readwrite,assign)IBOutlet UITextView* textView;
@property(nonatomic,readwrite,assign)IBOutlet UISlider* slider;
@property(nonatomic,readwrite,assign)IBOutlet UIView* fontChangeSlider;
@property(nonatomic,readwrite,assign)IBOutlet UITextView* translatedTextView;
@property(nonatomic,readwrite,assign)IBOutlet UIButton* submitButton;
@property(nonatomic,readwrite,assign)IBOutlet UIButton* closeButton;
@end
