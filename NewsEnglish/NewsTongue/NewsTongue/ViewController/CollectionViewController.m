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
    
    collectionView.numColsPortrait = 2;
    collectionView.numColsLandscape = 3;
    
    collectionView.pullArrowImage = [UIImage imageNamed:@"blackArrow"];
    collectionView.pullBackgroundColor = [UIColor yellowColor];
    collectionView.pullTextColor = [UIColor blackColor];
    //    UIView *headerView=[[UIView alloc]initWithFrame:CGRectMake(0, 0, 320, 45)];
    //    [headerView setBackgroundColor:[UIColor redColor]];
    //    self.collectionView.headerView=headerView;
    UILabel *loadingLabel = [[UILabel alloc] initWithFrame:self.collectionView.bounds];
    loadingLabel.text = @"Loading...";
    loadingLabel.textAlignment = NSTextAlignmentCenter;
    collectionView.loadingView = loadingLabel;
    
    //    [self loadDataSource];
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
    
    //    [self.items removeAllObjects];
    [self loadDataSource];
    self.collectionView.pullLastRefreshDate = [NSDate date];
    self.collectionView.pullTableIsRefreshing = NO;
    [self.collectionView reloadData];
}

- (void) loadMoreDataToTable
{
    /*
     
     Code to actually load more data goes here.
     
     */
    //    [self loadDataSource];
    //    [self.items addObjectsFromArray:self.items];
    [self.collectionView reloadData];
    self.collectionView.pullTableIsLoadingMore = NO;
}
#pragma mark - PullTableViewDelegate

- (void)pullPsCollectionViewDidTriggerRefresh:(PullPsCollectionView *)pullTableView
{
    [self performSelector:@selector(refreshTable) withObject:nil afterDelay:3.0f];
}

- (void)pullPsCollectionViewDidTriggerLoadMore:(PullPsCollectionView *)pullTableView
{
    [self performSelector:@selector(loadMoreDataToTable) withObject:nil afterDelay:3.0f];
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
    //    if (!v) {
    //        v = [[[PSCollectionViewCell alloc] initWithFrame:CGRectZero] autorelease];
    //    }
    if(v == nil) {
        NSArray *nib =
        [[NSBundle mainBundle] loadNibNamed:@"CellView" owner:self options:nil];
        v = [nib objectAtIndex:0];
    }
    
    //    [v fillViewWithObject:item];
    NSURL *URL = nil;
    if (item) {
        URL = [NSURL URLWithString:[NSString stringWithFormat:@"http://imgur.com/%@%@", [item objectForKey:@"hash"], [item objectForKey:@"ext"]]];
    }
    
    v.textView.text = [[item valueForKey:@"title"]objectForKey:@"0"];
    v.textView.backgroundColor = [UIColor blackColor];
    v.textView.textColor = [UIColor whiteColor];
    v.textView.font = [UIFont systemFontOfSize:15.0f];
    
    NSLog(@"%@",v.textView.text);
    [v.picView  setImageWithURL:URL placeholderImage:[UIImage imageNamed:@"placeholder"]];
    return v;
}

- (CGFloat)heightForViewAtIndex:(NSInteger)index {
    return 120;
//    NSDictionary *item = [self.items objectAtIndex:index];
//    
//    // You should probably subclass PSCollectionViewCell
//    return [PSCollectionViewCell heightForViewWithObject:item inColumnWidth:self.collectionView.colWidth];
}

- (void)collectionView:(PSCollectionView *)collectionView didSelectView:(PSCollectionViewCell *)view atIndex:(NSInteger)index {
    // Do something with the tap
    NSDictionary* item = [self.items objectAtIndex:index];
    NSString* contentStr = [item valueForKey:@"content"];
    contentStr = [contentStr base64DecodedString];
    contentStr = [contentStr stringByConvertingHTMLToPlainText];
    
    ReadModeController* controller = [[ReadModeController alloc]init];
    [controller setTapGranality:UITextGranularityParagraph];
    [controller setText:contentStr];
    [self presentViewController:controller animated:NO completion:nil];
}

- (NSInteger)numberOfViewsInCollectionView:(PSCollectionView *)collectionView {
    return [self.items count];
}

- (void)loadDataSource {
    // Request
    NSString *string = @"http://checknewversion.duapp.com/listnews.php";
    NSURL *url = [NSURL URLWithString:string];
    NSURLRequest *request = [NSURLRequest requestWithURL:url];
    
    AFHTTPRequestOperation *operation = [[AFHTTPRequestOperation alloc] initWithRequest:request];
    operation.responseSerializer = [AFJSONResponseSerializer serializer];
    
    [operation setCompletionBlockWithSuccess:^(AFHTTPRequestOperation *operation, id responseObject) {
        self.items = (NSArray *)responseObject;
        [self dataSourceDidLoad];
    } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
        [self dataSourceDidError];
    }];
    
    // 5
    [operation start];
}

- (void)dataSourceDidLoad {
    [self.collectionView reloadData];
}

- (void)dataSourceDidError {
    [self.collectionView reloadData];
}

//generate random numbers within range
-(int)generateRandomNumberBetweenMin:(int)min Max:(int)max
{
    return ( (arc4random() % (max-min+1)) + min );
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
