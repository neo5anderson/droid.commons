package neo.droid.commons;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.view.Display;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

/**
 * 资源工具类
 * 
 * @author neo
 */
public class ResUtils {

	protected static Context CONTEXT;

	/** 当前的 API 等级 */
	private static final int API_LEVEL = Integer
			.parseInt(android.os.Build.VERSION.SDK);

	/** 显示对象 */
	private static Display DISPLAY;
	/** 屏幕长度 */
	private static int DISPLAY_WIDTH;
	/** 屏幕宽度 */
	private static int DISPLAY_HEIGHT;

	/** 设备的 MEID */
	private static String MEID;
	/** 设备的密度 */
	private static float DENSITY;

	private static String USER_AGENT = "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1)";

	/**
	 * 初始化
	 * 
	 * @param context
	 */
	public static void make(Context context) {
		CONTEXT = context;
		DENSITY = context.getResources().getDisplayMetrics().density;

		try {
			// [Neo] 有点小纠结，预览时可能会有空指针异常
			DISPLAY = ((Activity) CONTEXT).getWindowManager()
					.getDefaultDisplay();

			if (API_LEVEL < 13) {
				DISPLAY_WIDTH = DISPLAY.getWidth();
				DISPLAY_HEIGHT = DISPLAY.getHeight();
			} else {
				DISPLAY_WIDTH = 0;
				DISPLAY_HEIGHT = 0;
			}
		} catch (Exception e) {
			DISPLAY = null;
			DISPLAY_WIDTH = 0;
			DISPLAY_HEIGHT = 0;
		}
	}

	/**
	 * 获取 Context
	 * 
	 * @return context
	 */
	public static Context getContext() {
		return CONTEXT;
	}

	/**
	 * 获取资源对象
	 * 
	 * @return 资源对象
	 */
	public static Resources getResources() {
		if (null != CONTEXT) {
			return CONTEXT.getResources();
		} else {
			return null;
		}
	}

	/**
	 * 获取显示对象
	 * 
	 * @return 显示对象
	 */
	public static Display getDisplay() {
		return DISPLAY;
	}

	/**
	 * 获取屏幕长度
	 * 
	 * @return 长度
	 */
	public static int getDisplayWidth() {
		return DISPLAY_WIDTH;
	}

	/**
	 * 获取屏幕高度
	 * 
	 * @return 高度
	 */
	public static int getDisplayHeight() {
		return DISPLAY_HEIGHT;
	}

	/**
	 * 获取资源字符串
	 * 
	 * @param resID
	 *            资源 ID
	 * @return 字符串
	 */
	public static String getString(int resID) {
		if (null != CONTEXT) {
			return CONTEXT.getString(resID);
		} else {
			return "";
		}
	}

	/**
	 * 获取 Drawable 对象
	 * 
	 * @param resID
	 *            资源 ID
	 * @return Drawable 对象
	 */
	public static Drawable getDrawable(int resID) {
		if (null != CONTEXT) {
			return getResources().getDrawable(resID);
		} else {
			return null;
		}
	}

	/**
	 * 获取文件输入流
	 * 
	 * @param fileName
	 *            文件名
	 * @return 文件输入流
	 */
	public static InputStream getResourceAsStream(String fileName) {
		if (null != CONTEXT) {
			return CONTEXT.getClass().getResourceAsStream(fileName);
		} else {
			return null;
		}
	}

	/**
	 * 获取 res/raw 文件输入流
	 * 
	 * @param resID
	 *            资源 ID
	 * @return 文件输入流
	 */
	public static InputStream getRawInputStream(int resID) {
		return getResources().openRawResource(resID);
	}

	/**
	 * 获取 res/raw 文件名
	 * 
	 * @param resID
	 *            资源 ID
	 * @return 文件名称
	 */
	public static String getResRawFileName(int resID) {
		if (null != CONTEXT) {
			String fileName = "" + getResources().getText(resID);
			if (8 < fileName.length()) {
				return fileName.substring(8);
			}
		}
		return "";
	}

	/**
	 * 获取 assets 文件输入流
	 * 
	 * @param fileName
	 *            文件名
	 * @return 输入流
	 */
	public static InputStream getAssetsInputStream(String fileName)
			throws IOException {
		return CONTEXT.getAssets().open(fileName);
	}

