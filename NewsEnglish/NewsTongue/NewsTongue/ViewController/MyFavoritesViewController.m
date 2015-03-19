//
//  ViewController.m
//  NewsTongue
//
//  Created by ramonqlee on 1/23/15.
//  Copyright (c) 2015 iDreems. All rights reserved.
//

#import "MyFavoritesViewController.h"
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
#import "DbCache.h"

#define LOAD_COUNT 30 // 每次加载更多，请求的条数
#define MENU_HEIGHT 0//25
#define MENU_BUTTON_WIDTH  60

@interface MyFavoritesViewController ()
{
    UIView *_navView;
    UIView *_topNaviV;
    UIScrollView *_navScrollV;
    UIView *_navBgV;
}
@end

@implementation MyFavoritesViewController
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
    [titleLabel setText:@"收藏列表"];
    [titleLabel setTextAlignment:NSTextAlignmentCenter];
    [titleLabel setTextColor:[UIColor whiteColor]];
    [titleLabel setFont:[UIFont boldSystemFontOfSize:18]];
    [titleLabel setBackgroundColor:[UIColor clearColor]];
    [_navView addSubview:titleLabel];
    
    UIButton *lbtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [lbtn setFrame:CGRectMake(10, 2, 40, 40)];
    [lbtn setTitle:NSLocalizedString(@"RETURN", "") forState:UIControlStateNormal];
    [lbtn addTarget:self action:@selector(backAction:) forControlEvents:UIControlEventTouchUpInside];
    [_navView addSubview:lbtn];
    
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
    // TODO 异步加载
    DbCache* cache =[[DbCache alloc]init];
    cache.dbName = kNewsCacheDbName;
    cache.tableName = kNewsCacheTableName;
    NSString* sql = [NSString stringWithFormat:@"SELECT * FROM %@ order by %@ desc LIMIT %d OFFSET %d",kNewsCacheTableName,kUpdated,LOAD_COUNT,0];
    NSArray* ret = [cache get:sql];
    NSLog(@"count: %lu",(unsigned long)ret.count);
    if (ret.count > 0) {
        if (!self.items) {
            self.items = [[NSMutableArray alloc] initWithCapacity:10];
        }
        [self.items removeAllObjects];
        [self.items addObjectsFromArray:ret];
    }
    self.collectionView.pullLastRefreshDate = [NSDate date];
    self.collectionView.pullTableIsRefreshing = NO;
    
    [self dataSourceDidLoad];
}

- (void) loadMoreDataToTable
{
    /*
     
     Code to actually load more data goes here.
     
     */
    DbCache* cache =[[DbCache alloc]init];
    cache.dbName = kNewsCacheDbName;
    cache.tableName = kNewsCacheTableName;
    NSString* sql = [NSString stringWithFormat:@"SELECT * FROM %@ order by %@ desc LIMIT %d OFFSET %lu",kNewsCacheTableName,kUpdated,LOAD_COUNT,(unsigned long)self.items.count];
    NSArray* ret = [cache get:sql];
    NSLog(@"count: %lu",(unsigned long)ret.count);
    if (ret.count > 0) {
        if (!self.items) {
            self.items = [[NSMutableArray alloc] initWithCapacity:10];
        }
        [self.items addObjectsFromArray:ret];
    }
    
    self.collectionView.pullLastRefreshDate = [NSDate date];
    self.collectionView.pullTableIsLoadingMore = NO;
    
    [self dataSourceDidLoad];
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
        URL = [NSURL URLWithString:[[item valueForKey:@"thumbnail"] base64DecodedString]];
    }
    
    v.textView.text = [[item valueForKey:@"title"] base64DecodedString];
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
    controller.data = item;
    [controller setTitle:[[item objectForKey:@"title"] base64DecodedString]];
    [controller setTime: [updated intValue]];
    [controller setTapGranality:UITextGranularityParagraph];
    [controller setText:contentStr];
    [self presentViewController:controller animated:NO completion:^(void){
        controller.add2FavButton.hidden = YES;
    }];
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


#pragma return action

- (void)backAction:(UIButton *)btn
{
    [self dismissViewControllerAnimated:YES completion:nil];
}


@end
