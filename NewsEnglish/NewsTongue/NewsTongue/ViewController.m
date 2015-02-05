//
//  ViewController.m
//  NewsTongue
//
//  Created by ramonqlee on 1/23/15.
//  Copyright (c) 2015 iDreems. All rights reserved.
//

#import "ViewController.h"
#import "ReadModeController.h"
#import "WordModeController.h"
#import "SentenceModeController.h"

#import "AFNetworking.h"
#import "Base64.h"
#import "NSString+HTML.h"

@interface ViewController ()

@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view, typically from a nib.
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

-(IBAction)enterDetailPage:(id)sender
{
#if 0
    WordModeController* controller = [[WordModeController alloc]init];
    SentenceModeController* controller = [[SentenceModeController alloc]init];
    [controller setTapGranality:UITextGranularitySentence];
    
    [controller setTapGranality:UITextGranularityWord];
#endif
    
    // 从服务器端获取新闻列表，并进行展示
    // TODO 1. 本地列表
    // 2. 点击进入阅读视图
    [self requestNewsList:^(NSArray* responseObjects)
     {
         // 获取第一个，测试下阅读模式
         if (!responseObjects || !responseObjects.count) {
             return;
         }
         NSDictionary* item = [responseObjects objectAtIndex:0];
         NSString* contentStr = [item valueForKey:@"content"];
         contentStr = [contentStr base64DecodedString];
         contentStr = [contentStr stringByConvertingHTMLToPlainText];
         
         ReadModeController* controller = [[ReadModeController alloc]init];
         [controller setTapGranality:UITextGranularityParagraph];
         [controller setText:contentStr];
         [self presentViewController:controller animated:NO completion:nil];
     }
    failure:^(NSError *error)
     {
         UIAlertView *alertView = [[UIAlertView alloc] initWithTitle:@"Error Request News"
                                                             message:[error localizedDescription]
                                                            delegate:nil
                                                   cancelButtonTitle:@"Ok"
                                                   otherButtonTitles:nil];
         [alertView show];
     }];
    
    
}
// TODO 后续考虑增加缓存
-(void)requestNewsList:(void (^)(NSArray* responseObject))success failure:(void (^)(NSError *error))failure
{
    NSString *string = @"http://checknewversion.duapp.com/listnews.php";
    NSURL *url = [NSURL URLWithString:string];
    NSURLRequest *request = [NSURLRequest requestWithURL:url];
    
    AFHTTPRequestOperation *operation = [[AFHTTPRequestOperation alloc] initWithRequest:request];
    operation.responseSerializer = [AFJSONResponseSerializer serializer];
    
    [operation setCompletionBlockWithSuccess:^(AFHTTPRequestOperation *operation, id responseObject) {
        NSArray* dict = (NSArray *)responseObject;
        //        NSLog(@"%@",dict);
        // TODO 保存到本地
        if (success) {
            // 在主线程中更新
            success(dict);
        }
    } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
        if (failure) {
            failure(error);
        }
    }];
    
    // 5
    [operation start];
}

@end
