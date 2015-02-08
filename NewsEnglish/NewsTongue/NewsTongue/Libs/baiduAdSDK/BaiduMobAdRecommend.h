//
//  BaiduMobAdRecommend.h
//  BaiduMobAdSdk
//
//  Created by shaobo on 10/20/14.
//
//

#import <Foundation/Foundation.h>
#import "BaiduMobAdRecommendDelegateProtocol.h"

@interface BaiduMobAdRecommend : NSObject{
@private
    id<BaiduMobAdRecommendDelegate> _delegate;
}

///---------------------------------------------------------------------------------------
/// @name 属性
///---------------------------------------------------------------------------------------

/**
 *  委托对象
 */
@property (nonatomic ,assign) id<BaiduMobAdRecommendDelegate>  delegate;

/**
 *  SDK版本
 */
@property (nonatomic, readonly) NSString* Version;


/**
 *  展示推荐页面
 */
- (void)showRecommend;

@end
