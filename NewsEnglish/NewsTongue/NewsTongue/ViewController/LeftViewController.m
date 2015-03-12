//
//  LeftViewController.m
//  WYApp
//
//  Created by chen on 14-7-17.
//  Copyright (c) 2014年 chen. All rights reserved.
//

#import "LeftViewController.h"
#import "ChatViewController.h"
#import "SliderViewController.h"
@interface LeftViewController ()
{
    NSArray *_arData;
}

@end

@implementation LeftViewController

- (void)viewDidLoad
{
    _arData = @[@"实时新闻",@"新闻收藏", @"翻译助手"];
    SEL selectors[] = {@selector(newsList:),@selector(localNewsList:),@selector(translator:)};
    
    [self.view setBackgroundColor:[UIColor clearColor]];
    UIImageView *imageBgV = [[UIImageView alloc] initWithFrame:self.view.bounds];
    [imageBgV setImage:[UIImage imageNamed:@"sidebar_bg.jpg"]];
    [self.view addSubview:imageBgV];
    
    __block float h = self.view.frame.size.height*0.7/[_arData count];
    __block float y = 0.15*self.view.frame.size.height;
    for (NSInteger i = 0; i < _arData.count; ++i)
    {
        NSString* obj = [_arData objectAtIndex:i];
        UIView *listV = [[UIView alloc] initWithFrame:CGRectMake(0, y, self.view.frame.size.width, h)];
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
-(void)localNewsList:(UIView*)view
{
    
}

-(void)newsList:(UIView*)view
{
    [[SliderViewController sharedSliderController] showLeftViewController];
}


-(void)translator:(UIView*)view
{
    UINavigationController *nav=[[[UINavigationController alloc]initWithRootViewController:[ChatViewController alloc]]init];
    
    
    [self presentViewController:nav animated:YES completion:nil];
}
@end
