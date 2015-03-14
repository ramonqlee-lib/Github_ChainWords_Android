//
//  ReadModeController.h
//  NewsTongue
//
//  Created by ramonqlee on 1/23/15.
//  Copyright (c) 2015 iDreems. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "UITextView+Extras.h"
#import "BaiduMobAdDelegateProtocol.h"

@interface ReadModeController : UIViewController<BaiduMobAdViewDelegate>

-(void)setText:(NSString*)value;
-(void)setTime:(NSInteger)time;
-(void)setTapGranality:(UITextGranularity)value;
-(void)setTitle:(NSString *)title;

-(IBAction)return2ParentAction:(id)sender;
-(IBAction)sliderValueChangeAction:(UISlider*)sender;
-(IBAction)changeReviewFontSizeButtonAction:(id)sender;

@property(nonatomic,readwrite,assign)IBOutlet UILabel* titleLabel;
@property(nonatomic,readwrite,assign)IBOutlet UITextView* textView;
@property(nonatomic,readwrite,assign)IBOutlet UISlider* slider;
@property(nonatomic,readwrite,assign)IBOutlet UIView* fontChangeSlider;
@property(nonatomic,readwrite,assign)IBOutlet UIView* adViewContainer;
@end
