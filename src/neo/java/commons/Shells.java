package neo.java.commons;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Shells {
	/** 指定执行 shell 的平台为 Windows */
	public static final int TYPE_WINDOWS = 0x01;
	/** 指定执行 shell 的平台为 类 Unix 桌面操作系统 */
	public static final int TYPE_UNIX_LIKE_PC = 0x02;
	/** 指定执行 shell 的平台为 Android 移动平台 */
	public static final int TYPE_ANDROID = 0x03;

	private Timer timer;
	private Process process;
	private int shellType;
	private String packageName;
	private boolean isWorking;

	private String[] cmds;

	/**
	 * 构造，通过系统的信息判断平台
	 * 
	 */
	public Shells() {
		if (System.getProperty("os.name").contains("ows")) {
			shellType = Shells.TYPE_WINDOWS;
		} else {
			shellType = Shells.TYPE_UNIX_LIKE_PC;
		}
		packageName = null;
		init();
	}

	/**
	 * 指定当前为 Android 移动平台，并指定所在包名以便中断时取样
	 * 
	 * @param packageName
	 *            所在包名
	 */
	public Shells(String packageName) {
		this.shellType = TYPE_ANDROID;
		this.packageName = packageName;
		init();
	}

	private void init() {
		isWorking = false;
		if (Shells.TYPE_WINDOWS == shellType) {
			cmds = new String[] { "cmd.exe", "/c", "" };
		} else {
			cmds = new String[] { "sh", "-c", "" };
		}
	}

	/**
	 * 运行 shell
	 * 
	 * @param cmd
	 *            具体的命令
	 * @param delayed2Kill
	 *            延迟多久之后关闭
	 * @return 返回执行结果
	 */
	public String exec(String cmd, int delayed2Kill) {
		if (delayed2Kill > 0) {
			timer = new Timer();
		}

		int read = 0;
		cmds[2] = cmd;
		StringBuilder sBuilder = new StringBuilder();

		try {
			ProcessBuilder pBuilder = new ProcessBuilder(cmds);
			pBuilder.redirectErrorStream(true);

			process = pBuilder.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					process.getInputStream()));

			if (null != timer) {
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						stop();
					}
				}, delayed2Kill);
			}

			isWorking = true;

			while (-1 != (read = reader.read())) {
				sBuilder.append((char) read);
			}

			isWorking = false;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return sBuilder.toString();
	}

	/**
	 * 执行执行的 shell 命令
	 * 
	 * @param cmd
	 *            具体的命令
	 * @return 返回执行的结果
	 * 
	 */
	public String exec(String cmd) {
		return exec(cmd, -1);
	}

	/**
	 * 强制停止当前所执行的命令
	 * 
	 * @return 是否成功
	 */
	public boolean stop() {
		if (null != timer) {
			timer.cancel();
			timer = null;
		}

		if (TYPE_ANDROID == shellType) {
			new Thread() {
				@Override
				public void run() {
					String key = cmds[2];

					if (null == key || 0 == key.length()) {
						return;
					}

					if (null == packageName) {
						return;
					}

					key = key.substring(0, key.indexOf(" "));

					String ppid = null;
					boolean isKillNext = false;

					Matcher matcher = Pattern
							.compile(
									"\\w+\\s+(\\d+)\\s+(\\d+)\\s+\\d+\\s+\\d+\\s+\\S+\\s+\\S+\\s+\\S+\\s+(\\S+).*")
							.matcher(new Shells().exec("ps"));

					while (matcher.find()) {
						if (false != isKillNext) {
							if (matcher.group(3).contains(key)) {
								new Shells().exec("kill " + matcher.group(1));
							}
							isKillNext = false;
						}

						if (3 == matcher.groupCount()) {
							if (matcher.group(3).contains(packageName)
									&& (null == ppid)) {
								ppid = matcher.group(1);
							}

							if (null != ppid && matcher.group(2).contains(ppid)
									&& matcher.group(3).contains("sh")) {
								isKillNext = true;
							}
						}
					}
				}
			}.start();
			
		} else {
			if (null != process && false != isWorking) {
				isWorking = false;
				new Thread() {
					@Override
					public void run() {
						process.destroy();
						process = null;
					}
				}.start();
			} else {
				return false;
			}
		}
		return true;
	}

}
