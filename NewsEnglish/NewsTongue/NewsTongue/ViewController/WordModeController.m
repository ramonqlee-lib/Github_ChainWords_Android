//
//  ReadModeController.m
//  NewsTongue
//
//  Created by ramonqlee on 1/23/15.
//  Copyright (c) 2015 iDreems. All rights reserved.
//

#import "WordModeController.h"
#import "WordManager.h"
#import "AFSoundManager.h"
#import "AFURLSessionManager.h"
#import "BaiduMobAdView.h"
#import "Constants.h"
#import "NSString+HTML.h"
#import "Toast+UIView.h"

static const CGFloat kLineSpacing = 5.0f;// 行间距
static const CGFloat kMinFontSize = 18.0f;// 字体缩放的最小值
static const CGFloat kMaxFontSize = 38.0f;// 字体缩放的最大值

@interface WordModeController ()<UITextViewDelegate,WordQueryResult>
{
    UITextGranularity mGranuality;
    NSString* mBodyText;
    CGFloat fontSize;
    NSString* word;
    NSString* fromLang;
    NSString* toLang;
    NSDictionary* wordDict;
    
    BOOL shortMeaning;
}
@end

@implementation WordModeController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    fromLang = @"en";
    toLang = @"zh";
    shortMeaning = NO;
    [self startAd];
    
    if(nil != mBodyText && mBodyText.length > 0)
    {
        _textView.attributedText = [[NSAttributedString alloc]initWithString:mBodyText];
    }
    _textView.editable = NO;
    _textView.delegate = self;
    fontSize = kMinFontSize;
    _textView.font = [UIFont fontWithName:_textView.font.fontName size:fontSize];
    [_textView setLineSpacing:kLineSpacing clearPrevious:NO];
    CGFloat val = (CGFloat)0x40/256;
    _textView.textColor = [UIColor colorWithRed:val green:val blue:val alpha:1];
    
    val = (CGFloat)0xf0/256;
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

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

-(void)setText:(NSString*)value
{
    if(nil == _textView)
    {
        mBodyText = value;
        return;
    }
    _textView.attributedText = [[NSAttributedString alloc]initWithString:value];
}

-(void)setTapGranality:(UITextGranularity)value
{
    mGranuality = value;
}


#pragma mark - Gesture Handling

-(void)_handleTap:(UITapGestureRecognizer*)tap{
    // hide
    _fontChangeSlider.hidden = YES;
    word = @"";
    wordDict = nil;
    
    if(tap.state == UIGestureRecognizerStateRecognized){
        
        CGPoint tappedPoint = [tap locationInView:_textView];
        
        UITextGranularity granularity = mGranuality;
        
        NSString* tappedString = [_textView substringWithGranularity:granularity atPoint:tappedPoint];
        if (nil == tappedString || !tappedString.length || ![[NSString stringWithString:tappedString] stringByTrimmingStopCharactersInSet].length) {
            return;
        }
        NSLog(@"%@",tappedString);
        
        NSRange range = [_textView rangeWithGranularity:granularity atPoint:tappedPoint];
        [_textView setLineSpacing:kLineSpacing clearPrevious:YES];
        _textView.font = [UIFont fontWithName:_textView.font.fontName size:fontSize];
        
        [_textView addColor:range value:[UIColor redColor] clearPrevious:NO];
        UIFont* font = _textView.font;
        NSLog(@"font point size: %f",font.pointSize);
        
        // change selected font
        font = [UIFont fontWithName:font.fontName size:font.pointSize*1.2];
        [_textView setFont:range value:font clearPrevious:NO];
        word = [[_textView.text substringWithRange:range]lowercaseString];
        [self updateTranslatedText:word];
    }
}

-(void)updateTranslatedText:(NSString*)originText
{
    if (!originText || !originText.length || ![[NSString stringWithString:originText] stringByTrimmingStopCharactersInSet].length) {
        return;
    }
    
    [self showProgress];
    _translatedTextView.text = @"";
    //  翻译单词
    [[WordManager sharedInstance]query:[originText lowercaseString] from:fromLang to:toLang response:self];
    
}
#pragma mark WordQueryResult protocol

