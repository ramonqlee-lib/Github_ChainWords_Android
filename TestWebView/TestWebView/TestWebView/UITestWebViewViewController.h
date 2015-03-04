//
//  UITestWebViewViewController.h
//  TestWebView
//
//  Created by ramonqlee on 2/11/15.
//  Copyright (c) 2015 iDreems. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface UITestWebViewViewController : UIViewController


-(IBAction)close:(id)sender;

@property(nonatomic,readwrite,assign)IBOutlet UIWebView* webview;
@end
