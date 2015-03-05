//
//  ReadModeController.m
//  NewsTongue
//
//  Created by ramonqlee on 1/23/15.
//  Copyright (c) 2015 iDreems. All rights reserved.
//

#import "ReadModeController.h"
#import "WordModeController.h"
#import "SentenceModeController.h"
#import "MobClick.h"
#import "Constants.h"
#import "NSString+HTML.h"

static const CGFloat kLineSpacing = 5.0f;// 行间距
static const CGFloat kMinFontSize = 18.0f;// 字体缩放的最小值
static const CGFloat kMaxFontSize = 38.0f;// 字体缩放的最大值

@interface ReadModeController ()<UITextViewDelegate>
{
    UITextGranularity mGranuality;
    NSString* bodyText;
    NSString* titileText;
    CGFloat fontSize;
    NSInteger updated;
}
@end

@implementation ReadModeController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    
    self.titleLabel.text = titileText;
    if(nil != bodyText && bodyText.length > 0)
    {
#if 0
        NSAttributedString *attributedString = [[NSAttributedString alloc] initWithData:[mBodyText dataUsingEncoding:NSUnicodeStringEncoding] options:@{ NSDocumentTypeDocumentAttribute: NSHTMLTextDocumentType } documentAttributes:nil error:nil];
#else
        NSAttributedString* attributedString = [self getFormatedBody];//[[NSAttributedString alloc]initWithString:bodyText];
#endif
        _textView.attributedText = attributedString;
    }
    [_textView scrollRangeToVisible:NSMakeRange(0, 0)];
    _textView.editable = NO;
    _textView.delegate = self;
    [_textView setLineSpacing:kLineSpacing clearPrevious:NO];
    
    CGFloat val = (CGFloat)0xf0/256;
    _textView.backgroundColor = [UIColor colorWithRed:val green:val blue:val alpha:1];
    
    UITapGestureRecognizer* tap = [[UITapGestureRecognizer alloc]initWithTarget:self action:@selector(_handleTap:)];
    
    [_textView addGestureRecognizer:tap];
    
    // init slider
    if(nil != self.slider)
    {
        _slider.minimumValue = kMinFontSize;
        _slider.maximumValue = kMaxFontSize;
    }
    
    //hide font change silder
    if(nil != _fontChangeSlider)
    {
        _fontChangeSlider.hidden = YES;
    }
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}
-(void)setTitle:(NSString *)title{
    titileText = title;
}

-(void)setTime:(NSInteger)time{
    updated = time;
}

-(void)setText:(NSString*)value
{
    if(nil == _textView)
    {
        bodyText = value;
        return;
    }
#if 0
    NSAttributedString *attributedString = [[NSAttributedString alloc] initWithData:[value dataUsingEncoding:NSUnicodeStringEncoding] options:@{ NSDocumentTypeDocumentAttribute: NSHTMLTextDocumentType } documentAttributes:nil error:nil];
#else
    NSAttributedString* attributedString = [self getFormatedBody];//[[NSAttributedString alloc]initWithString:bodyText];
#endif
    _textView.attributedText = attributedString;
}
-(NSAttributedString*)getFormatedBody
{
    NSDate* date = [NSDate dateWithTimeIntervalSince1970:updated];
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    dateFormatter.dateFormat = @"MM-dd hh:mm";
    NSString *formatedTime = [dateFormatter stringFromDate:date];

    NSString* tmp = [NSString stringWithFormat:@"%@\n%@\n\n%@",titileText,formatedTime,bodyText];//titileText + time + bodyText;
    NSMutableAttributedString *hogan = [[NSMutableAttributedString alloc] initWithString:tmp];
    [hogan addAttribute:NSFontAttributeName
                  value:[UIFont systemFontOfSize:25.0]
                  range:NSMakeRange(0, titileText.length)];
    [hogan addAttribute:NSFontAttributeName
                  value:[UIFont systemFontOfSize:15.0]
                  range:NSMakeRange(titileText.length+1,formatedTime.length)];
    
    // body setting
    fontSize = kMinFontSize;
    UIFont* bodyFont = [UIFont fontWithName:_textView.font.fontName size:fontSize];
    NSInteger start = titileText.length+1+formatedTime.length;
    NSInteger len = tmp.length-start;
    [hogan addAttribute:NSFontAttributeName
                  value:bodyFont
                  range:NSMakeRange(start,len)];
    
    CGFloat val = (CGFloat)0x40/256;
    [hogan addAttribute:NSForegroundColorAttributeName
                  value:[UIColor colorWithRed:val green:val blue:val alpha:1]
                  range:NSMakeRange(start,len)];
    
    return hogan;
}