-(void)handleWordResponse:(NSDictionary*)result
{
    [self hideProgress];
    wordDict = result;
    // 单词意义
//    _translatedTextView.text = [WordManager getReadableMeaning:wordDict];
    [self extendWordDetail:nil];
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
    fontSize = sender.value;
    _textView.font = [UIFont fontWithName:_textView.font.fontName size:sender.value];
    _translatedTextView.font = [UIFont fontWithName:_translatedTextView.font.fontName size:sender.value];
}
-(IBAction)changeReviewFontSizeButtonAction:(id)sender
{
    if (nil == _fontChangeSlider) {
        return;
    }
    
    _fontChangeSlider.hidden = !_fontChangeSlider.hidden;
    
    // 设置slider的当前值
    if (!_fontChangeSlider.hidden) {
        _slider.value = _textView.font.pointSize;
    }
}
+(NSURL*)getRecordFilePath:(NSString*)fileName
{
    NSURL *documentsDirectoryURL = [[NSFileManager defaultManager] URLForDirectory:NSDocumentDirectory inDomain:NSUserDomainMask appropriateForURL:nil create:NO error:nil];
    return [documentsDirectoryURL URLByAppendingPathComponent:[NSString stringWithFormat:@"%@.caf",fileName]];
}
+(NSURL*)getAudioFilePath:(NSString*)fileName
{
    NSURL *documentsDirectoryURL = [[NSFileManager defaultManager] URLForDirectory:NSDocumentDirectory inDomain:NSUserDomainMask appropriateForURL:nil create:NO error:nil];
    return [documentsDirectoryURL URLByAppendingPathComponent:[NSString stringWithFormat:@"%@.mp3",fileName]];
}

-(IBAction)speakWord:(id)sender
{
    if (!word || !word.length) {
        [self.view makeToast:NSLocalizedString(@"NO_SELECTED_WORD", @"")];
        return;
    }
    
    NSArray* prons = [WordManager getProns:wordDict];
    if (!prons || !prons.count) {
        return;
    }
    
    [[AFSoundManager sharedManager]changeVolumeToValue:1.0];
    
    // 文件存在了,直接播放
    NSURL* audioFileUrl = [WordModeController getAudioFilePath:word];
    if ([[NSFileManager defaultManager]fileExistsAtPath:[audioFileUrl path]]) {
        [[AFSoundManager sharedManager]startPlayingLocalFileWithFilePath:[audioFileUrl path] andBlock:^(int percentage, CGFloat elapsedTime, CGFloat timeRemaining, NSError *error, BOOL finished) {
            NSLog(@"%d%%",percentage);
            if (percentage >= 100) {
                self.progressView.hidden = YES;
                return;
            }
            self.progressView.hidden  = NO;
            self.progressView.progress = (CGFloat)percentage/100;
            
            if (!error) {
            } else {
                
                NSLog(@"There has been an error playing the remote file: %@", [error description]);
            }
            
        }];
        return;
    }
    
    // TODO 先下载音频文件，然后再播放
    //start
    NSURLSessionConfiguration *configuration = [NSURLSessionConfiguration defaultSessionConfiguration];
    AFURLSessionManager *manager = [[AFURLSessionManager alloc] initWithSessionConfiguration:configuration];
    
    NSURL *URL = [NSURL URLWithString:[prons objectAtIndex:0]];
    NSURLRequest *request = [NSURLRequest requestWithURL:URL];
    
    NSURLSessionDownloadTask *downloadTask = [manager downloadTaskWithRequest:request progress:nil destination:^NSURL *(NSURL *targetPath, NSURLResponse *response) {
        return [WordModeController getAudioFilePath:word];
    } completionHandler:^(NSURLResponse *response, NSURL *filePath, NSError *error) {
        NSLog(@"File downloaded to: %@", filePath);
        [self hideProgress];
        [[AFSoundManager sharedManager]startPlayingLocalFileWithFilePath:[filePath path] andBlock:^(int percentage, CGFloat elapsedTime, CGFloat timeRemaining, NSError *error, BOOL finished) {
            NSLog(@"%d%%",percentage);
            if (percentage >= 100) {
                self.progressView.hidden = YES;
                return;
            }
            self.progressView.hidden  = NO;
            self.progressView.progress = (CGFloat)percentage/100;
            
            if (!error) {
            } else {
                
                NSLog(@"There has been an error playing the remote file: %@", [error description]);
            }
            
        }];
    }];
    [downloadTask resume];
    [self showProgress];
}

-(IBAction)recordWord:(id)sender
{
    if (!word || !word.length) {
        [self.view makeToast:NSLocalizedString(@"NO_SELECTED_WORD", @"")];
        return;
    }
    
    // TODO 1.判断是否在录制中，如果在录制中，则停止并保存；否则开始录制
    NSString* recordedFileName = [[WordModeController getRecordFilePath:[NSString stringWithFormat:@"%@_recording",word]]path];
    AFSoundManager* sm = [AFSoundManager sharedManager];
    if ([sm isRecording]) {
        [self.recordButton setImage:[UIImage imageNamed:@"recording.png"] forState:UIControlStateNormal];
        NSLog(@"stopAndSaveRecording: %@",recordedFileName);
        [sm stopAndSaveRecording];
        return;
    }
    [self.recordButton setImage:[UIImage imageNamed:@"record.png"] forState:UIControlStateNormal];
    [sm startRecordingAudioWithFilepath:recordedFileName shouldStopAtSecond:0];
    NSLog(@"startRecordingAudioWithFileName: %@",recordedFileName);
}

