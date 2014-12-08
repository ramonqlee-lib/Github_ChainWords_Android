package com.yees.sdk.keyManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;


public class GZip {
	public static byte[] getGZipCompressed(byte[] byteData) {
		byte[] compressed = null;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream(
					byteData.length);
			Deflater compressor = new Deflater();
			compressor.setLevel(Deflater.BEST_COMPRESSION); // 灏嗗綋鍓嶅帇缂╃骇鍒缃负鎸囧畾鍊笺?
			compressor.setInput(byteData, 0, byteData.length);
			compressor.finish(); // 璋冪敤鏃讹紝鎸囩ず鍘嬬缉搴斿綋浠ヨ緭鍏ョ紦鍐插尯鐨勫綋鍓嶅唴瀹圭粨灏俱?

			// Compress the data
			final byte[] buf = new byte[1024];
			while (!compressor.finished()) {
				int count = compressor.deflate(buf);
				bos.write(buf, 0, count);
			}
			compressor.end(); // 鍏抽棴瑙ｅ帇缂╁櫒骞舵斁寮冩墍鏈夋湭澶勭悊鐨勮緭鍏ャ?
			compressed = bos.toByteArray();
			bos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return compressed;
	}
	
	public static byte[] getGZipCompressed(String data) {
		byte[] compressed = null;
		try {
			byte[] byteData = data.getBytes();
			ByteArrayOutputStream bos = new ByteArrayOutputStream(
					byteData.length);
			Deflater compressor = new Deflater();
			compressor.setLevel(Deflater.BEST_COMPRESSION); // 灏嗗綋鍓嶅帇缂╃骇鍒缃负鎸囧畾鍊笺?
			compressor.setInput(byteData, 0, byteData.length);
			compressor.finish(); // 璋冪敤鏃讹紝鎸囩ず鍘嬬缉搴斿綋浠ヨ緭鍏ョ紦鍐插尯鐨勫綋鍓嶅唴瀹圭粨灏俱?

			// Compress the data
			final byte[] buf = new byte[1024];
			while (!compressor.finished()) {
				int count = compressor.deflate(buf);
				bos.write(buf, 0, count);
			}
			compressor.end(); // 鍏抽棴瑙ｅ帇缂╁櫒骞舵斁寮冩墍鏈夋湭澶勭悊鐨勮緭鍏ャ?
			compressed = bos.toByteArray();
			bos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return compressed;
	}

	public static byte[] getGZipUncompress(byte[] data) throws IOException {
		byte[] unCompressed = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
		Inflater decompressor = new Inflater();
		try {
			decompressor.setInput(data);
			final byte[] buf = new byte[1024];
			while (!decompressor.finished()) {
				int count = decompressor.inflate(buf);
				bos.write(buf, 0, count);
			}

			unCompressed = bos.toByteArray();
			bos.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			decompressor.end();
		}
		// String test = bos.toString();
		return unCompressed;
	}
}