-(void)setTapGranality:(UITextGranularity)value
{
    mGranuality = value;
}
/*
 #pragma mark - Navigation
 
 // In a storyboard-based application, you will often want to do a little preparation before navigation
 - (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
 // Get the new view controller using [segue destinationViewController].
 // Pass the selected object to the new view controller.
 }
 */
#pragma mark - actions
-(IBAction)return2ParentAction:(id)sender
{
    [self dismissViewControllerAnimated:NO completion:nil];
}
-(IBAction)sliderValueChangeAction:(UISlider*)sender
{
    NSLog(@"value: %f",sender.value);
    if (nil == _textView) {
        return;
    }
    CGFloat inc = sender.value - fontSize;
    fontSize = sender.value;
    //_textView.font = [UIFont fontWithName:_textView.font.fontName size:sender.value];
    //  遍历，逐项size进行相应变动
    NSMutableAttributedString *res = [self.textView.attributedText mutableCopy];
    [res beginEditing];
    __block BOOL found = NO;
    [res enumerateAttribute:NSFontAttributeName inRange:NSMakeRange(0, res.length) options:0 usingBlock:^(id value, NSRange range, BOOL *stop) {
        if (value) {
            UIFont *oldFont = (UIFont *)value;
            UIFont *newFont = [oldFont fontWithSize:oldFont.pointSize + inc];
            NSLog(@"font size: %f",oldFont.pointSize + inc);
            [res removeAttribute:NSFontAttributeName range:range];
            [res addAttribute:NSFontAttributeName value:newFont range:range];
            found = YES;
        }
    }];
    if (!found) {
        // No font was found - do something else?
    }
    [res endEditing];
    self.textView.attributedText = res;
}
-(IBAction)changeReviewFontSizeButtonAction:(id)sender
{
    [MobClick event:CHANGE_FONT_SIZE_AT_READ_MODE_PAGE];
    if (nil == _fontChangeSlider) {
        return;
    }
    
    _fontChangeSlider.hidden = !_fontChangeSlider.hidden;
    
    // 设置slider的当前值
    if (!_fontChangeSlider.hidden) {
        _slider.value = _textView.font.pointSize;
    }
}

#pragma mark - Gesture Handling

-(void)_handleTap:(UITapGestureRecognizer*)tap{
    // hide
    _fontChangeSlider.hidden = YES;
    _textView.layoutManager.allowsNonContiguousLayout = NO;
    
    if(tap.state == UIGestureRecognizerStateRecognized){
        
        CGPoint tappedPoint = [tap locationInView:_textView];
        
        UITextGranularity granularity = mGranuality;
        
        NSString* tappedString = [_textView substringWithGranularity:granularity atPoint:tappedPoint];

        if (nil == tappedString || !tappedString.length || ![[NSString stringWithString:tappedString] stringByTrimmingStopCharactersInSet].length) {
            return;
        }
        NSLog(@"%@",tappedString);
        
        NSRange range = [_textView rangeWithGranularity:granularity atPoint:tappedPoint];
//        [_textView setLineSpacing:kLineSpacing clearPrevious:YES];
//        _textView.font = [UIFont fontWithName:_textView.font.fontName size:fontSize];
        
        [_textView addColor:range value:[UIColor redColor] clearPrevious:NO];
        UIFont* font = _textView.font;
        NSLog(@"font point size: %f",font.pointSize);
        
        // change selected font
        font = [UIFont fontWithName:font.fontName size:font.pointSize*1.2];
        [_textView setFont:range value:font clearPrevious:NO];
        
        [self selectPragraph:[_textView.text substringWithRange:range]];
    }
    
}

-(void)selectPragraph:(NSString*)text
{
    // 是否选择了合法的输入
    if (!text || !text.length || ![[NSString stringWithString:text] stringByTrimmingStopCharactersInSet].length) {
        return;
    }
    
    // 进入学习模式：单词模式和单句模式
    // TODO 通过上下文弹出菜单方式，确认进入的模式，缺省进入单句模式
    SentenceModeController* controller = [[SentenceModeController alloc]init];
    [controller setTapGranality:UITextGranularitySentence];
    [controller setText:text];
    [self presentViewController:controller animated:NO completion:nil];
}

@end
