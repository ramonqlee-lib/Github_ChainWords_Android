//
//  ViewController.h
//  NewsTongue
//
//  Created by ramonqlee on 1/23/15.
//  Copyright (c) 2015 iDreems. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PSCollectionView.h"
#import "PullPsCollectionView.h"

@interface MyFavoritesViewController : UIViewController<PSCollectionViewDelegate,PSCollectionViewDataSource,UIScrollViewDelegate,PullPsCollectionViewDelegate>

@property(nonatomic,retain) PullPsCollectionView *collectionView;
@property(nonatomic,retain)NSMutableArray *items;

@end

