package com.dream2reality.utils;

import android.app.Activity;
import android.media.AudioManager;
import android.text.TextUtils;

import com.baidu.speechsynthesizer.SpeechSynthesizer;

/**
 * 语音播报
 * 
 * @author ramonqlee
 * 
 */
public class TTSPlayer {
	private static final TTSPlayer sTTSPlayer = new TTSPlayer();
	private boolean initialized;

	private SpeechSynthesizer speechSynthesizer;

	public static TTSPlayer sharedInstance(Activity activity) {
		sTTSPlayer.init(activity);
		return sTTSPlayer;
	}

	public void speak(final String text) {
		if (TextUtils.isEmpty(text)) {
			return;
		}
		new Thread(new Runnable() {

			@Override
			public void run() {
				setParams();
				// TODO Text to speech
				int ret = speechSynthesizer.speak(text);
				if (ret != 0) {
					// logError("开始合成器失败：" + errorCodeAndDescription(ret));
				}
			}
		}).start();
	}

	private void init(Activity activity) {
		if (initialized || null == activity) {
			return;
		}
		// 初始化
		initialized = true;
		speechSynthesizer = new SpeechSynthesizer(activity,
				activity.getPackageName(), null);
		// 此处需要将setApiKey方法的两个参数替换为你在百度开发者中心注册应用所得到的apiKey和secretKey
		speechSynthesizer.setApiKey("RqUEl4HtPM7NMbPdNtLdxGHK",
				"Tt4mtlTXYp0uMUdjKRYZnKbUVvX4G0Zt");
		speechSynthesizer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
	}

	private void setParams() {
		speechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0");
		speechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME, "5");
		speechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, "6");
		speechSynthesizer.setParam(SpeechSynthesizer.PARAM_PITCH, "4");
		speechSynthesizer.setParam(SpeechSynthesizer.PARAM_AUDIO_ENCODE,
				SpeechSynthesizer.AUDIO_ENCODE_AMR);
		speechSynthesizer.setParam(SpeechSynthesizer.PARAM_AUDIO_RATE,
				SpeechSynthesizer.AUDIO_BITRATE_AMR_15K85);
		// speechSynthesizer.setParam(SpeechSynthesizer.PARAM_LANGUAGE,
		// SpeechSynthesizer.LANGUAGE_ZH);
//		 speechSynthesizer.setParam(SpeechSynthesizer.PARAM_NUM_PRON, "0");
//		 speechSynthesizer.setParam(SpeechSynthesizer.PARAM_ENG_PRON, "0");
//		 speechSynthesizer.setParam(SpeechSynthesizer.PARAM_PUNC, "1");
		// speechSynthesizer.setParam(SpeechSynthesizer.PARAM_BACKGROUND, "0");
//		 speechSynthesizer.setParam(SpeechSynthesizer.PARAM_STYLE, "0");
		// speechSynthesizer.setParam(SpeechSynthesizer.PARAM_TERRITORY, "0");
	}

}
