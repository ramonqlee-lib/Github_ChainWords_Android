//
//  UIImage+StrethImage.m
//  气泡
//
//  Created by zzy on 14-5-13.
//  Copyright (c) 2014年 zzy. All rights reserved.
//

#import "UIImage+StretchImage.h"

@implementation UIImage (StretchImage)

+(id)stretchImageWith:(NSString *)imageName
{
    UIImage *image=[UIImage imageNamed:imageName];
    
    image=[image stretchableImageWithLeftCapWidth:image.size.width*0.5 topCapHeight:image.size.height*0.5];
    
    return image;
}
@end
