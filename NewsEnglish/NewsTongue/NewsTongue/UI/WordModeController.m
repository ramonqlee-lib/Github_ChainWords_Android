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
    NSArray* prons;// 单词发音
}
@end

@implementation WordModeController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    fromLang = @"en";
    toLang = @"zh";
    
    if(nil != mBodyText && mBodyText.length > 0)
    {
        _textView.attributedText = [[NSAttributedString alloc]initWithString:mBodyText];
    }
    _textView.editable = NO;
    _textView.delegate = self;
    fontSize = kMinFontSize;
    _textView.font = [UIFont fontWithName:_textView.font.fontName size:fontSize];
    [_textView setLineSpacing:kLineSpacing clearPrevious:NO];
    
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
    if(tap.state == UIGestureRecognizerStateRecognized){
        
        CGPoint tappedPoint = [tap locationInView:_textView];
        
        UITextGranularity granularity = mGranuality;
        
        NSString* tappedString = [_textView substringWithGranularity:granularity atPoint:tappedPoint];
        if (nil == tappedString || tappedString.length == 0) {
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
    _translatedTextView.text = @"";
    //  翻译单词
    [[WordManager sharedInstance]query:[originText lowercaseString] from:fromLang to:toLang response:self];
    
}
#pragma mark WordQueryResult protocol

-(void)handleResponse:(NSDictionary*)result
{
    // 单词意义
    _translatedTextView.text = [WordManager getReadableMeaning:result];
    
    // 读音
    prons = [WordManager getProns:result];
    
    // 使用举例
    //    [WordManager getExamples:result];
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
    if (!prons || !prons.count) {
        return;
    }
    
    // 文件存在了,直接播放
    NSURL* audioFileUrl = [WordModeController getAudioFilePath:word];
    if ([[NSFileManager defaultManager]fileExistsAtPath:[audioFileUrl path]]) {
        [[AFSoundManager sharedManager]startPlayingLocalFileWithFilePath:[audioFileUrl path] andBlock:^(int percentage, CGFloat elapsedTime, CGFloat timeRemaining, NSError *error, BOOL finished) {
            
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
        [[AFSoundManager sharedManager]startPlayingLocalFileWithFilePath:[filePath path] andBlock:^(int percentage, CGFloat elapsedTime, CGFloat timeRemaining, NSError *error, BOOL finished) {
            
            if (!error) {
            } else {
                
                NSLog(@"There has been an error playing the remote file: %@", [error description]);
            }
            
        }];
    }];
    [downloadTask resume];
    
}

-(IBAction)recordWord:(id)sender
{
    // TODO 1.判断是否在录制中，如果在录制中，则停止并保存；否则开始录制
    NSString* recordedFileName = [[WordModeController getRecordFilePath:[NSString stringWithFormat:@"%@_recording",word]]path];
    AFSoundManager* sm = [AFSoundManager sharedManager];
    if ([sm isRecording]) {
        NSLog(@"stopAndSaveRecording: %@",recordedFileName);
        [sm stopAndSaveRecording];
        return;
    }
    [sm startRecordingAudioWithFilepath:recordedFileName shouldStopAtSecond:0];
    NSLog(@"startRecordingAudioWithFileName: %@",recordedFileName);
}

-(IBAction)replayWord:(id)sender
{
    if(!word)
    {
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
@end
