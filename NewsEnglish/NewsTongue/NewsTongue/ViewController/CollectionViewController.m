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
#import "Base64Simple.h"
#import "NSString+HTML.h"

#import "PSCollectionViewCell.h"
#import "CellView.h"
#import "UIImageView+WebCache.h"

#import "Constants.h"
#import "SliderViewController.h"

#define LOAD_COUNT 30 // 每次加载更多，请求的条数
#define MENU_HEIGHT 0//25
#define MENU_BUTTON_WIDTH  60

@interface CollectionViewController ()
{
    UIView *_navView;
    UIView *_topNaviV;
    UIScrollView *_navScrollV;
    UIView *_navBgV;
}
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
    
    self.view.backgroundColor = [UIColor whiteColor];
    UIView *statusBarView = [[UIImageView alloc] initWithFrame:CGRectMake(0.f, 0.f, self.view.frame.size.width, 0.f)];
    if (isIos7 >= 7 && __IPHONE_OS_VERSION_MAX_ALLOWED > __IPHONE_6_1)
    {
        statusBarView.frame = CGRectMake(statusBarView.frame.origin.x, statusBarView.frame.origin.y, statusBarView.frame.size.width, 20.f);
        statusBarView.backgroundColor = [UIColor clearColor];
        ((UIImageView *)statusBarView).backgroundColor = RGBA(159,5,13,1);
        [self.view addSubview:statusBarView];
    }
    
    _navView = [[UIImageView alloc] initWithFrame:CGRectMake(0.f, StatusbarSize, self.view.frame.size.width, 44.f)];
    ((UIImageView *)_navView).backgroundColor = RGBA(159,5,13,1);
    [self.view insertSubview:_navView belowSubview:statusBarView];
    _navView.userInteractionEnabled = YES;
    
    UILabel *titleLabel = [[UILabel alloc] initWithFrame:CGRectMake((_navView.frame.size.width - 200)/2, (_navView.frame.size.height - 40)/2, 200, 40)];
    [titleLabel setText:@"新闻列表"];
    [titleLabel setTextAlignment:NSTextAlignmentCenter];
    [titleLabel setTextColor:[UIColor whiteColor]];
    [titleLabel setFont:[UIFont boldSystemFontOfSize:18]];
    [titleLabel setBackgroundColor:[UIColor clearColor]];
    [_navView addSubview:titleLabel];
    
    UIButton *lbtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [lbtn setFrame:CGRectMake(10, 2, 40, 40)];
    [lbtn setTitle:@"左" forState:UIControlStateNormal];
    [lbtn addTarget:self action:@selector(leftAction:) forControlEvents:UIControlEventTouchUpInside];
    [_navView addSubview:lbtn];
    
    UIButton *rbtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [rbtn setFrame:CGRectMake(_navView.frame.size.width - 50, 2, 40, 40)];
    [rbtn setTitle:@"右" forState:UIControlStateNormal];
    [rbtn addTarget:self action:@selector(rightAction:) forControlEvents:UIControlEventTouchUpInside];
    [_navView addSubview:rbtn];
    
    _topNaviV = [[UIView alloc] initWithFrame:CGRectMake(0, _navView.frame.size.height + _navView.frame.origin.y, self.view.frame.size.width, MENU_HEIGHT)];
    //    [_topNaviV setBackgroundColor:[UIColor greenColor]];
    _topNaviV.backgroundColor = RGBA(236.f, 236.f, 236.f, 1);
    [self.view addSubview:_topNaviV];
    
    collectionView = [[PullPsCollectionView alloc] initWithFrame:CGRectMake(0, _topNaviV.frame.origin.y + _topNaviV.frame.size.height, self.view.frame.size.width, self.view.frame.size.height - _topNaviV.frame.origin.y - _topNaviV.frame.size.height)];
    [self.view insertSubview:collectionView belowSubview:_navView];
    
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
    
    NSArray* tipArr = [NSArray arrayWithObjects:@"反馈您的宝贵建议!",@"将我分享给你的好朋友!", nil];
    NSInteger pos =  arc4random() % tipArr.count;
    loadingLabel.text = [tipArr objectAtIndex:pos];
    loadingLabel.textAlignment = NSTextAlignmentCenter;
    collectionView.loadingView = loadingLabel;
    
    if(!collectionView.pullTableIsRefreshing) {
        collectionView.pullTableIsRefreshing = YES;
        [self performSelector:@selector(refreshTable) withObject:nil afterDelay:0];
    }
    
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
    // 支持分页加载
    NSString *string = [NSString stringWithFormat:@"http://checknewversion.duapp.com/newsladder/listnews.php?offset=%d&count=%d",0,LOAD_COUNT];
    NSURL *url = [NSURL URLWithString:string];
    NSURLRequest *request = [NSURLRequest requestWithURL:url];
    
    AFHTTPRequestOperation *operation = [[AFHTTPRequestOperation alloc] initWithRequest:request];
    operation.responseSerializer = [AFJSONResponseSerializer serializer];
    
    [operation setCompletionBlockWithSuccess:^(AFHTTPRequestOperation *operation, id responseObject) {
        if ([responseObject isKindOfClass:[NSArray class]])
        {
            if (!self.items) {
                self.items = [[NSMutableArray alloc] initWithCapacity:10];
            }
            [self.items removeAllObjects];
            [self.items addObjectsFromArray:(NSArray *)responseObject];
        }
        
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
    NSString *string = [NSString stringWithFormat:@"http://checknewversion.duapp.com/newsladder/listnews.php?offset=%lu&count=%d&stat=1",(unsigned long)self.items.count,LOAD_COUNT];
    NSURL *url = [NSURL URLWithString:string];
    NSURLRequest *request = [NSURLRequest requestWithURL:url];
    
    AFHTTPRequestOperation *operation = [[AFHTTPRequestOperation alloc] initWithRequest:request];
    operation.responseSerializer = [AFJSONResponseSerializer serializer];
    
    [operation setCompletionBlockWithSuccess:^(AFHTTPRequestOperation *operation, id responseObject) {
        
        if (![responseObject isKindOfClass:[NSDictionary class]]) {
            self.collectionView.pullLastRefreshDate = [NSDate date];
            self.collectionView.pullTableIsLoadingMore = NO;
            
            [self dataSourceDidLoad];
            return;
        }
        
        NSString* countStr = [((NSDictionary*)responseObject) objectForKey:@"count"];
        NSArray* data = [((NSDictionary*)responseObject) objectForKey:@"data"];
        // 是否还有更多数据
        if(self.items.count < countStr.integerValue)
        {
            if (!self.items) {
                self.items = [[NSMutableArray alloc] initWithCapacity:10];
            }
            [self.items addObjectsFromArray:data];
        }
        
        
        self.collectionView.pullLastRefreshDate = [NSDate date];
        self.collectionView.pullTableIsLoadingMore = NO;
        
        [self dataSourceDidLoad];
    } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
        [self dataSourceDidError];
    }];
    
    // 5
    [operation start];
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
    NSString* updated = [item objectForKey:@"updated"];
    ReadModeController* controller = [[ReadModeController alloc]init];
    [controller setTitle:[item objectForKey:@"title"]];
    [controller setTime: [updated intValue]];
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


#pragma left and right action

- (void)leftAction:(UIButton *)btn
{
    //    if ([collectionView isHidden] == NO)
    //    {
    //        [self showSelectView:btn];
    //        return;
    //    }
    [((SliderViewController *)[[[self.view superview] superview] nextResponder]) showLeftViewController];
}

- (void)rightAction:(UIButton *)btn
{
    //    if ([collectionView isHidden] == NO)
    //    {
    //        [self showSelectView:btn];
    //        return;
    //    }
    [((SliderViewController *)[[[self.view superview] superview] nextResponder]) showRightViewController];
}

- (void)showSelectView:(UIButton *)btn
{
    if ([collectionView isHidden] == YES)
    {
        [collectionView setHidden:NO];
        [UIView animateWithDuration:0.6 animations:^
         {
             //             [collectionView setFrame:CGRectMake(0, collectionView.frame.origin.y, collectionView.frame.size.width, collectionView.frame.size.height)];
         } completion:^(BOOL finished)
         {
         }];
    }else
    {
        [UIView animateWithDuration:0.6 animations:^
         {
             //             [_selectTabV setFrame:CGRectMake(0, _scrollV.frame.origin.y - _scrollV.frame.size.height, _scrollV.frame.size.width, _scrollV.frame.size.height)];
         } completion:^(BOOL finished)
         {
             [collectionView setHidden:YES];
         }];
    }
}



@end
