package com.yees.sdk.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

public class FileUtils {
	
	private static final int BUFF_SIZE = 1024 * 1024; // 1M Byte
	
	/**
	 * 批量压缩文件（夹）
	 * 
	 * @param resFile
	 *            要压缩的文件（夹）路径
	 * @param zipDir
	 *            生成的压缩文件文件夹路径
	 * @param fileName
	 *            生成的压缩文件的文件名
	 * @throws IOException
	 *             当压缩过程出错时抛出
	 */
	public static Boolean zipFiles(String resFilePath, String zipFilePath) {
		try {

			File resFile = new File(resFilePath);
			String tempZipFilePath = zipFilePath + ".temp";
			File tempZipFile = new File(tempZipFilePath);
			File saveZipDir = new File(tempZipFile.getParent());
			if (!saveZipDir.exists()) {
				boolean mkResult = saveZipDir.mkdirs();
				if (!mkResult) {
					return false;
				}
			}
			ZipOutputStream zipout = new ZipOutputStream(
					new BufferedOutputStream(new FileOutputStream(tempZipFile),
							BUFF_SIZE));
			Log.i("zip_result",
					"resFile.getAbsolutePath() = " + resFile.getAbsolutePath());
			zipFile(resFile, zipout, "");
			Log.i("zip_result", "zipFiles finish ");
			tempZipFile.renameTo(new File(zipFilePath));
			zipout.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 解压缩一个文件
	 * 
	 * @param zipFile
	 *            压缩文件
	 * @param folderPath
	 *            解压缩的目标目录
	 * @return
	 * @throws IOException
	 *             当解压缩过程出错时抛出
	 */
	public static boolean unZipFile(String zipFileName, String folderPath) {
		System.out.println("unZipFile start");
		byte[] buf = new byte[512];
		int readedBytes;
		try {
			FileOutputStream fileOut;
			File folder = new File(folderPath);
			if (!folder.exists()) {
				boolean mkResult = folder.mkdirs();
				if (!mkResult) {
					return false;
				}
			}
			InputStream inputStream;
			ZipFile zipFile = new ZipFile(zipFileName);

			Log.i("zip_result", "unZipfile == " + zipFileName);
			for (Enumeration entries = zipFile.entries(); entries
					.hasMoreElements();) {
				ZipEntry entry = (ZipEntry) entries.nextElement();
				File file = new File(folderPath + File.separator
						+ entry.getName());
				if (entry.isDirectory()) {
					file.mkdirs();
				} else {
					// 如果指定文件的目录不存在,则创建之
					File parent = file.getParentFile();
					if (!parent.exists()) {
						parent.mkdirs();
					}
					inputStream = zipFile.getInputStream(entry);
					fileOut = new FileOutputStream(file);
					while ((readedBytes = inputStream.read(buf)) > 0) {
						fileOut.write(buf, 0, readedBytes);
					}
					fileOut.close();
					inputStream.close();
				}
			}
			zipFile.close();
		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * 压缩文件
	 * 
	 * @param resFile
	 *            需要压缩的文件（夹）
	 * @param zipout
	 *            压缩的目的文件
	 * @param rootpath
	 *            压缩的文件路径
	 * @throws FileNotFoundException
	 *             找不到文件时抛出
	 * @throws IOException
	 *             当压缩过程出错时抛出
	 */
	private static void zipFile(File resFile, ZipOutputStream zipout,
			String rootpath) throws FileNotFoundException, IOException {
		rootpath = rootpath
				+ (rootpath.trim().length() == 0 ? "" : File.separator)
				+ resFile.getName();
		rootpath = new String(rootpath.getBytes("8859_1"), "GB2312");

		Log.i("Yees",
				"zipFile -- resFile.getAbsolutePath() = "
						+ resFile.getAbsolutePath() + "---rootpath = "
						+ rootpath);

		if (resFile.isDirectory()) {
			File[] fileList = resFile.listFiles();
			for (File file : fileList) {
				zipFile(file, zipout, rootpath);
			}
			Log.i("Yees", "zipFile -- finish");
		} else {
			byte buffer[] = new byte[BUFF_SIZE];
			BufferedInputStream in = new BufferedInputStream(
					new FileInputStream(resFile), BUFF_SIZE);
			zipout.putNextEntry(new ZipEntry(rootpath));
			int realLength;
			while ((realLength = in.read(buffer)) != -1) {
				zipout.write(buffer, 0, realLength);
			}
			in.close();
			zipout.flush();
			zipout.closeEntry();
		}
	}

	public static byte[] getBytes(InputStream is) throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = is.read(buffer)) != -1) {
			bos.write(buffer, 0, len);
		}
		is.close();
		bos.flush();
		byte[] result = bos.toByteArray();
		return result;
	}

	/**
	 * 
	 * @param files
	 *            要压缩的文件集合
	 * @param zipFilePath
	 *            生成zip文件的绝对路径
	 * @return
	 */
	public static File zipFiles(List<File> files, String zipFilePath) {
		File tempZipFile = null;
		try {
			tempZipFile = new File(zipFilePath);
			byte[] buf = new byte[1024];
			// ZipOutputStream类：完成文件或文件夹的压缩
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(
					tempZipFile));
			for (int i = 0; i < files.size(); i++) {
				zipFile(files.get(i), out, "");
			}
			out.close();
			System.out.println("压缩完成.");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tempZipFile;
	}

	/**
	 * 拷贝文件夹
	 * 
	 * @param srcDirector
	 * @param desDirector
	 * @param pd
	 * @param sleepTime
	 * @return
	 */
	public static boolean copyFileWithDirector(String srcDirector,
			String desDirector) {
		try {
			(new File(desDirector)).mkdirs();
			File[] file = (new File(srcDirector)).listFiles();
			for (int i = 0; i < file.length; i++) {
				if (file[i].isFile()) {
					FileInputStream input = new FileInputStream(file[i]);
					FileOutputStream output = new FileOutputStream(desDirector
							+ "/" + file[i].getName());
					byte[] b = new byte[1024 * 5];
					int len;
					while ((len = input.read(b)) != -1) {
						output.write(b, 0, len);
					}
					output.flush();
					output.close();
					input.close();
				}
				if (file[i].isDirectory()) {
					copyFileWithDirector(srcDirector + "/" + file[i].getName(),
							desDirector + "/" + file[i].getName());
				}
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 删除指定文件夹下所有文件
	 * 
	 * @param path
	 *            文件夹完整绝对路径
	 * @return
	 */

	public static void DeleteFile(File file) {
		if (!file.exists()) {
			return;
		} else {
			if (file.isFile()) {
				file.delete();
				return;
			}
			if (file.isDirectory()) {
				File[] childFile = file.listFiles();
				if (childFile == null || childFile.length == 0) {
					file.delete();
					return;
				}
				for (File f : childFile) {
					DeleteFile(f);
				}
				file.delete();
			}
		}
	}
	
	public static List<File> getFiles(String[] args) {
		List<File> files = new ArrayList<File>();
		for (int i = 0; i < args.length; i++) {
				String path = getAbsolutePath(args[i].trim());//将path从原相对路径改为绝对路径
				if (!TextUtils.isEmpty(path)) {
					File file = new File(path);
					Log.d("Yees", "getFiles -->" + file.isFile());
					files.add(file);
				}
		}
		return files;
	}
	
	private static String getAbsolutePath(String path){
		StringBuilder sb = null;
		sb = new StringBuilder();
		sb.append(getSdcardDir()).append(path.startsWith(File.separator) ? "" : File.separator).append(path);
		return sb.toString();
	}

	
	/**
	 * 获取目录下所有文件
	 * @param path
	 * @return
	 */
	public static List<File> getFiles(String path){   
        File file = new File(path);   
        if(!file.exists())
        	return null;
        ArrayList<File> files = new ArrayList<File>();
        File[] array = file.listFiles();   
        for(int i=0;i<array.length;i++){   
            if(array[i].isFile()){   
            	files.add(array[i]);
            }else if(array[i].isDirectory()){   
            	getFiles(array[i].getPath());   
            }   
        }
		return files;   
    }    
	
	/**
	 * 获取sd卡的路径
	 * @return
	 */
	public static String getSdcardDir() {
		if (Environment.getExternalStorageState().equalsIgnoreCase(
				Environment.MEDIA_MOUNTED)) {
			return Environment.getExternalStorageDirectory().toString();
		}
		return null;
	}
	

	public static final long CYCLE = 7 * 24 * 60 * 60000L;
	/**
	 * 递归删除文件
	 * @param file 文件或文件夹
	 * @param cycle 允许保存的时间(毫秒为单位) 例 : cycle = 20 * 24 * 60 * 60000L; // log保留时间为 20天
	 * 注:cycle = 0 默认只保存7天的log
	 */
	public static void delFile(File file, long cycle) {
		if (!file.exists()) {
			return;
		} else {
			cycle = cycle == -1 ? CYCLE : cycle;
			// 如果file是文件并且属于在被删除的条件
			if (file.isFile() && delFileOfDate(file, cycle)) {
				file.delete();
				return;
			}
			if (file.isDirectory()) {
				File[] childFile = file.listFiles();
				if (childFile == null || childFile.length == 0) {
					file.delete();
					return;
				}
				for (File f : childFile) {
					delFile(f, cycle);
				}
			}
		}
	}
	
	private static boolean delFileOfDate(File file, long cycle) {
		long lastModified_time = file.lastModified();// 返回文件最后修改时间，是以个long型毫秒数
		long now_time =  System.currentTimeMillis();
		long jet_time = now_time - lastModified_time;
		if (lastModified_time > 0 && jet_time > cycle)
			return true;
		else
			return false;
	}
	

}
