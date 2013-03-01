package neo.java.commons;

import java.io.File;

/**
 * 文件相关的工具类
 * 
 * @author neo
 */
public class Files {

	/**
	 * 创建一个文件夹，存在同名的文件会删除它再创建指定文件夹
	 * 
	 * @param folderName
	 *            文件夹名称
	 * @return 创建是否成功
	 */
	public static boolean mkdir(String folderName) {
		File folder = new File(folderName);
		if (folder.exists() && folder.isDirectory()) {
			return true;
		}
		try {
			if (false == folder.isDirectory()) {
				folder.delete();
			}
			if (false == folder.exists()) {
				folder.mkdirs();
			}
		} catch (SecurityException e) {
			return false;
		}

		if (false == folder.exists()) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 修改某个文件的后缀名
	 * 
	 * @param fileName
	 *            文件名
	 * @param newExt
	 *            新的后缀名
	 * @return 修改后的文件名
	 */
	public static String chext(String fileName, String newExt) {
		int lastDot = fileName.lastIndexOf(".");
		int lastSlash = fileName.lastIndexOf("/");

		if (0 > lastDot || lastDot <= lastSlash) {
			return fileName + "." + newExt;
		} else {
			return fileName.substring(0, lastDot + 1) + newExt;
		}
	}

}
