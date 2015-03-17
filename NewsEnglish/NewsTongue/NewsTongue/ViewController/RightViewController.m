//
//  LeftViewController.m
//  WYApp
//
//  Created by chen on 14-7-17.
//  Copyright (c) 2014年 chen. All rights reserved.
//

#import "RightViewController.h"

#import "UMFeedback.h"
#import "UMSocial.h"

@interface RightViewController ()
{

}

@end

@implementation RightViewController

- (void)viewDidLoad
{
    NSArray *_arData = @[@"提建议", @"推荐给好友",@"关于"];
    SEL selectors[] = {@selector(feedback:),@selector(share:),@selector(about:)};
    
    [self.view setBackgroundColor:[UIColor clearColor]];
    UIImageView *imageBgV = [[UIImageView alloc] initWithFrame:self.view.bounds];
    [imageBgV setImage:[UIImage imageNamed:@"sidebar_bg.jpg"]];
    [self.view addSubview:imageBgV];
    
    __block float h = self.view.frame.size.height*0.7/[_arData count];
    __block float y = 0.15*self.view.frame.size.height;
    for (NSInteger i = 0; i < _arData.count; ++i)
    {
        NSString* obj = [_arData objectAtIndex:i];
        
        UIView *listV = [[UIView alloc] initWithFrame:CGRectMake(SCREEN_WIDTH-200, y, self.view.frame.size.width, h)];
        [listV setBackgroundColor:[UIColor clearColor]];
        UILabel *l = [[UILabel alloc] initWithFrame:CGRectMake(60, 0, listV.frame.size.width - 60, listV.frame.size.height)];
        [l setFont:[UIFont systemFontOfSize:20]];
        [l setTextColor:[UIColor whiteColor]];
        [l setBackgroundColor:[UIColor clearColor]];
        [l setText:obj];
        [listV addSubview:l];
        
        // event
        UITapGestureRecognizer *single_tap_recognizer = [[UITapGestureRecognizer alloc] initWithTarget : self action: selectors[i]];
        [single_tap_recognizer setNumberOfTouchesRequired : 1];
        [listV addGestureRecognizer : single_tap_recognizer];
        
        [self.view addSubview:listV];
        y += h;
    };
}

#pragma mark selectors

-(void)feedback:(UIView*)view
{
    [self presentViewController:[UMFeedback feedbackModalViewController] animated:YES completion:nil];
}
-(void)about:(UIView*)view
{
    UIAlertView *alertView = [[UIAlertView alloc] initWithTitle:@"About"
                                                        message:@"  Copyright (c) 2015 iDreems. All rights reserved."
                                                       delegate:self
                                              cancelButtonTitle:@"I Know"
                                              otherButtonTitles:nil, nil];
    [alertView show];
}
-(void)share:(UIView*)view
{
    [UMSocialSnsService presentSnsIconSheetView:self
                                         appKey:UMENG_APP_KEY
                                      shareText:NSLocalizedString(@"ShareMessage", nil)
                                     shareImage:nil//[UIImage imageNamed:@"icon.png"]
                                shareToSnsNames:[NSArray arrayWithObjects:UMShareToSina,UMShareToQQ,UMShareToTencent,UMShareToWechatSession,UMShareToWechatTimeline,UMShareToEmail,UMShareToSms,nil]
                                       delegate:nil];
}
-(void)setting:(UIView*)view
{
    NSLog(@"setting");
}
@end
