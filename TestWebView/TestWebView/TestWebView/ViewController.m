//
//  ViewController.m
//  TestWebView
//
//  Created by ramonqlee on 2/11/15.
//  Copyright (c) 2015 iDreems. All rights reserved.
//

#import "ViewController.h"
#import "UITestWebViewViewController.h"

@interface ViewController ()

@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

-(IBAction)openWebview:(id)sender
{
    UIViewController* controller = [[UITestWebViewViewController alloc]initWithNibName:@"UITestWebViewViewController" bundle:nil];
    [self presentViewController:controller animated:YES completion:nil];
}

@end
