package neo.droid.commons;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.Uri;

/**
 * Android 设备相关的工具类
 * 
 * @author neo
 */
public class DevUtils {

	/**
	 * 获取 wifi 状态
	 * 
	 * @return 状态对象
	 */
	public static State getWifiState() {
		if (null == ResUtils.CONTEXT)
			return null;

		ConnectivityManager cManager = (ConnectivityManager) (ResUtils.CONTEXT
				.getSystemService(Context.CONNECTIVITY_SERVICE));
		return cManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
				.getState();
	}

	/**
	 * 获取 3G 状态
	 * 
	 * @return 状态对象
	 */
	public static State get3GState() {
		if (null == ResUtils.CONTEXT)
			return null;

		ConnectivityManager cManager = (ConnectivityManager) (ResUtils.CONTEXT
				.getSystemService(Context.CONNECTIVITY_SERVICE));
		return cManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
				.getState();
	}

	/**
	 * 判断是否为连接状态
	 * 
	 * @param state
	 *            状态对象
	 * @return 是否
	 */
	private static boolean isConnectionState(State state) {
		if (State.CONNECTED == state || State.CONNECTING == state) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 判断 wifi 是否正在工作
	 * 
	 * @return 是否
	 */
	public static boolean isWifiWorking() {
		return isConnectionState(getWifiState());
	}

	/**
	 * 判断 3G 是否正在工作
	 * 
	 * @return 是否
	 */
	public static boolean is3GWorking() {
		return isConnectionState(get3GState());
	}

	/**
	 * 判断是否使用 xxwap 方式网上
	 * 
	 * @return 是否
	 */
	public static boolean isWAPMode() {
		String apnString = ((ConnectivityManager) ResUtils.CONTEXT
				.getSystemService(Context.CONNECTIVITY_SERVICE))
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getExtraInfo();

		if (null != apnString
				&& apnString.length() > 3
				&& "wap".equals(apnString.substring(apnString.length() - 3)
						.toLowerCase())) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 设置 APN 为 net 方式
	 * 
	 * @return 是否成功
	 * @deprecated 普通的应用是没有权限去执行此操作的，会直接终止应用；4.x 已弃用。
	 */
	public static boolean setNETMode() {
		ContentResolver resolver = ResUtils.CONTEXT.getContentResolver();
		Cursor cursor = resolver.query(
				Uri.parse("content://telephony/carriers"),
				new String[] { "_id" }, "apn like '%net' and current=1", null,
				null);

		if (null != cursor && cursor.moveToNext()) {
			ContentValues values = new ContentValues();
			values.put("apn_id", cursor.getString(0));
			resolver.update(
					Uri.parse("content://telephony/carriers/preferapn"),
					values, null, null);

			return true;
		} else {
			return false;
		}
	}
}
