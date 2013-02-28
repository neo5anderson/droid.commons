package neo.droid.commons;

import neo.java.commons.Strings;
import android.content.Context;
import android.content.SharedPreferences.Editor;

/**
 * 配置文件工具类
 * 
 * @author neo
 */
public class Configurations {

	/** 配置文件名称 */
	private static String NAME;

	private static Context CONTEXT = null;
	private static int MODE = Context.MODE_PRIVATE;

	/**
	 * 初始化
	 * 
	 * @param context
	 * @param fileName
	 *            配置文件名
	 * @return 是否成功
	 */
	public static boolean make(Context context, String fileName) {
		if (null == context || Strings.isEmpty(fileName))
			return false;

		Configurations.CONTEXT = context;
		Configurations.NAME = fileName;
		return true;
	}

	/**
	 * 设置配置文件存取模式
	 * 
	 * @param mode
	 *            默认 Context.MODE_PRIVATE
	 */
	public static void setMode(int mode) {
		Configurations.MODE = mode;
	}

	/**
	 * 设置 boolean 类型的配置变量
	 * 
	 * @param keyResId
	 *            键名资源 ID
	 * @param value
	 *            值
	 * @return 是否保存成功
	 */
	public static boolean setBoolean(int keyResId, boolean value) {
		if (null == CONTEXT || Strings.isEmpty(NAME))
			return false;

		Editor editor = CONTEXT.getSharedPreferences(NAME, MODE).edit();
		editor.putBoolean(CONTEXT.getString(keyResId), value);
		return editor.commit();
	}

	/**
	 * 设置 int 类型的配置变量
	 * 
	 * @param keyResId
	 *            键名资源 ID
	 * @param value
	 *            值
	 * @return 是否保存成功
	 */
	public static boolean setInt(int keyResId, int value) {
		if (null == CONTEXT || Strings.isEmpty(NAME))
			return false;

		Editor editor = CONTEXT.getSharedPreferences(NAME, MODE).edit();
		editor.putInt(CONTEXT.getString(keyResId), value);
		return editor.commit();
	}

	/**
	 * 设置 float 类型的配置变量
	 * 
	 * @param keyResId
	 *            键名资源 ID
	 * @param value
	 *            值
	 * @return 是否保存成功
	 */
	public static boolean setFloat(int keyResId, float value) {
		if (null == CONTEXT || Strings.isEmpty(NAME))
			return false;

		Editor editor = CONTEXT.getSharedPreferences(NAME, MODE).edit();
		editor.putFloat(CONTEXT.getString(keyResId), value);
		return editor.commit();
	}

	/**
	 * 设置 String 类型的配置变量
	 * 
	 * @param keyResId
	 *            键名资源 ID
	 * @param value
	 *            值
	 * @return 是否保存成功
	 */
	public static boolean setString(int keyResId, String value) {
		if (null == CONTEXT || Strings.isEmpty(NAME))
			return false;

		Editor editor = CONTEXT.getSharedPreferences(NAME, MODE).edit();
		editor.putString(CONTEXT.getString(keyResId), value);
		return editor.commit();
	}

	/**
	 * 获取 boolean 类型配置变量的值
	 * 
	 * @param keyResId
	 *            键名资源 ID
	 * @return boolean 值
	 */
	public static boolean getBoolean(int keyResId) {
		if (null == CONTEXT || Strings.isEmpty(NAME))
			return false;

		return CONTEXT.getSharedPreferences(NAME, MODE).getBoolean(
				CONTEXT.getString(keyResId), false);
	}

	/**
	 * 获取 int 类型配置变量的值
	 * 
	 * @param keyResId
	 *            键名资源 ID
	 * @param defaultInt
	 *            默认值
	 * @return int 值
	 */
	public static int getInt(int keyResId, int defaultInt) {
		if (null == CONTEXT || Strings.isEmpty(NAME))
			return defaultInt;

		return CONTEXT.getSharedPreferences(NAME, MODE).getInt(
				CONTEXT.getString(keyResId), defaultInt);
	}

	/**
	 * 获取 float 类型配置变量的值
	 * 
	 * @param keyResId
	 *            键名资源 ID
	 * @param defaultFloat
	 *            默认值
	 * @return float 值
	 */
	public static float getFloat(int keyResId, int defaultFloat) {
		if (null == CONTEXT || Strings.isEmpty(NAME))
			return defaultFloat;

		return CONTEXT.getSharedPreferences(NAME, MODE).getFloat(
				CONTEXT.getString(keyResId), defaultFloat);
	}

	/**
	 * 获取 String 类型配置变量的值
	 * 
	 * @param keyResId
	 *            键名资源 ID
	 * @param defaultValueResId
	 *            默认值资源 ID
	 * @return String 值
	 */
	public static String getString(int keyResId, int defaultValueResId) {
		return getString(keyResId, CONTEXT.getString(defaultValueResId));
	}

	/**
	 * 获取 String 类型配置变量的值
	 * 
	 * @param keyResId
	 *            键名资源 ID
	 * @param defaultString
	 *            默认值
	 * @return String 值
	 */
	public static String getString(int keyResId, String defaultString) {
		if (null == CONTEXT || Strings.isEmpty(NAME))
			return defaultString;

		return CONTEXT.getSharedPreferences(NAME, MODE).getString(
				CONTEXT.getString(keyResId), defaultString);
	}
}
