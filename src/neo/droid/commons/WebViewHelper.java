package neo.droid.commons;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.webkit.DownloadListener;
import android.webkit.HttpAuthHandler;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings.ZoomDensity;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

/**
 * WebView 帮助类
 * 
 * @author neo
 */
@SuppressLint("SetJavaScriptEnabled")
public class WebViewHelper {

	/** 默认处理交互的 what **/
	public static final int DEFAULT_WHAT = 0x11;

	/** 实际定制的 what **/
	public static int WHAT = DEFAULT_WHAT;
	/** 开始打开页面的信息分类 **/
	public static final int ARG1_PAGE_START = 0x01;
	/** 开始装载页面资源的信息分类 **/
	public static final int ARG1_LOAD_RES_AGAIN = 0x02;
	/** 装载页面完毕的信息分类 **/
	public static final int ARG1_PAGE_FINISHED = 0x03;
	/** 打开页面时发生错误的信息分类 **/
	public static final int ARG1_RECV_ERROR = 0x04;
	/** 页面需要认证的信息分类 **/
	public static final int ARG1_HTTP_AUTH = 0x05;
	/** 页面需要下载的信息分类 **/
	public static final int ARG1_DOWNLOAD = 0x10;
	/** 滑动至顶部消息 **/
	public static final int ARG1_SCROLL_TO_TOP = 0x20;

	private MyWebView webView;
	private static Handler HANDLER;

	private boolean isGBK = true;

	private MyDownloadListener downloadListener = new MyDownloadListener();
	private MyWebViewClient webViewClient = new MyWebViewClient();
	private MyWebChromeClient webChromeClient = new MyWebChromeClient();

