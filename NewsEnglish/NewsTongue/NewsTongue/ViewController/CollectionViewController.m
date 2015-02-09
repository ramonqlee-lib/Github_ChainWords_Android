//
//  ViewController.m
//  NewsTongue
//
//  Created by ramonqlee on 1/23/15.
//  Copyright (c) 2015 iDreems. All rights reserved.
//

#import "CollectionViewController.h"
#import "ReadModeController.h"
#import "WordModeController.h"
#import "SentenceModeController.h"

#import "AFNetworking.h"
#import "Base64.h"
#import "NSString+HTML.h"

#import "PSCollectionViewCell.h"
#import "CellView.h"
#import "UIImageView+WebCache.h"

#import "UMFeedback.h"
#import "UMSocial.h"
#import "Constants.h"


@interface CollectionViewController ()

@end

@implementation CollectionViewController
@synthesize collectionView;
@synthesize items;
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        
        self.items = [NSMutableArray array];
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    collectionView = [[PullPsCollectionView alloc] initWithFrame:CGRectMake(0, 24, self.view.frame.size.width, self.view.frame.size.height)];
    [self.view addSubview:collectionView];
    
    collectionView.collectionViewDelegate = self;
    collectionView.collectionViewDataSource = self;
    collectionView.pullDelegate=self;
    collectionView.backgroundColor = [UIColor grayColor];
    collectionView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
    
    // 动态计算行数
    NSInteger CELL_WIDTH = 100;
    CGFloat screenWidth = [[UIScreen mainScreen]bounds].size.width;
    collectionView.numColsPortrait = screenWidth/CELL_WIDTH;
    
    collectionView.numColsLandscape = 3;
    
    collectionView.pullArrowImage = [UIImage imageNamed:@"blackArrow"];
    collectionView.pullBackgroundColor = [UIColor clearColor];
    collectionView.pullTextColor = [UIColor blackColor];
    UILabel *loadingLabel = [[UILabel alloc] initWithFrame:self.collectionView.bounds];
    
    NSArray* tipArr = [NSArray arrayWithObjects:@"点加号，反馈您的宝贵建议!",@"点加号，将我分享给你的好朋友!", nil];
    NSInteger pos =  arc4random() % tipArr.count;
    loadingLabel.text = [tipArr objectAtIndex:pos];
    loadingLabel.textAlignment = NSTextAlignmentCenter;
    collectionView.loadingView = loadingLabel;
    
    if(!collectionView.pullTableIsRefreshing) {
        collectionView.pullTableIsRefreshing = YES;
        [self performSelector:@selector(refreshTable) withObject:nil afterDelay:0];
    }
    
    [self addSystemMenu];
}
- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
}
- (void) refreshTable
{
    /*
     Code to actually refresh goes here.
     */
    // Request
    NSString *string = @"http://checknewversion.duapp.com/listnews.php";
    NSURL *url = [NSURL URLWithString:string];
    NSURLRequest *request = [NSURLRequest requestWithURL:url];
    
    AFHTTPRequestOperation *operation = [[AFHTTPRequestOperation alloc] initWithRequest:request];
    operation.responseSerializer = [AFJSONResponseSerializer serializer];
    
    [operation setCompletionBlockWithSuccess:^(AFHTTPRequestOperation *operation, id responseObject) {
        self.items = (NSArray *)responseObject;
        
        self.collectionView.pullLastRefreshDate = [NSDate date];
        self.collectionView.pullTableIsRefreshing = NO;
        
        [self dataSourceDidLoad];
    } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
        [self dataSourceDidError];
    }];
    
    // 5
    [operation start];
}

- (void) loadMoreDataToTable
{
    /*
     
     Code to actually load more data goes here.
     
     */
    //    [self.items addObjectsFromArray:self.items];
    [self.collectionView reloadData];
    self.collectionView.pullTableIsLoadingMore = NO;
}
#pragma mark - PullTableViewDelegate

- (void)pullPsCollectionViewDidTriggerRefresh:(PullPsCollectionView *)pullTableView
{
    [self performSelector:@selector(refreshTable) withObject:nil afterDelay:1.0f];
}

- (void)pullPsCollectionViewDidTriggerLoadMore:(PullPsCollectionView *)pullTableView
{
    [self performSelector:@selector(loadMoreDataToTable) withObject:nil afterDelay:1.0f];
}
- (void)viewDidUnload
{
    [self setCollectionView:nil];
    [super viewDidUnload];
    // Release any retained subviews of the main view.
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    return (interfaceOrientation != UIInterfaceOrientationPortraitUpsideDown);
}
- (PSCollectionViewCell *)collectionView:(PSCollectionView *)collectionView viewAtIndex:(NSInteger)index {
    NSDictionary *item = [self.items objectAtIndex:index];
    
    // You should probably subclass PSCollectionViewCell
    CellView *v = (CellView *)[self.collectionView dequeueReusableView];
    if(v == nil) {
        NSArray *nib =
        [[NSBundle mainBundle] loadNibNamed:@"CellView" owner:self options:nil];
        v = [nib objectAtIndex:0];
    }
    
    //    [v fillViewWithObject:item];
    NSURL *URL = nil;
    if (item) {
        URL = [NSURL URLWithString:[item valueForKey:@"thumbnail"]];
    }
    
    v.textView.text = [item valueForKey:@"title"];
    v.textView.backgroundColor = [UIColor blackColor];
    v.textView.textColor = [UIColor whiteColor];
    v.textView.font = [UIFont systemFontOfSize:15.0f];
    
    NSLog(@"%@",v.textView.text);
    [v.picView  setImageWithURL:URL placeholderImage:[UIImage imageNamed:@"placeholder"]];
    return v;
}

