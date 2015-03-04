//
//  UITestWebViewViewController.m
//  TestWebView
//
//  Created by ramonqlee on 2/11/15.
//  Copyright (c) 2015 iDreems. All rights reserved.
//

#import "UITestWebViewViewController.h"

@interface UITestWebViewViewController ()

@end

@implementation UITestWebViewViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    [NSHTTPCookieStorage sharedHTTPCookieStorage].cookieAcceptPolicy = NSHTTPCookieAcceptPolicyAlways;
//    [NSHTTPCookieStorage sharedHTTPCookieStorage].cookieAcceptPolicy = NSHTTPCookieAcceptPolicyOnlyFromMainDocumentDomain;
    
    NSLog(@"cookie policy %d",[NSHTTPCookieStorage sharedHTTPCookieStorage].cookieAcceptPolicy);
    // Do any additional setup after loading the view, typically from a nib.
    NSString* url = @"http://m.dev.yees.com.cn/act/app_wv/fy2/airport_meet?unique_id=1F65D120-1D16-43A1-A8FB-9818C4C727AC&phone=13695690965&flight=CZ3818&date=2015-02-12&time=1423648568&sign=932b8881f4da2c4bc09878477c286ce0&";
    CGRect rc = [[UIScreen mainScreen]bounds];
//    UIWebView *webview=[[UIWebView alloc]initWithFrame:rc];
    
    NSURLRequest *nsrequest=[NSURLRequest requestWithURL:[NSURL URLWithString:url]];
    [self.webview loadRequest:nsrequest];
//    [self.view addSubview:webview];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}
-(IBAction)close:(id)sender
{
    [self dismissViewControllerAnimated:YES completion:nil];
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