	/**
	 * 构造，需要有效的 webview 控件和消息处理 handler
	 * 
	 * @param webView
	 * @param handler
	 */
	public WebViewHelper(Context context, LinearLayout layout, Handler handler) {
		webView = new MyWebView(context);
		layout.removeAllViews();
		layout.addView(webView, new LinearLayout.LayoutParams(new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT)));
		HANDLER = handler;
		initWebView();
	}

	private void initWebView() {
		webView.setWebViewClient(webViewClient);
		webView.setDownloadListener(downloadListener);
		webView.setWebChromeClient(webChromeClient);

		// [Neo] 焦点支持
		webView.setFocusable(true);
		webView.requestFocus();

		// [Neo] 滚动条覆盖屏幕
		webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

		// [Neo] 缩放
		webView.getSettings().setSupportZoom(true);
		webView.getSettings().setDefaultZoom(ZoomDensity.MEDIUM);
		webView.getSettings().setBuiltInZoomControls(true);

		// [Neo] 启用 JS
		webView.getSettings().setJavaScriptEnabled(true);

		// [Neo] 字符集
		setGBK(isGBK);
	}

	/**
	 * 判断当前的字符集是否为 GBK
	 * 
	 * @return
	 */
	public boolean isGBK() {
		return isGBK;
	}

	/**
	 * 设置当前的字符集为 GBK
	 * 
	 * @param isGBK
	 */
	public void setGBK(boolean isGBK) {
		this.isGBK = isGBK;
		if (isGBK) {
			webView.getSettings().setDefaultTextEncodingName("GBK");
		} else {
			webView.getSettings().setDefaultTextEncodingName("UTF-8");
		}
	}

	/**
	 * 设置汇报的类型
	 * 
	 * @param what
	 */
	public static void setWHAT(int what) {
		WHAT = what;
	}

	/**
	 * 支持 js 对话框
	 * 
	 */
	private class MyWebChromeClient extends WebChromeClient {
		@Override
		public boolean onJsAlert(WebView view, String url, String message,
				JsResult result) {
			return super.onJsAlert(view, url, message, result);
		}
	}

	/**
	 * 下载监听
	 * 
	 */
	private class MyDownloadListener implements DownloadListener {
		@Override
		public void onDownloadStart(String url, String userAgent,
				String contentDisposition, String mimetype, long contentLength) {
			REPORT(ARG1_DOWNLOAD, 0, new DownloadKit(url, userAgent,
					contentDisposition, mimetype, contentLength));
		}
	}

	/**
	 * webView 的事物应答处理
	 * 
	 */
	private class MyWebViewClient extends WebViewClient {
		// [Neo] 记录载入资源的个数
		int loadResTimes = 0;

		@Override
		public void onLoadResource(WebView view, String url) {
			REPORT(ARG1_LOAD_RES_AGAIN, loadResTimes++, url);
			super.onLoadResource(view, url);
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			loadResTimes = 0;
			REPORT(ARG1_PAGE_START, 0, url);
			super.onPageStarted(view, url, favicon);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			loadResTimes = 0;
			REPORT(ARG1_PAGE_FINISHED, 0, url);

			// [Neo] 焦点的操作
			webView.setFocusable(true);
			webView.requestFocus();
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			return super.shouldOverrideUrlLoading(view, url);
		}

		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {

			// [Neo] -2: 找不到该网址, bad url
			// [Neo] -6: 未能连接到服务器, https，或者不存在的站点
			// [Neo] -7: 服务器无法通信，稍后重试, 光猫
			// [Neo] -10: 不支持该协议, ftp, mailto

			// [Neo] -12: 网页无效
			// [Neo] -14: 文件不存在
			REPORT(ARG1_RECV_ERROR, errorCode, description + "\n" + failingUrl);
			super.onReceivedError(view, errorCode, description, failingUrl);
		}

		@Override
		public void onReceivedHttpAuthRequest(WebView view,
				HttpAuthHandler handler, String host, String realm) {

			// [Neo] 检查一下 webview.httpauth 里面有木有存储的认证信息
			if (handler.useHttpAuthUsernamePassword()) {
				String[] arrayStrings = webView.getHttpAuthUsernamePassword(
						host, realm);
				if (arrayStrings != null && arrayStrings[0] != null
						&& arrayStrings[1] != null) {
					handler.proceed(arrayStrings[0], arrayStrings[1]);
					return;
				}
			}

			REPORT(ARG1_HTTP_AUTH, 0, new HttpAuthKit(view, host, realm,
					handler));
		}

		// [Neo] TODO after api > 2.1
		// @Override
		// public void onReceivedSslError(WebView view, SslErrorHandler handler,
		// SslError error) {
		// handler.proceed();
		// super.onReceivedSslError(view, handler, error);
		// }

	}

	/**
	 * 打开某个 http 请求的页面 不推荐外面的人用自己的成员的方法
	 * 
	 * @param urlString
	 */
	public void loadURL(String urlString) {
		webView.loadUrl(urlString);
	}

	/**
	 * 如果可以后退，那就后退 不然就返回 false
	 * 
	 */
	public boolean back() {
		if (webView.canGoBack()) {
			webView.goBack();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 退出页面的时候清空缓存
	 * 
	 * @param includeDiskFiles
	 */
	public void clear(boolean includeDiskFiles) {
		webView.removeAllViews();
		webView.clearCache(includeDiskFiles);
		webView.destroy();
	}

	/**
	 * 强制重写 URL
	 * 
	 * @param urlString
	 */
	public void overrideURL(String urlString) {
		webViewClient.shouldOverrideUrlLoading(webView, urlString);
	}

	/**
	 * 汇报给 浏览器活动，静态方法
	 * 
	 * @param arg1
	 * @param arg2
	 * @param obj
	 */
	private static void REPORT(int arg1, int arg2, Object obj) {
		Message message = HANDLER.obtainMessage(WHAT, arg1, arg2, obj);
		HANDLER.sendMessage(message);
	}

	private static class MyWebView extends WebView {

		public MyWebView(Context context) {
			super(context);
		}

		@Override
		public boolean onInterceptTouchEvent(MotionEvent ev) {
			REPORT(ARG1_SCROLL_TO_TOP, getScrollY(), null);
			return super.onInterceptTouchEvent(ev);
		}

	}

	/**
	 * 触发下载事件后传递的工具类
	 * 
	 * @author neo
	 */
	public static class DownloadKit {
		private String url;
		private String userAgent;
		private String contentDisposition;
		private String mimetype;
		private long contentLength;

		/**
		 * 构造
		 * 
		 * @param url
		 *            下载地址
		 * @param userAgent
		 *            用户代理名称
		 * @param contentDisposition
		 *            描述
		 * @param mimetype
		 *            类型
		 * @param contentLength
		 *            长度
		 */
		public DownloadKit(String url, String userAgent,
				String contentDisposition, String mimetype, long contentLength) {
			this.url = url;
			this.userAgent = userAgent;
			this.contentDisposition = contentDisposition;
			this.mimetype = mimetype;
			this.contentLength = contentLength;
		}

		/**
		 * 获取下载地址
		 * 
		 * @return
		 */
		public String getUrl() {
			return url;
		}

		/**
		 * 获取用户代理名称
		 * 
		 * @return
		 */
		public String getUserAgent() {
			return userAgent;
		}

		/**
		 * 获取描述
		 * 
		 * @return
		 */
		public String getContentDisposition() {
			return contentDisposition;
		}

		/**
		 * 获取类型
		 * 
		 * @return
		 */
		public String getMimetype() {
			return mimetype;
		}

		/**
		 * 获取文件长度
		 * 
		 * @return
		 */
		public long getContentLength() {
			return contentLength;
		}

		@Override
		public String toString() {
			return ("url: " + url + ", ua: " + userAgent + ", disposition: "
					+ contentDisposition + ", mime: " + mimetype + ", length: " + contentLength);
		}

	}

	/**
	 * 触发用户认证页面时传递的工具类
	 * 
	 * @author neo
	 */
	public static class HttpAuthKit {
		private WebView webView;
		private String host;
		private String realm;
		private HttpAuthHandler handler;

		/**
		 * 构造
		 * 
		 * @param webView
		 *            视图类对象
		 * @param host
		 *            主机名
		 * @param realm
		 *            标识
		 * @param handler
		 *            处理对象
		 */
		public HttpAuthKit(WebView webView, String host, String realm,
				HttpAuthHandler handler) {
			this.webView = webView;
			this.host = host;
			this.realm = realm;
			this.handler = handler;
		}

		/**
		 * 获取视图类对象
		 * 
		 * @return
		 */
		public WebView getWebView() {
			return webView;
		}

		/**
		 * 获取主机名
		 * 
		 * @return
		 */
		public String getHost() {
			return host;
		}

		/**
		 * 获取标识
		 * 
		 * @return
		 */
		public String getRealm() {
			return realm;
		}

		/**
		 * 获取处理类对象
		 * 
		 * @return
		 */
		public HttpAuthHandler getHandler() {
			return handler;
		}

		@Override
		public String toString() {
			return ("webView: " + webView.toString() + ", host: " + host
					+ ", realm: " + realm + ", " + handler.toString());
		}

	}

}
