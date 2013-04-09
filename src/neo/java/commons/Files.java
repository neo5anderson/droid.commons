package neo.java.commons;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

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
	 * 给 listFile 的结果集排序
	 * 
	 * @param files
	 */
	public static void sort(File[] files) {
		Arrays.sort(files, new Comparator<File>() {

			@Override
			public int compare(File file1, File file2) {
				if (false != file1.isDirectory()
						&& false == file2.isDirectory()) {
					return -1;
				} else if (false == file1.isDirectory()
						&& false != file2.isDirectory()) {
					return 1;
				} else {
					return file1.getName().compareToIgnoreCase(file2.getName());
				}
			}
		});
	}

	/**
	 * 删除文件或文件夹
	 * 
	 * @param file
	 *            目标文件
	 * @return 是否成功删除
	 */
	public static boolean delete(File file) {
		boolean result = false;

		if (false != file.isDirectory()) {
			File[] files = file.listFiles();
			if (null != files && files.length > 0) {
				for (File f : files) {
					result = delete(f);
					if (false == result) {
						break;
					}
				}

				if (false != result) {
					result = file.delete();
				}
			}
		} else {
			result = file.delete();
		}

		return result;
	}

	/**
	 * 格式化文件大小
	 * 
	 * @param size
	 *            实际大小，file.length 获取
	 * @param formatter
	 *            格式化模式，比如 %.2f
	 * @return 格式化后的字符串对象
	 */
	public static String formatFileSize(long size, String formatter) {
		if (size > 1000000) {
			return String.format(formatter + "MB", size / 1024.0f / 1024);
		} else if (size > 1000) {
			return String.format(formatter + "KB", size / 1024.0f);
		} else {
			return size + "B";
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