	/**
	 * 获取 assets 文件描述对象
	 * 
	 * @param fileName
	 *            文件名
	 * @return 文件描述对象
	 */
	public static AssetFileDescriptor getAssetFileDescriptor(String fileName)
			throws IOException {
		return CONTEXT.getAssets().openFd(fileName);
	}

	/**
	 * 复制 assets 的文件至指定路径
	 * 
	 * @param targetPath
	 *            指定路径
	 * @return 是否成功
	 */
	public static boolean cpAssetFileTo(String targetPath) {
		InputStream inputStream = null;
		FileOutputStream outputStream = null;

		try {
			String[] files = CONTEXT.getAssets().list("");

			int perLength;
			byte[] buffer = new byte[1024];

			for (int i = 0; i < files.length; i++) {
				try {
					inputStream = getAssetsInputStream(files[i]);
				} catch (Exception e) {
					break;
				}

				outputStream = new FileOutputStream(new File(targetPath,
						files[i]));

				while (-1 != (perLength = inputStream.read(buffer))) {
					outputStream.write(buffer, 0, perLength);
				}

				outputStream.flush();
				inputStream.close();
				outputStream.close();

			}

		} catch (Exception e) {
			e.printStackTrace();
			try {
				if (null != inputStream) {
					inputStream.close();
				}
				if (null != outputStream) {
					outputStream.close();
				}
			} catch (Exception e2) {
				// [Neo] Empty

			}

			return false;
		}

		return true;
	}

	/**
	 * 获取带有图像的 ImageView
	 * 
	 * @param bitmap
	 *            比特图对象
	 * @return ImageView
	 */
	public static ImageView getImageViewAttachedBitmap(Bitmap bitmap) {
		if (null != CONTEXT) {
			ImageView imageView = new ImageView(CONTEXT);
			imageView.setImageBitmap(bitmap);
			return imageView;
		} else {
			return null;
		}
	}

	/**
	 * 播放点声音的类
	 * 
	 * @author neo
	 */
	public static class MakeSomeNoise extends Thread {

		private FileDescriptor fileDescriptor;

		public MakeSomeNoise(FileDescriptor fileDescriptor) {
			this.fileDescriptor = fileDescriptor;
		}

		@Override
		public void run() {
			try {
				MediaPlayer player = new MediaPlayer();
				player.setDataSource(fileDescriptor);
				player.prepare();
				player.setOnCompletionListener(new OnCompletionListener() {
					// @Override
					public void onCompletion(MediaPlayer mp) {
						mp.reset();
						mp.release();
					}
				});
				player.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	/**
	 * 线密度转像素
	 * 
	 * @param dp
	 *            线密度
	 * @return 像素值
	 */
	public static int dp2px(int dp) {
		return (int) (dp * DENSITY + 0.5f);
	}

	/**
	 * 像素转线密度
	 * 
	 * @param px
	 *            像素
	 * @return 线密度值
	 */
	public static int px2dp(int px) {
		return (int) (px / DENSITY + 0.5f);
	}

	/**
	 * 显示输入法
	 * 
	 * @param view
	 *            有效的一个视图对象
	 */
	public static void showIMM(View view) {
		view.requestFocus();
		InputMethodManager imm = (InputMethodManager) CONTEXT
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(view, InputMethodManager.RESULT_SHOWN);
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,
				InputMethodManager.HIDE_IMPLICIT_ONLY);
	}

	/**
	 * 隐藏输入法
	 * 
	 * @param view
	 *            有效的一个视图对象
	 */
	public static void hideIMM(View view) {
		if (null != view) {
			InputMethodManager imm = (InputMethodManager) CONTEXT
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}

	/**
	 * 获取设备 MEID
	 * 
	 * @return 字符串类型，已转大写
	 */
	public static String getMEID() {
		if (null == MEID || MEID.length() < 8) {
			MEID = ((TelephonyManager) CONTEXT
					.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId()
					.toUpperCase();
		}
		return MEID;
	}

	public static Map<String, String> getRClassField(String className) {
		Class<?> class2do = null;

		try {
			class2do = Class.forName(CONTEXT.getPackageName() + ".R$"
					+ className);
		} catch (Exception e) {
			return null;
		}

		Map<String, String> map = new HashMap<String, String>();

		Field[] fields = class2do.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			try {
				map.put(fields[i].getName(), "" + fields[i].getInt(null));
			} catch (Exception e) {
				map.put(fields[i].getName(), "0");
				e.printStackTrace();
			}
		}

		return map;
	}

	/**
	 * 获取当前的缓存目录
	 * 
	 * @return 字符串路径
	 */
	public static String getCacheDir() {
		return CONTEXT.getCacheDir().getPath();
	}

	/**
	 * 获取当前的 Files 目录
	 * 
	 * @return 字符串路径
	 */
	public static String getFilesDir() {
		return CONTEXT.getFilesDir().getPath();
	}

	/**
	 * 获取 SD 目录
	 * 
	 * @return 字符串路径
	 */
	public static String getSDDir() {
		return Environment.getExternalStorageDirectory().getPath();
	}

	/**
	 * 判断 SD 是否已经挂载
	 * 
	 * @return 是否
	 */
	public static boolean isSDAvailable() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			return true;
		}
		return false;
	}

