package com.yees.sdk.keyManager;

import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;


import android.content.Context;

import com.yees.sdk.utils.Utils;

public class Des {
	// json keys(转移到加密文件中，保存在assets目录下，文件名safepay_imagedata.dat)
//	private static final String ALGORITHM_DES = "DES/CBC/PKCS5Padding";
//	private static final String ALGORITHM= "DES";
	//todo: secret
//	public static final String DES_KEY = "ab345678";
//	public static final String STRING_IV = "12345678";
	
	public static final String getDesKey(Context context)
	{
		return Utils.getJsonValue(context, "3");
	}
	public static final String getDesIV(Context context)
	{
		return Utils.getJsonValue(context, "4");
	}
	public static final String getDesAlgorithm_Des(Context context)
	{
		return Utils.getJsonValue(context, "2");
	}
	public static final String getDesAlgorithm(Context context)
	{
		return Utils.getJsonValue(context, "1");
	}
	
	public static byte[] encode(String key, byte[] data, String ivKey,Context context) {
		try {
			DESKeySpec dks = new DESKeySpec(key.getBytes());
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(getDesAlgorithm(context));
			Key secretKey = keyFactory.generateSecret(dks);
			Cipher cipher = Cipher.getInstance(getDesAlgorithm_Des(context));
			IvParameterSpec iv = new IvParameterSpec(ivKey.getBytes());
			AlgorithmParameterSpec paramSpec = iv;
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, paramSpec);
			
			byte[] bytes = cipher.doFinal(data);
			return bytes;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static byte[] decode(String key, byte[] data, String ivKey,Context context) {
		try {
			DESKeySpec dks = new DESKeySpec(key.getBytes());
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(getDesAlgorithm(context));

			Key secretKey = keyFactory.generateSecret(dks);
			Cipher cipher = Cipher.getInstance(getDesAlgorithm_Des(context));
			IvParameterSpec iv = new IvParameterSpec(ivKey.getBytes());
			AlgorithmParameterSpec paramSpec = iv;
			cipher.init(Cipher.DECRYPT_MODE, secretKey, paramSpec);
			return cipher.doFinal(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}