- (CGFloat)heightForViewAtIndex:(NSInteger)index {
    return 150;
}

- (void)collectionView:(PSCollectionView *)collectionView didSelectView:(PSCollectionViewCell *)view atIndex:(NSInteger)index {
    // Do something with the tap
    NSDictionary* item = [self.items objectAtIndex:index];
    NSString* contentStr = [item valueForKey:@"content"];
    contentStr = [contentStr base64DecodedString];
    contentStr = [contentStr stringByConvertingHTMLToPlainText];
    NSLog(@"%@",contentStr);
    
    ReadModeController* controller = [[ReadModeController alloc]init];
    [controller setTitle:[item objectForKey:@"title"]];
    [controller setTapGranality:UITextGranularityParagraph];
    [controller setText:contentStr];
    [self presentViewController:controller animated:NO completion:nil];
}

- (NSInteger)numberOfViewsInCollectionView:(PSCollectionView *)collectionView {
    return [self.items count];
}
- (void)dataSourceDidLoad {
    [self.collectionView reloadData];
}

- (void)dataSourceDidError {
    [self.collectionView reloadData];
}

#pragma QuadCurveMenuDelegate
- (void)addSystemMenu
{
    UIImage *storyMenuItemImage = [UIImage imageNamed:@"bg-menuitem.png"];
    UIImage *storyMenuItemImagePressed = [UIImage imageNamed:@"bg-menuitem-highlighted.png"];
    
    // About MenuItem.
    QuadCurveMenuItem *aboutMenuItem = [[QuadCurveMenuItem alloc] initWithImage:storyMenuItemImage
                                                                highlightedImage:storyMenuItemImagePressed
                                                                    ContentImage:[UIImage imageNamed:@"about.png"]
                                                         highlightedContentImage:nil];
    // Share MenuItem.
    QuadCurveMenuItem *shareMenuItem = [[QuadCurveMenuItem alloc] initWithImage:storyMenuItemImage
                                                                highlightedImage:storyMenuItemImagePressed
                                                                    ContentImage:[UIImage imageNamed:@"share.png"]
                                                         highlightedContentImage:nil];
    // Feedback MenuItem.
    QuadCurveMenuItem *feedbackMenuItem = [[QuadCurveMenuItem alloc] initWithImage:storyMenuItemImage
                                                               highlightedImage:storyMenuItemImagePressed
                                                                   ContentImage:[UIImage imageNamed:@"feedback.png"]
                                                        highlightedContentImage:nil];

    
    NSArray *menus = [NSArray arrayWithObjects:aboutMenuItem, shareMenuItem, feedbackMenuItem, nil];
    
    QuadCurveMenu *menu = [[QuadCurveMenu alloc] initWithFrame:self.view.bounds menus:menus];
    menu.delegate = self;
    [self.view addSubview:menu];
}

- (void)quadCurveMenu:(QuadCurveMenu *)menu didSelectIndex:(NSInteger)idx
{
//    NSLog(@"Select the index : %d",idx);
    switch (idx) {
        case 0:// about
            [self popupAbout:nil];
            break;
        case 1:// share
            [UMSocialSnsService presentSnsIconSheetView:self
                                                 appKey:UMENG_APP_KEY
                                              shareText:NSLocalizedString(@"ShareMessage", nil)
                                             shareImage:nil//[UIImage imageNamed:@"icon.png"]
                                        shareToSnsNames:[NSArray arrayWithObjects:UMShareToSina,UMShareToQQ,UMShareToTencent,UMShareToRenren,UMShareToWechatSession,UMShareToWechatTimeline,UMShareToEmail,UMShareToSms,nil]
                                               delegate:nil];
            break;
        case 2:// feedback
            [self presentViewController:[UMFeedback feedbackModalViewController] animated:YES completion:nil];
            break;
            
        default:
            break;
    }
}


-(IBAction)popupAbout:(id)sender
{
    UIAlertView *alertView = [[UIAlertView alloc] initWithTitle:@"About"
                                                        message:@"  Copyright (c) 2015 iDreems. All rights reserved."
                                                       delegate:self
                                              cancelButtonTitle:@"I Know"
                                              otherButtonTitles:nil, nil];
    [alertView show];
}
@end
