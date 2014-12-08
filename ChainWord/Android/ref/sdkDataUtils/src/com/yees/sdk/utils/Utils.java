package com.yees.sdk.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.http.conn.util.InetAddressUtils;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.yees.sdk.keyManager.Des;
import com.yees.sdk.keyManager.GZip;
import com.yees.sdk.keyManager.KeyManagerUnpack;
import com.yees.sdk.netmodel.NetTaskConst;

public class Utils {
	private static final char SEPRATOR = '-';

	private final static String LIB_SUFFIX = ".out";// bug:android system.load
	// seems to need this,no
	// suffix will fail for
	// loading once again
	private static boolean assetsExtracted;// 资源文件是否已经释放过
	// 辅助小工具修改联网使用的参数
	public static final String OSName = "Android";
	private static final String JSON_DATA_FILE_NAME = "yees_config.dat";// 加密后的json键值对
	private static JSONObject sUnpackedJsonObject;
	private static final int ANDROID_API_LEVLE_16 = 16;

	// 读取加密文件中的字符串
	public static String getJsonValue(Context context, String key) {
		if (context == null || TextUtils.isEmpty(key)) {
			return "";
		}
		try {
			if (sUnpackedJsonObject == null) {
				// ::assets文件拷贝到缓存目录下，解析完毕后删除(参考文件释放部分)
				String dir = context.getFilesDir().getAbsolutePath();
				extractAssetsFile(context, dir, JSON_DATA_FILE_NAME, true);
				File file = new File(dir, JSON_DATA_FILE_NAME);
				KeyManagerUnpack unpacker = KeyManagerUnpack.newInstance(file);
				byte[] unpackeddata = unpacker.unpack();
				file.delete();// clean this file

				// before android2.3,there is a bug for json
				// sUnpackedJsonObject = new JSONObject(new
				// String(unpackeddata));
				final int PREFIX_BYTES_UTF8 = 3;
				sUnpackedJsonObject = new JSONObject(new String(unpackeddata,
						PREFIX_BYTES_UTF8, unpackeddata.length
								- PREFIX_BYTES_UTF8));
			}
			if (sUnpackedJsonObject != null && sUnpackedJsonObject.has(key)) {
				return sUnpackedJsonObject.getString(key);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 获取当前cpu信息，用于指令集的部分
	 * 
	 * @param context
	 * @return 缺省返回armeabi
	 */
	public static String getCPU(Context context) {
		String cpuForAssets = Build.CPU_ABI;
		int index = Build.CPU_ABI.indexOf(SEPRATOR);
		if (-1 != index) {
			cpuForAssets = Build.CPU_ABI.substring(0, index);
		}
		return cpuForAssets;
	}

	/**
	 * sdk是否支持当前cpu体系
	 * 
	 * @return sdk是否支持当前cpu
	 */
	public static boolean sdkCanRun(Context context) {
		try {
			AssetManager assetManager = context.getAssets();
			// 查看assets下是否有相应的库支持
			return assetManager.list(getCPU(context)).length > 0;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 释放assets平台相关文件到指定目录下。将根据当前cpu，释放所有文件到指定目录下，包括子目录中的文件
	 * 
	 * @param context
	 * @param dir
	 * @param overwrite
	 *            是否覆盖
	 * @param accessMode
	 *            文件权限
	 */
	public static void extractAssetSubDirFiles(Context context, String dir,
			boolean overwrite, int accessMode) {
		if (null == context || assetsExtracted) {
			return;
		}

		String cpuForAssets = Utils.getCPU(context);
		try {
			AssetManager assetManager = context.getAssets();

			// 释放assets cpu相关目录下的文件
			String cpuFiles[] = assetManager.list(cpuForAssets);
			for (String fileName : cpuFiles) {
				String assetFileFullName = cpuForAssets + File.separatorChar
						+ fileName;

				releaseLib(context, assetFileFullName, true);
			}
			assetsExtracted = true;// 已经释放过了
		} catch (Exception e) {
			if (Constants.isLocalLogEnabled()) {
				Log.e(Constants.LOG_TAG, e.getMessage());
			}
		}
	}

	private static void changeFilePermission(String fileName, int accessMode) {
		try {
			Runtime.getRuntime().exec(
					String.format("chmod %d %s", accessMode, fileName));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 释放文件到指定目录下
	 * 
	 * @param context
	 * @param dir
	 * @param fileName
	 * @param accessMode
	 * @param overwrite
	 */
	private static String releaseFile(Context context, String dir,
			final String fileName, boolean overwrite) {
		// destination file
		String shortFileName = fileName;
		int end = shortFileName.lastIndexOf(File.separatorChar);
		if (-1 != end) {
			shortFileName = fileName.substring(end + 1);
		}
		// change file permission
		String destFileName = dir + File.separatorChar + shortFileName;
		extractAssetsFile(context, dir, fileName, overwrite);
		return destFileName;
	}

	/**
	 * 释放lib
	 * 
	 * @param fileName
	 * @param load
	 */
	private static void releaseLib(Context context, final String fileName,
			boolean load) {
		final int RWX_RWX_NULL = 0x770;
		if (null == context) {
			return;
		}
		// 释放到lib目录下，并根据load标示确定是否加载
		String dir = context.getFilesDir().getAbsolutePath();// lib文件目录
		String destFileName = releaseFile(context, dir, fileName, true);
		if (TextUtils.isEmpty(destFileName)) {
			return;
		}
		String libName = fileName;
		// make sure file name end with .so
		if (!destFileName.endsWith(LIB_SUFFIX)) {
			File file = new File(destFileName);
			libName = destFileName + LIB_SUFFIX;
			file.renameTo(new File(libName));
		}
		changeFilePermission(destFileName, RWX_RWX_NULL);

		// load lib
		if (load) {
			System.load(libName);
		}
	}

	/**
	 * 云端数据传输解密函数
	 * 
	 * @param data
	 * @return
	 */
	public static byte[] decrypt(byte[] data, Context context,
			boolean base64Decode) {
		try {
			// 先进行base64解码
			if (base64Decode) {
				data = Base64.decode(data, Base64.DEFAULT);
			}
			return GZip.getGZipUncompress(Des.decode(Des.getDesKey(context),
					data, Des.getDesIV(context), context));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new byte[0];
	}

	public static byte[] decrypt(byte[] data, Context context) {
		try {
			return GZip.getGZipUncompress(Des.decode(Des.getDesKey(context),
					data, Des.getDesIV(context), context));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new byte[0];
	}

	/**
	 * 云端数据传输加密函数
	 * 
	 * @param data
	 * @return
	 */
	public static byte[] encrypt(String data, Context context) {
		// 先压缩，后加密
		return Des.encode(Des.getDesKey(context), GZip.getGZipCompressed(data),
				Des.getDesIV(context), context);
	}

	/**
	 * 释放assets目录下的文件到指定目录
	 * 
	 * @param context
	 * @param dir
	 *            释放目录
	 * @param fileName
	 *            释放文件名
	 * @param overwrite
	 *            当文件已经存在时，是否覆盖
	 * @return 释放成功，返回true；否则返回false
	 */
	public static boolean extractAssetsFile(Context context, String dir,
			String fileName, boolean overwrite) {
		if (null == context) {
			return false;
		}
		try {
			AssetManager assetManager = context.getResources().getAssets();
			InputStream in = assetManager.open(fileName);
			if (null == in) {
				return false;
			}

			// destination file
			int end = fileName.lastIndexOf(File.separatorChar);
			if (-1 != end) {
				fileName = fileName.substring(end + 1);
			}
			String destFileName = dir + File.separatorChar + fileName;
			File destFile = new File(destFileName);
			if (destFile.exists()) {
				if (!overwrite) {
					return true;
				}
				if (destFile.delete()) {
					if (Constants.isLocalLogEnabled()) {
						Logger.d(Constants.LOG_TAG, String.format(
								"asset file %s deleted", fileName));
					}
				}
			}
			// 修复如果包含目录的bug
			int index = fileName.indexOf(File.separatorChar);
			if (-1 != index) {
				String parentDir = dir + File.separatorChar
						+ fileName.substring(0, index);
				new File(parentDir).mkdirs();
			}
			FileOutputStream outputStream = new FileOutputStream(destFile,
					false);
			byte[] buf = new byte[1024];
			long bufferTotalSize = 0;
			int bytesRead;
			while ((bytesRead = in.read(buf)) > 0) {
				outputStream.write(buf, 0, bytesRead);
				bufferTotalSize += bytesRead;
			}
			in.close();
			outputStream.close();

			// 校验下确实释放成功了
			if (bufferTotalSize != destFile.length()) {
				if (Constants.isLocalLogEnabled()) {
					Log.e(Constants.LOG_TAG, String.format(
							"asset file %s size changed", fileName));
				}
			}

			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * 读取properties文件
	 * 
	 * @param context
	 * @param fileName
	 * @param key
	 * @return
	 */
	public static String loadProperties(Context context, String fileName,
			String key) {
		Properties properties = new Properties();
		InputStream is = null;
		String sdkVersion = "";
		try {
			is = context.getAssets().open(fileName);
			properties.load(is);
			sdkVersion = properties.getProperty(key);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sdkVersion;
	}

	/**
	 * 
	 * @param zipFile
	 * @param unzipChild
	 *            待解压的文件
	 * @param location
	 */
	public static void unzip(String zipFile, String unzipChild, String location) {
		int size;
		final int BUFFER_SIZE = 1024;
		byte[] buffer = new byte[BUFFER_SIZE];

		try {
			if (!location.endsWith("/")) {
				location += "/";
			}
			File f = new File(location);
			if (!f.isDirectory()) {
				f.mkdirs();
			}
			ZipInputStream zin = new ZipInputStream(new BufferedInputStream(
					new FileInputStream(zipFile), BUFFER_SIZE));
			try {
				ZipEntry ze = null;
				while ((ze = zin.getNextEntry()) != null) {
					if (!TextUtils.isEmpty(unzipChild)) {// filter child for
															// unzipping
						if (!unzipChild.equalsIgnoreCase(ze.getName())) {
							continue;
						}
					}

					String path = location + ze.getName();
					File unzipFile = new File(path);

					if (ze.isDirectory()) {
						if (!unzipFile.isDirectory()) {
							unzipFile.mkdirs();
						}
					} else {
						// check for and create parent directories if they don't
						// exist
						File parentDir = unzipFile.getParentFile();
						if (null != parentDir) {
							if (!parentDir.isDirectory()) {
								parentDir.mkdirs();
							}
						}

						// unzip the file
						FileOutputStream out = new FileOutputStream(unzipFile,
								false);
						BufferedOutputStream fout = new BufferedOutputStream(
								out, BUFFER_SIZE);
						try {
							while ((size = zin.read(buffer, 0, BUFFER_SIZE)) != -1) {
								fout.write(buffer, 0, size);
							}

							zin.closeEntry();
						} finally {
							fout.flush();
							fout.close();
						}
					}
				}
			} finally {
				zin.close();
			}
		} catch (Exception e) {
			if (Constants.isLocalLogEnabled()) {
				Log.e(Constants.LOG_TAG, "Unzip exception", e);
			}
		}
	}

	/**
	 * Unzip a zip file. Will overwrite existing files.
	 * 
	 * @param zipFile
	 *            Full path of the zip file you'd like to unzip.
	 * @param location
	 *            Full path of the directory you'd like to unzip to (will be
	 *            created if it doesn't exist).
	 * @throws IOException
	 */
	public static void unzip(String zipFile, String location)
			throws IOException {
		unzip(zipFile, "", location);
	}

	public static boolean fileExists(String fullPathName) {
		if (TextUtils.isEmpty(fullPathName)) {
			return false;
		}
		File file = new File(fullPathName);
		return file.exists();
	}

	public static void deleteFile(String fullPathName) {
		if (TextUtils.isEmpty(fullPathName)) {
			return;
		}
		File file = new File(fullPathName);
		if (file.exists()) {
			file.delete();
		}
	}

	// 下面这个函数用于将字节数组换成成16进制的字符串
	public static String byteArrayToHex(byte[] b) {
		String hs = "";
		String stmp = "";
		for (int n = 0; n < b.length; n++) {
			stmp = (Integer.toHexString(b[n] & 0XFF));
			if (stmp.length() == 1) {
				hs = hs + "0" + stmp;
			} else {
				hs = hs + stmp;
			}
			if (n < b.length - 1) {
				hs = hs + "";
			}
		}
		return hs;
	}

	/**
	 * 返回文件的md5
	 * 
	 * @param inputFile
	 * @return 文件不存在或者出现异常时，会返回""
	 * @throws IOException
	 */
	public static String fileMD5(String inputFile) throws IOException {
		if (!fileExists(inputFile)) {
			return "";
		}
		// 缓冲区大小（这个可以抽出一个参数）
		int bufferSize = 256 * 1024;
		FileInputStream fileInputStream = null;
		DigestInputStream digestInputStream = null;
		try {
			// 拿到一个MD5转换器（同样，这里可以换成SHA1）
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			// 使用DigestInputStream
			fileInputStream = new FileInputStream(inputFile);
			digestInputStream = new DigestInputStream(fileInputStream,
					messageDigest);
			// read的过程中进行MD5处理，直到读完文件
			byte[] buffer = new byte[bufferSize];
			while (digestInputStream.read(buffer) > 0)
				;
			// 获取最终的MessageDigest
			messageDigest = digestInputStream.getMessageDigest();
			// 拿到结果，也是字节数组，包含16个元素
			byte[] resultByteArray = messageDigest.digest();
			// 同样，把字节数组转换成字符串
			return byteArrayToHex(resultByteArray);
		} catch (NoSuchAlgorithmException e) {
			return "";
		} finally {
			try {
				digestInputStream.close();
			} catch (Exception e) {
			}
			try {
				fileInputStream.close();
			} catch (Exception e) {
			}
		}
	}

	// 得到手机的IMEI号，需要context参数,获取不到时，返回“000000000000000”
	public static String getIMEI(Context context) {
		String r = "";
		try {
			if (context != null) {
				TelephonyManager mTelephonyMgr = (TelephonyManager) context
						.getSystemService(Context.TELEPHONY_SERVICE);
				r = mTelephonyMgr.getDeviceId();
			}
		} catch (Exception e) {
			// : handle exception
		}

		return TextUtils.isEmpty(r) ? "000000000000000" : r;
	}

	// 得到手机的IMSI号，需要context参数,获取不到时，返回“000000000000000”
	public static String getIMSI(Context context) {
		String r = "";
		try {
			if (context != null) {
				TelephonyManager mTelephonyMgr = (TelephonyManager) context
						.getSystemService(Context.TELEPHONY_SERVICE);
				r = mTelephonyMgr.getSubscriberId();
			}
		} catch (Exception e) {
			// : handle exception
		}
		return TextUtils.isEmpty(r) ? "000000000000000" : r;
	}

	/**
	 * 得到androidID
	 * 
	 * @param context
	 * @return the corresponding value, or "" if not present
	 */
	public static String getAndroidId(Context context) {
		String r = "";
		if (context != null) {
			r = Settings.Secure.getString(context.getContentResolver(),
					Settings.Secure.ANDROID_ID);
		}

		return TextUtils.isEmpty(r) ? "" : r;
	}

	/**
	 * 是否在模拟器上运行
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isEmulator(Context context) {
		String android_id = getAndroidId(context);
		boolean emulator = TextUtils.isEmpty(android_id)
				|| "google_sdk".equals(Build.PRODUCT)
				|| "sdk".equals(Build.PRODUCT);
		return emulator;
	}

	/**
	 * 获取本地mac地址
	 * 
	 * @param context
	 * @return
	 */
	public static String getLocalWiFiMac(Context context) {
		String r = "";
		if (context != null) {
			WifiManager wifi = (WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);
			if (wifi != null) {
				WifiInfo info = wifi.getConnectionInfo();
				if (info != null) {
					r = info.getMacAddress();
				}
			}
		}
		if (TextUtils.isEmpty(r)) {
			r = "";
		}
		return r;
	}

	/**
	 * 机型.获取不到时，返回"Emulator?"
	 * 
	 * @return
	 */
	public static String getModel() {
		return TextUtils.isEmpty(Build.MODEL) ? "Emulator?" : Build.MODEL;
	}

	/**
	 * 从Assets中读取图片
	 */
	public static Bitmap getImageFromAssetsFile(Context context, String fileName) {
		if (context == null || TextUtils.isEmpty(fileName)) {
			return null;
		}
		Bitmap image = null;
		AssetManager am = context.getResources().getAssets();
		try {
			InputStream is = am.open(fileName);
			image = BitmapFactory.decodeStream(is);
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return image;
	}

	/**
	 * 以最省内存的方式读取本地资源的图片
	 * 
	 * @param context
	 * @param resId
	 * @return
	 */
	public static Bitmap readResourceBitmap(Context context, int resId) {
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inPreferredConfig = Bitmap.Config.RGB_565;
		opt.inPurgeable = true;
		opt.inInputShareable = true;
		// 获取资源图片
		InputStream is = context.getResources().openRawResource(resId);
		return BitmapFactory.decodeStream(is, null, opt);
	}

	public static int dp2px(Context context, float dp) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dp * scale + 0.5f);
	}

	// 返回sdk的版本
	public static String getVersion(Context context) {
		if (null == context) {
			return Constants.VERSOIN_STRING;
		}
		return Constants.VERSOIN_STRING
				+ "."
				+ Utils.loadProperties(context, Constants.REVISION_FILE,
						Constants.REVISION_KEY);
	}

	public static boolean packageExist(Context context, String packageName) {
		if (context == null) {
			return false;
		}
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
			return (packageInfo != null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 
	 * @param context
	 * @param packageName
	 * @return 可能返回""
	 */
	public static String getPackageMd5(Context context, String packageName) {
		if (context == null) {
			return "";
		}
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
			return signatureMD5(packageInfo.signatures);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static String signatureMD5(Signature[] signatures) {
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			if (signatures != null) {
				for (Signature s : signatures)
					digest.update(s.toByteArray());
			}
			return byteArrayToHex(digest.digest());
		} catch (Exception e) {
			return "";
		}
	}

	public static String getMD5(byte[] data) {
		if (null == data || 0 == data.length) {
			return "";
		}
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(data);
			return byteArrayToHex(digest.digest());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	// try to find a valid package name
	// return: 如果装换成功，则返回包名；否则返回""
	public static String getPackageNameFromDirectory(String dir) {
		final String kDefaultRetString = "";
		if (TextUtils.isEmpty(dir)) {
			return kDefaultRetString;
		}

		String[] headKeyWords = { "/data/data/", "/data@data@", "]" };
		String[] tailKeyWordsStrings = { "/", "@", "]" };

		// find occurrences of keywords(only once)
		int startPos = -1;
		for (int i = 0; i < headKeyWords.length; ++i) {
			String headItem = headKeyWords[i];
			startPos = dir.indexOf(headItem);
			if (startPos == -1) {
				continue;
			}

			// find the corresponding end
			String tailItem = tailKeyWordsStrings[i];
			int s = startPos + headItem.length();
			int endPos = dir.indexOf(tailItem, s);
			if (endPos == -1) {
				return dir.length() <= s ? "" : dir.substring(s);
			}
			return dir.substring(s, endPos);
		}
		return kDefaultRetString;
	}

	public static String getLocalIpAddress() {
		try {
			String ipv4;
			List<NetworkInterface> nilist = Collections.list(NetworkInterface
					.getNetworkInterfaces());
			for (NetworkInterface ni : nilist) {
				List<InetAddress> ialist = Collections.list(ni
						.getInetAddresses());
				for (InetAddress address : ialist) {
					if (!address.isLoopbackAddress()
							&& InetAddressUtils.isIPv4Address(ipv4 = address
									.getHostAddress())) {
						return ipv4;
					}
				}
			}
		} catch (SocketException ex) {
			// Log.e(LOG_TAG, ex.toString());
		}
		return null;
	}

	/**
	 * 此方法暂时不可靠
	 * 
	 * @param context
	 * @return
	 */
	@SuppressLint("NewApi")
	public static String getLocalMacAddressFromIP(Context context) {
		String mac_s = "";
		try {
			byte[] mac;
			NetworkInterface ne = NetworkInterface.getByInetAddress(InetAddress
					.getByName(getLocalIpAddress()));
			mac = ne.getHardwareAddress();
			if (null == mac || 0 == mac.length) {
				String wifiMac = getLocalWiFiMac(context);
				if (Constants.isLocalLogEnabled()) {
					Log.e(Constants.LOG_TAG,
							"fail to get mac from ip, get mac from wifi "
									+ wifiMac);
				}
				return wifiMac;
			}
			mac_s = byte2hex(mac);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mac_s;
	}

	public static String byte2hex(byte[] b) {
		StringBuffer hs = new StringBuffer(b.length);
		String stmp = "";
		int len = b.length;
		for (int n = 0; n < len; n++) {
			stmp = Integer.toHexString(b[n] & 0xFF);
			if (stmp.length() == 1) {
				hs = hs.append("0").append(stmp);
			} else {
				hs = hs.append(stmp);
			}
		}
		return String.valueOf(hs);
	}

	private static final int MOBILE_DIGIT_COUNT = 11;

	public static boolean legalMobile(String mobile) {
		return (!TextUtils.isEmpty(mobile) && TextUtils.isDigitsOnly(mobile) && mobile
				.length() == MOBILE_DIGIT_COUNT);
	}

	@SuppressLint("NewApi")
	public static void setBackground(View containerView, int viewId, int resId) {
		if (null == containerView) {
			return;
		}
		View view = containerView.findViewById(viewId);
		if (null != view) {
			Bitmap bitmap = Utils.readResourceBitmap(
					containerView.getContext(), resId);
			BitmapDrawable bmDrawable = new BitmapDrawable(containerView
					.getContext().getResources(), bitmap);
			if (Build.VERSION.SDK_INT >= ANDROID_API_LEVLE_16) {
				view.setBackground(bmDrawable);
			} else {
				view.setBackgroundDrawable(bmDrawable);
			}
		}
	}

	@SuppressLint("NewApi")
	public static void setBackground(View view, int resId) {
		if (null == view) {
			return;
		}
		Bitmap bitmap = Utils.readResourceBitmap(view.getContext(), resId);
		BitmapDrawable bmDrawable = new BitmapDrawable(view.getContext()
				.getResources(), bitmap);
		if (Build.VERSION.SDK_INT >= ANDROID_API_LEVLE_16) {
			view.setBackground(bmDrawable);
		} else {
			view.setBackgroundDrawable(bmDrawable);
		}
	}

	@SuppressLint("NewApi")
	public static void releaseBackground(View view) {
		unbindDrawables(view);
	}

	/**
	 * 解除view和drawable的绑定关系
	 * 
	 * @param view
	 */
	public static void unbindDrawables(View view) {
		if (null == view) {
			return;
		}
		try {
			Drawable drawable = view.getBackground();
			if (drawable != null) {
				drawable.setCallback(null);
			}

			// recycle bitmap if possible
			if (drawable instanceof BitmapDrawable) {
				BitmapDrawable bmDrawable = ((BitmapDrawable) drawable);
				Bitmap bm = bmDrawable.getBitmap();
				if (null != bm && !bm.isRecycled()) {
					bm.recycle();
				}
			}
			view.setBackgroundResource(0);// remove the background

			if (view instanceof ViewGroup) {
				for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
					unbindDrawables(((ViewGroup) view).getChildAt(i));
				}
				((ViewGroup) view).removeAllViews();

				if (Constants.isLocalLogEnabled()) {
					Logger.d(Constants.LOG_TAG, "removeAllViews ");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 百度定位结果是否有效
	 * 
	 * @param errorCode
	 * @return
	 */
	public static boolean isBaiduLocationSuccessful(int errorCode) {
		// 参考
		// http://developer.baidu.com/map/index.php?title=android-locsdk/guide/v4-2#.E5.8F.91.E8.B5.B7.E5.AE.9A.E4.BD.8D.E8.AF.B7.E6.B1.82
		/*
		 * 61 ： GPS定位结果
		 * 
		 * 62 ： 扫描整合定位依据失败。此时定位结果无效。
		 * 
		 * 63 ： 网络异常，没有成功向服务器发起请求。此时定位结果无效。
		 * 
		 * 65 ： 定位缓存的结果。
		 * 
		 * 66 ： 离线定位结果。通过requestOfflineLocaiton调用时对应的返回结果
		 * 
		 * 67 ： 离线定位失败。通过requestOfflineLocaiton调用时对应的返回结果
		 * 
		 * 68 ： 网络连接失败时，查找本地离线定位时对应的返回结果
		 * 
		 * 161： 表示网络定位结果
		 * 
		 * 162~167： 服务端定位失败
		 * 
		 * 502：key参数错误
		 * 
		 * 505：key不存在或者非法
		 * 
		 * 601：key服务被开发者自己禁用
		 * 
		 * 602：key mcode不匹配
		 * 
		 * 501～700：key验证失败
		 */

		int OK_CODES[] = { 61, 161 };// 成功状态码

		for (int i = 0; i < OK_CODES.length; i++) {
			if (errorCode == OK_CODES[i]) {
				return true;
			}
		}
		return false;
	}

	public static void setLatestLat(Context context, String lat) {
		if (null == context || TextUtils.isEmpty(lat)) {
			return;
		}
		Config.sharedInstance(context).putString(
				NetTaskConst.LATEST_LOCATION_LAT_KEY, lat);
	}

	public static void setLatestLng(Context context, String lng) {
		if (null == context || TextUtils.isEmpty(lng)) {
			return;
		}
		Config.sharedInstance(context).putString(
				NetTaskConst.LATEST_LOCATION_LNG_KEY, lng);
	}

	/**
	 * 获取后台定位，最后一次定位的结果
	 * 
	 * @return
	 */
	public static String getLastestLat(Context context) {
		if (null == context) {
			return "";
		}
		return Config.sharedInstance(context).getString(
				NetTaskConst.LATEST_LOCATION_LAT_KEY);
	}

	public static String getLastestLng(Context context) {
		if (null == context) {
			return "";
		}
		return Config.sharedInstance(context).getString(
				NetTaskConst.LATEST_LOCATION_LNG_KEY);
	}

	/**
	 * 获取版本号
	 * 
	 * @param context
	 * @return
	 */
	public static String getAppVersion(Context context) {// 获取版本号
		if (null == context) {
			return "";
		}
		try {
			PackageInfo pi = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
			return pi.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 *   判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的     
	 * 
	 * @param context
	 * @return true 表示开启      
	 */
	public static boolean isGPSOpen(final Context context) {
		LocationManager locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		// 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快） 
		boolean gps = locationManager
				.isProviderEnabled(LocationManager.GPS_PROVIDER);
		// 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位） 
		boolean network = locationManager
				.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		return (gps | network);
	}

	public static boolean copyFile(String srcFileName, String destFileName,
			boolean overwrite) throws IOException {
		File srcFile = new File(srcFileName);
		File destFile = new File(destFileName);
		if (!srcFile.exists()) {
			return false;
		}
		if (!srcFile.isFile()) {
			return false;
		}
		if (!srcFile.canRead()) {
			return false;
		}
		if (destFile.exists() && overwrite) {
			destFile.delete();
		}

		try {
			InputStream inStream = new FileInputStream(srcFile);
			FileOutputStream outStream = new FileOutputStream(destFile);
			byte[] buf = new byte[1024];
			int byteRead = 0;
			while ((byteRead = inStream.read(buf)) != -1) {
				outStream.write(buf, 0, byteRead);
			}
			outStream.flush();
			outStream.close();
			inStream.close();
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}
}