	/**
	 * 获取 SD 可用空间
	 * 
	 * @return 整型数据
	 */
	public static long getSDAvailableBytes() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			StatFs state = new StatFs(Environment.getExternalStorageDirectory()
					.getPath());
			return state.getAvailableBlocks() * state.getBlockSize();
		} else {
			return 0L;
		}
	}

	/**
	 * 获取本地网络设备的地址，lo eth wlan
	 * 
	 * @return 设备名称 => 设备地址
	 */
	public static Map<String, String> getLocalIpAddr() {
		try {
			Map<String, String> map = new HashMap<String, String>();
			for (Enumeration<NetworkInterface> eNetIf = NetworkInterface
					.getNetworkInterfaces(); eNetIf.hasMoreElements();) {
				NetworkInterface netIf = eNetIf.nextElement();
				String name = netIf.getName();
				if (name.contains("lo") || name.contains("eth")
						|| name.contains("wlan")) {
					for (Enumeration<InetAddress> eIpAddr = netIf
							.getInetAddresses(); eIpAddr.hasMoreElements();) {
						InetAddress addr = eIpAddr.nextElement();
						map.put(name, addr.getHostAddress());
					}
				}
			}
			return map;
		} catch (SocketException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 获取已安装的某类应用列表
	 * 
	 * @param flags
	 *            像素
	 * @return 应用列表
	 */
	public static List<ResolveInfo> getPackageList(int flags) {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		return CONTEXT.getPackageManager().queryIntentActivities(intent, flags);
	}

	/**
	 * 获取已安装的 Activities 应用列表
	 * 
	 * @return 应用列表
	 */
	public static List<ResolveInfo> getActivitiesPackageList() {
		return getPackageList(PackageManager.GET_ACTIVITIES);
	}

	/**
	 * 获取某个应用的包信息
	 * 
	 * @param packageName
	 *            该应用的报名，null 表示当前包名
	 * @return 包信息
	 */
	public static PackageInfo getPackageInfo(String packageName)
			throws NameNotFoundException {
		if (null == packageName) {
			packageName = CONTEXT.getPackageName();
		}
		return CONTEXT.getPackageManager().getPackageInfo(packageName,
				PackageManager.GET_ACTIVITIES);
	}

	/**
	 * 获取 apk 文件的包信息
	 * 
	 * @param fileName
	 *            apk 文件名
	 * @return 包信息
	 */
	public static PackageInfo getPackageFromFile(String fileName) {
		if (null != fileName) {
			return CONTEXT.getPackageManager().getPackageArchiveInfo(fileName,
					PackageManager.GET_ACTIVITIES);
		} else {
			return null;
		}
	}

	/**
	 * 获取指定包名的版本号
	 * 
	 * @param packageName
	 *            报名，null 表示当前软件
	 * @return versionName
	 */
	public static String getPackageVersionName(String packageName)
			throws NameNotFoundException {
		return getPackageInfo(packageName).versionName;
	}

	/**
	 * 获取正在运行的服务信息
	 * 
	 * @param maxCount
	 *            统计个数的最大值
	 * @return 正在运行的服务列表
	 */
	public static List<ActivityManager.RunningServiceInfo> getRunningServerInfos(
			int maxCount) {
		ActivityManager manager = (ActivityManager) CONTEXT
				.getSystemService(Context.ACTIVITY_SERVICE);
		if (20 > maxCount)
			maxCount = 20;
		return manager.getRunningServices(maxCount);
	}

	/**
	 * 取消某个通知
	 * 
	 * @param notifyId
	 *            通知的 ID
	 * @return 是否成功
	 */
	public static boolean cancelNotification(int notifyId) {
		if (null == CONTEXT) {
			return false;
		}

		((NotificationManager) CONTEXT
				.getSystemService(Context.NOTIFICATION_SERVICE))
				.cancel(notifyId);
		return true;
	}

	/**
	 * 生成 POST 表单实体列表
	 * 
	 * @param map
	 *            表单键值对
	 * @return 表单实体列表
	 */
	public static List<NameValuePair> makeEntity(Map<String, String> map) {
		List<NameValuePair> list = new ArrayList<NameValuePair>(map.size());
		Iterator<Entry<String, String>> iterator = map.entrySet().iterator();
		Map.Entry<String, String> entry = null;
		if (map.size() > 0) {
			while (iterator.hasNext()) {
				entry = (Map.Entry<String, String>) iterator.next();
				list.add(new BasicNameValuePair(entry.getKey(), entry
						.getValue()));
			}
		}
		return list;
	}

	/**
	 * 生成默认配置的 HttpClient
	 * 
	 * @param ConnTimeout
	 *            连接超时时间
	 * @param SoTimeout
	 *            socket 超时时间
	 * @return 客户端
	 */
	public static HttpClient makeClient(int ConnTimeout, int SoTimeout) {
		BasicHttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, ConnTimeout);
		HttpConnectionParams.setSoTimeout(params, SoTimeout);

		HttpClient client = new DefaultHttpClient(params);
		client.getParams().setParameter(ClientPNames.COOKIE_POLICY,
				CookiePolicy.RFC_2109);

		return client;
	}

	/**
	 * 发送 POST 请求
	 * 
	 * @param url
	 *            服务器地址
	 * @param pairsMap
	 *            表单键值对
	 * @throws ClientProtocolException
	 * @return 客户端
	 */
	public static HttpResponse requestPOST(String url,
			Map<String, String> pairsMap) throws ClientProtocolException,
			IOException {
		HttpPost post = new HttpPost(url);
		post.setHeader("Accept", "*/*");
		post.setHeader("User-Agent", USER_AGENT);
		post.setEntity(new UrlEncodedFormEntity(makeEntity(pairsMap), "UTF-8"));
		return makeClient(5555, 5555).execute(post);
	}

	/**
	 * 发送 GET 请求
	 * 
	 * @param url
	 *            服务器地址
	 * @throws ClientProtocolException
	 * @return 客户端
	 */
	public static HttpResponse requestGET(String url)
			throws ClientProtocolException, IOException {
		HttpGet get = new HttpGet(url);
		get.setHeader("Accept", "*/*");
		get.setHeader("User-Agent", USER_AGENT);
		return makeClient(5555, 5555).execute(get);
	}

	/**
	 * 设置模拟 POST/GET 请求的 UserAgent
	 * 
	 * @param userAgent
	 *            待设置的 User-Agent
	 */
	public static void setUserAgent(String userAgent) {
		if (null != userAgent) {
			USER_AGENT = userAgent;
		} else {
			USER_AGENT = "";
		}
	}

	/**
	 * 解析 JSON
	 * 
	 * @param json
	 *            待解析的 json 字符串
	 * @param valueList
	 *            待获取的值列表
	 * @return 指定需求的键值对
	 * @throws JSONException
	 */
	public static Map<String, String> parseJson(String json,
			List<String> valueList) throws JSONException {
		Map<String, String> map = new HashMap<String, String>();
		JSONObject object = new JSONObject(json);
		for (int i = 0; i < valueList.size(); i++) {
			map.put(valueList.get(i), object.getString(valueList.get(i)));
		}
		return map;
	}

}
