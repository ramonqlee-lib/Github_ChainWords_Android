package com.yees.sdk.keyManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Random;

//TODO::这个是制作加密文件的类，不能对外开放
public class KeyManagerPack {
	private File _file;
	private File _packedFile;

	public static KeyManagerPack newInstance(File file, File packedFile) {
		// TODO::加密数据的管理
		KeyManagerPack manager = new KeyManagerPack(file, packedFile);
		return manager;
	}

	private KeyManagerPack(File file, File packedFile) {
		_file = file;
		_packedFile = packedFile;
	}

	private static byte[] readFile(File file) {
		if (file != null) {
			try {
				// 文件size
				FileInputStream inputStream = new FileInputStream(file);
				byte[] data = new byte[(int) file.length()];
				inputStream.read(data);
				inputStream.close();
				return data;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return new byte[0];
	}

	// 将输入字符串加密后，存在在指定的文件中
	public void pack() {
		byte[] srcData = readFile(_file);
		// 从文件中读取待加密的字符串

		if (srcData == null || srcData.length == 0) {
			return;
		}

		// TODO::写入版本，文件密钥和des加密后的数据
		if (_file != null) {
			try {
				if(_packedFile.exists())
				{
					_packedFile.delete();
				}
				FileOutputStream outputStream = new FileOutputStream(_packedFile);
				outputStream.write(KeyConstants.VERSION);

				// placeholder,后续根据版本，再次存储不同的预留数据
				byte[] randoms = new byte[KeyConstants.DATA_AREA_OFFSET - 1];// 1代表version已经占用的字节数
				Random r = new Random();
				r.nextBytes(randoms);
				outputStream.write(randoms);

				// 写入压缩后的数据
				byte[]zipped=GZip.getGZipCompressed(srcData);
				outputStream.write(zipped);
				outputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	//利用此程序，制作加密后的键值对
	public static void main(String[] args) {
		// 加密
		File baseFile = new File("");
		String baseDir = baseFile.getAbsolutePath();
		File unpackfile = new File(baseDir+"/data/unpacked.txt");// 待加密数据
		File packfile = new File(baseDir+"/data/yees_config.dat");// 加密后数据
		KeyManagerPack packer = KeyManagerPack
				.newInstance(unpackfile, packfile);
		packer.pack();

		// 解密
		KeyManagerUnpack unpacker = KeyManagerUnpack.newInstance(packfile);
		byte[] unpackeddata = unpacker.unpack();
		if(unpackeddata!=null&&unpackeddata.length!=0)
		{
			System.out.println(new String(unpackeddata));
		}
	}
}
