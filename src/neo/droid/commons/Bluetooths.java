package neo.droid.commons;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

/**
 * Android 蓝牙工具类
 * 
 * @author neo
 * 
 */
public class Bluetooths {

	/** 蓝牙固定的 UUID */
	public static final String UUID = "00001101-0000-1000-8000-00805F9B34FB";

	/** 内容提供者 */
	public static Context CONTEXT;

	/** 活动界面消息处理 */
	public static Handler HANDLER;
	/** 活动界面消息反馈 what 标志位 */
	private static int WHAT;
	/** 活动界面消息反馈 arg1 标志位 */
	private static int ARG1;
	/** 发现新的蓝牙设备 */
	public static final int ARG2_FOUND = 1;
	/** 发现已绑定的蓝牙设备 */
	public static final int ARG2_BONDED = 2;

	/** 蓝牙适配器 */
	private static BluetoothAdapter ADAPTER = BluetoothAdapter
			.getDefaultAdapter();

	/** 蓝牙已绑定设备列表 */
	private static List<BluetoothDevice> DEVICE_LIST = new ArrayList<BluetoothDevice>();
	/** 蓝牙已绑定设备描述列表 */
	private static List<Map<String, String>> DEVICE_DETAIL_LIST = new ArrayList<Map<String, String>>();

	/**
	 * 初始化
	 * 
	 * @param context
	 *            内容提供者
	 * @param handler
	 *            消息处理
	 * @param what
	 *            消息处理 what 标志位
	 * @param arg1
	 *            消息处理 arg1 标志位
	 */
	public static void init(Context context, Handler handler, int what, int arg1) {
		CONTEXT = context;
		HANDLER = handler;
		WHAT = what;
		ARG1 = arg1;
	}

	/**
	 * 获取蓝牙设备状态的字符表示形式
	 * 
	 * @param stateCode
	 *            getBondState() 的返回值
	 * @return 对应的字符，非法数值会返回 INVLIAD
	 */
	private static String getBluetoothState(int stateCode) {
		switch (stateCode) {
		case BluetoothDevice.BOND_BONDED:
			return "BONDED";

		case BluetoothDevice.BOND_BONDING:
			return "BONDING";

		case BluetoothDevice.BOND_NONE:
			return "NONE";

		default:
			return "INVALID";
		}
	}

	/** 刷新设备列表 */
	protected static List<Map<String, String>> reloadDevices() {
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		Set<BluetoothDevice> pairedDevices = ADAPTER.getBondedDevices();
		DEVICE_DETAIL_LIST.clear();
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				Map<String, String> map = new HashMap<String, String>();
				map.put("name", device.getName());
				map.put("addr", device.getAddress());
				map.put("state", getBluetoothState(device.getBondState()));
				list.add(map);
				DEVICE_DETAIL_LIST.add(map);
				DEVICE_LIST.add(device);
			}
		}
		return list;
	}

	/**
	 * 查看当前的蓝牙是否已启用
	 * 
	 * @return 是否
	 */
	public static boolean isEnabled() {
		return ADAPTER.isEnabled();
	}

	/**
	 * 判断当前是否有已配对的蓝牙设备
	 * 
	 * @return 是否
	 */
	public static boolean isBonded() {
		if (isEnabled()) {
			reloadDevices();
			return (DEVICE_LIST.size() > 0);
		}
		return false;
	}

	/**
	 * 发送系统所提供打开蓝牙设备的认证对话框
	 * 
	 * @return 意图是否被发送
	 */
	public static boolean requestEnable() {
		return postIntent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	}

	/**
	 * 请求系统开始搜索周围的蓝牙设备
	 * 
	 * @return 是否开始搜索
	 */
	public static boolean requestDiscovery() {
		return ADAPTER.startDiscovery();
	}

	/**
	 * 发送意图
	 * 
	 * @param action
	 *            意图的 Action
	 * @return 是否被成功发送
	 */
	private static boolean postIntent(String action) {
		try {
			Intent intent = new Intent(action);
			CONTEXT.startActivity(intent);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * 直接打开蓝牙设备，不做任何提示
	 * 
	 * @return 是否成功
	 */
	public static boolean forceEnable() {
		if (null != ADAPTER) {
			return ADAPTER.enable();
		}
		return false;
	}

	/**
	 * 直接关闭蓝牙设备，不做任何提示
	 * 
	 * @return 是否成功
	 */
	public static boolean forceDisable() {
		if (null != ADAPTER) {
			return ADAPTER.disable();
		}
		return false;
	}

	/**
	 * 获取指定索引的蓝牙设备
	 * 
	 * @param deviceListIndex
	 *            列表索引
	 * @return 蓝牙设备对象
	 */
	public static BluetoothDevice getDevice(int deviceListIndex) {
		if (deviceListIndex < DEVICE_LIST.size()) {
			return DEVICE_LIST.get(deviceListIndex);
		}
		return null;
	}

	/**
	 * 创建蓝牙绑定配对
	 * 
	 * @param device
	 *            蓝牙设备对象
	 * @return 是否成功
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static boolean createBond(BluetoothDevice device)
			throws SecurityException, NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
		return ((Boolean) createBondMethod.invoke(device)).booleanValue();
	}

	/**
	 * 删除蓝牙绑定配对
	 * 
	 * @param device
	 *            蓝牙设备对象
	 * @return 是否成功
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static boolean removeBond(BluetoothDevice device)
			throws SecurityException, NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		Method removeMethod = BluetoothDevice.class.getMethod("removeBond");
		return ((Boolean) removeMethod.invoke(device)).booleanValue();
	}

	/**
	 * 设置蓝牙绑定配对 PIN
	 * 
	 * @param device
	 *            蓝牙设备对象
	 * @param pinString
	 *            配对字符
	 * @return 是否成功
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static boolean setPin(BluetoothDevice device, String pinString)
			throws SecurityException, NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		Method autoBondMethod = BluetoothDevice.class.getMethod("setPin",
				new Class[] { byte[].class });
		return ((Boolean) autoBondMethod.invoke(device,
				new Object[] { pinString.getBytes() })).booleanValue();
	}

	/**
	 * 取消用户输入
	 * 
	 * @param device
	 *            蓝牙设备对象
	 * @return 是否成功
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static boolean cancelPairingUserInput(BluetoothDevice device)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException, SecurityException, NoSuchMethodException {
		String string = "cancelPairingUserInput";
		Method cancelUserInputMethod = BluetoothDevice.class.getMethod(string);
		return ((Boolean) cancelUserInputMethod.invoke(device)).booleanValue();
	}

	/**
	 * 蓝牙广播接收器
	 * 
	 * @author neo
	 */
	public static class BluetoothReceiver extends BroadcastReceiver {
		private Message message;

		public void onReceive(Context context, Intent intent) {
			BluetoothDevice device;
			String actionString = intent.getAction();
			// [Neo] 系统找到一个蓝牙设备
			if (BluetoothDevice.ACTION_FOUND.equals(actionString)) {
				device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				message = HANDLER.obtainMessage(WHAT, ARG1, ARG2_FOUND, device);
				HANDLER.sendMessage(message);
			}
			// [Neo] 新绑定了蓝牙设备
			else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED
					.equals(actionString)) {
				if (BluetoothDevice.BOND_BONDED == intent.getIntExtra(
						BluetoothDevice.EXTRA_BOND_STATE,
						BluetoothDevice.BOND_NONE)) {
					device = intent
							.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					message = HANDLER.obtainMessage(WHAT, ARG1, ARG2_BONDED,
							device);
					HANDLER.sendMessage(message);
				}
			}
		}
	}
}