-(IBAction)replayWord:(id)sender
{
    if (!word || !word.length) {
        [self.view makeToast:NSLocalizedString(@"NO_SELECTED_WORD", @"")];
        return;
    }
    
    NSString* recordedFileName = [[WordModeController getRecordFilePath:[NSString stringWithFormat:@"%@_recording",word]]path];
    if (![[NSFileManager defaultManager]fileExistsAtPath:recordedFileName]) {
        return;
    }
    
    AFSoundManager* sm = [AFSoundManager sharedManager];
    [sm startPlayingLocalFileWithFilePath:recordedFileName andBlock:^(int percentage, CGFloat elapsedTime, CGFloat timeRemaining, NSError *error, BOOL finished) {
        
        if (!error) {
        } else {
            NSLog(@"There has been an error playing the remote file: %@", [error description]);
        }
        
    }];
    NSLog(@"replay: %@",recordedFileName);
}
#pragma mark bottom part

-(IBAction)extendWordDetail:(id)sender
{
    NSMutableString* detail = [[NSMutableString alloc]init];
    [detail appendString:[WordManager getReadableMeaning:wordDict]];
    [detail appendString:@"\n"];
//    if (shortMeaning) {
//        shortMeaning = !shortMeaning;
//        _translatedTextView.text = detail;
//        return;
//    }
    
//    shortMeaning = !shortMeaning;
    NSArray* examples = [WordManager getExamples:wordDict];
    for (NSString* item in examples) {
        [detail appendString:item];
        if ([item hasSuffix:@"\n"]) {
            continue;
        }
        [detail appendString:@"\n"];
    }
    
    _translatedTextView.text = detail;
}


#pragma mark baidu ad delegate

- (NSString *)publisherId
{
    return  BAIDU_AD_APPID;
}

- (NSString*) appSpec
{
    //注意：该计费名为测试用途，不会产生计费，请测试广告展示无误以后，替换为您的应用计费名，然后提交AppStore.
    return BAIDU_AD_APPID;
}

-(BOOL) enableLocation
{
    //启用location会有一次alert提示
    return NO;
}

-(void) willDisplayAd:(BaiduMobAdView*) adview
{
    //在广告即将展示时，产生一个动画，把广告条加载到视图中
    sharedAdView.hidden = NO;
    CGRect f = sharedAdView.frame;
    CGFloat xOrg = f.origin.x;
    f.origin.x = -SCREEN_WIDTH;
    sharedAdView.frame = f;
    [UIView beginAnimations:nil context:nil];
    f.origin.x = xOrg;
    sharedAdView.frame = f;
    [UIView commitAnimations];
    NSLog(@"delegate: will display ad");
    
}

-(void) failedDisplayAd:(BaiduMobFailReason) reason;
{
    NSLog(@"delegate: failedDisplayAd %d", reason);
}

-(void) startAd
{
    //使用嵌入广告的方法实例。
    sharedAdView = [[BaiduMobAdView alloc] init];
    sharedAdView.AdUnitTag = BAIDU_AD_APPID;//@"myAdPlaceId1";
    //此处为广告位id，可以不进行设置，如需设置，在百度移动联盟上设置广告位id，然后将得到的id填写到此处。
    sharedAdView.AdType = BaiduMobAdViewTypeBanner;
    sharedAdView.frame = kAdViewPortraitRect;
    sharedAdView.delegate = self;
    [self.adViewContainer addSubview:sharedAdView];
    [sharedAdView start];
}


#pragma mark progress api
-(void) showProgress
{
    self.progressView.hidden = NO;
    self.progressView.progress = 0;
    
    // 显示进度条
    // 定时更新进度，循环更新
    [self fireNextUpdate];
}
-(void) hideProgress
{
    self.progressView.hidden = YES;
}

-(void)fireNextUpdate
{
    // fire next one
    if (self.progressView.hidden) {
        return;
    }
    
    [self performSelector:@selector(timeFire) withObject:nil afterDelay:0.1];
}

-(void)timeFire
{
    CGFloat progress = self.progressView.progress + 0.1;
    if (progress >= 1) {
        progress = 0;
    }
    NSLog(@"%f",progress);
    [self.progressView setProgress:progress animated:YES];
    
    [self fireNextUpdate];
}

@end
