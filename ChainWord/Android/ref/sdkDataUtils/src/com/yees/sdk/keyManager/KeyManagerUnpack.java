package com.yees.sdk.keyManager;

import java.io.File;
import java.io.FileInputStream;

//::这个是解密文件的读取类，对外开发
public class KeyManagerUnpack {
	private File _file;

	public static KeyManagerUnpack newInstance(File file) {
		// ::加密数据的管理
		KeyManagerUnpack manager = new KeyManagerUnpack(file);
		return manager;
	}

	private KeyManagerUnpack(File file) {
		_file = file;
	}

	// 对指定的文件进行解密，数据加载
	public byte[] unpack() {
		if(_file!=null)
		{
			try {
				//文件size
				FileInputStream inputStream = new FileInputStream(_file);
				//版本和预留区域
				int headerLength = KeyConstants.DATA_AREA_OFFSET;
				byte version[] =  new byte[headerLength];
				inputStream.read(version);
				
				byte[] data = new byte[(int)_file.length()-headerLength];
				inputStream.read(data);
				inputStream.close();
				return GZip.getGZipUncompress(data);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return new byte[0];
	}
}
