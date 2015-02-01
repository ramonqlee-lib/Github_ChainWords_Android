//
//  ReadModeController.h
//  NewsTongue
//
//  Created by ramonqlee on 1/23/15.
//  Copyright (c) 2015 iDreems. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "UITextView+Extras.h"

@interface WordModeController : UIViewController

-(void)setText:(NSString*)value;
-(void)setTapGranality:(UITextGranularity)value;

-(IBAction)return2ParentAction:(id)sender;
-(IBAction)sliderValueChangeAction:(UISlider*)sender;
-(IBAction)changeReviewFontSizeButtonAction:(id)sender;

-(IBAction)speakWord:(id)sender;
-(IBAction)recordWord:(id)sender;
-(IBAction)replayWord:(id)sender;

@property(nonatomic,readwrite,assign)IBOutlet UITextView* textView;
@property(nonatomic,readwrite,assign)IBOutlet UISlider* slider;
@property(nonatomic,readwrite,assign)IBOutlet UIView* fontChangeSlider;
@property(nonatomic,readwrite,assign)IBOutlet UITextView* translatedTextView;
@end
