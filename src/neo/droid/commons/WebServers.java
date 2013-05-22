package neo.droid.commons;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.net.URLConnection;
import java.net.URLDecoder;

import neo.java.commons.Files;
import neo.java.commons.Strings;
import neo.java.commons.TFTPServer;

import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpServerConnection;
import org.apache.http.HttpStatus;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * WebService 一个简单的 http 服务器
 * 
 * @author neo
 */
public class WebServers extends Service {

	private Daemon daemon;
	private static String DOC_ROOT;
	private static final String INFO = "neo.droid.webserver/0.1";
	private static final String SUF_DEL = "&delete";

	public static final int DEFAULT_PORT = 8000;

	private static TFTPServer tftpServer;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		daemon.close();

		// [Neo] TODO
		System.out.println("tftp stop: " + tftpServer.stop());

		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Resource.make(WebServers.this);
		DOC_ROOT = intent.getStringExtra("root");
		Resource.cpAssetFileTo(DOC_ROOT);

		// [Neo] TODO
		tftpServer = new TFTPServer(DOC_ROOT);
		System.out.println("tftp start: " + tftpServer.start());

		daemon = new Daemon(intent.getIntExtra("port", DEFAULT_PORT));
		daemon.setDaemon(true);
		daemon.start();
		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * 守护进程
	 * 
	 * @author neo
	 */
	private static class Daemon extends Thread {
		private int port;
		private boolean isQuit;

		public Daemon(int port) {
			this.port = port;
		}

		public void close() {
			isQuit = true;
		}

		@Override
		public void run() {
			ServerSocket socket = null;
			try {
				socket = new ServerSocket(port);
				BasicHttpProcessor httpProcessor = new BasicHttpProcessor();
				httpProcessor.addInterceptor(new ResponseDate());
				httpProcessor.addInterceptor(new ResponseServer());
				httpProcessor.addInterceptor(new ResponseContent());
				httpProcessor.addInterceptor(new ResponseConnControl());

				HttpService httpService = new HttpService(httpProcessor,
						new DefaultConnectionReuseStrategy(),
						new DefaultHttpResponseFactory());

				HttpParams params = new BasicHttpParams();
				params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT,
						10 * 1000)
						.setIntParameter(
								CoreConnectionPNames.SOCKET_BUFFER_SIZE,
								8 * 1024)
						.setBooleanParameter(
								CoreConnectionPNames.STALE_CONNECTION_CHECK,
								false)
						.setBooleanParameter(CoreConnectionPNames.TCP_NODELAY,
								true)
						.setParameter(CoreProtocolPNames.ORIGIN_SERVER, INFO);
				httpService.setParams(params);

				// [Neo] 监听所有文件链接以及定制的删除操作
				HttpRequestHandlerRegistry reqistry = new HttpRequestHandlerRegistry();
				reqistry.register("*", new HttpFileHandler());
				reqistry.register("*" + SUF_DEL, new HttpDeleteHandler());
				httpService.setHandlerResolver(reqistry);

				isQuit = false;
				while (false == isQuit && false == Thread.interrupted()) {
					DefaultHttpServerConnection connection = new DefaultHttpServerConnection();
					connection.bind(socket.accept(), params);

					// [Neo] 把干活的叫起来
					Worker worker = new Worker(httpService, connection);
					worker.setDaemon(true);
					worker.start();
				}

			} catch (Exception e) {
				isQuit = true;
				e.printStackTrace();
			} finally {
				try {
					if (null != socket) {
						socket.close();
					}
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}
	}

	/**
	 * 处置请求的工组类
	 * 
	 * @author neo
	 */
	private static class Worker extends Thread {

		private HttpService httpService;
		private HttpServerConnection connection;

		public Worker(HttpService httpService, HttpServerConnection connection) {
			this.httpService = httpService;
			this.connection = connection;
		}

		@Override
		public void run() {
			HttpContext httpContext = new BasicHttpContext();
			try {
				while (false == Thread.interrupted()
						&& false != connection.isOpen()) {
					httpService.handleRequest(connection, httpContext);
				}
			} catch (SocketTimeoutException e) {
				// [Neo] socket 超时的异常不用显示
			} catch (ConnectionClosedException e) {
				// [Neo] 客户端切断连接的异常也不用显示
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					connection.shutdown();
				} catch (Exception e2) {
					// [Neo] Empty
				}
			}
		}
	}

	/**
	 * 针对文件的请求处理
	 * 
	 * @author neo
	 */
	private static class HttpFileHandler implements HttpRequestHandler {

		@Override
		public void handle(HttpRequest request, HttpResponse response,
				HttpContext context) throws HttpException, IOException {

			String target = URLDecoder.decode(
					request.getRequestLine().getUri(), "UTF-8");
			File file = new File(DOC_ROOT, target);

			HttpEntity entity = null;

			if (false == file.exists()) {
				response.setStatusCode(HttpStatus.SC_NOT_FOUND);
				entity = new StringEntity("" + HttpStatus.SC_NOT_FOUND, "UTF-8");
				response.setHeader("Content-Type", "text/html");
				response.setEntity(entity);
			} else if (false == file.canRead()) {
				response.setStatusCode(HttpStatus.SC_FORBIDDEN);
				entity = new StringEntity("" + HttpStatus.SC_FORBIDDEN, "UTF-8");
				response.setHeader("Content-Type", "text/html");
				response.setEntity(entity);
			} else {
				response.setStatusCode(HttpStatus.SC_OK);

				if (false == file.isDirectory()) {
					String contentType = URLConnection
							.guessContentTypeFromName(file.getAbsolutePath());
					contentType = (null == contentType) ? "charset=UTF-8"
							: contentType + "; charset=UTF-8";
					entity = new FileEntity(file, contentType);
					response.setHeader("Content-Type", contentType);
				} else {
					StringBuilder sBuilder = new StringBuilder();
					sBuilder.append("<html>\n<head>\n\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n\t<link rel=\"icon\" type=\"image/x-icon\" href=\"favicon.ico\" />\n\t<link rel=\"shortcut icon\" type=\"image/x-icon\" href=\"favicon.ico\" />\n</head>\n");
					sBuilder.append("<body>\n\t<h1>"
							+ ((null == target) ? file.getAbsolutePath()
									: target) + "</h1>\n\t<table>\n");
					File[] files = file.listFiles();
					if (null != files && files.length > 0) {
						Files.sort(files);
						for (File f : files) {
							sBuilder.append(addFile(f));
						}
					}
					if (false == file.getPath().equals(DOC_ROOT)) {
						sBuilder.append("\t\t<tr><p><a href=\"..\">Back</a></p></tr>\n");
					}
					sBuilder.append("\t</table>\n\t<hr />" + INFO
							+ "\n</body>\n</html>\n");
					entity = new StringEntity(sBuilder.toString(), "UTF-8");
					response.setHeader("Content-Type", "text/html");
				}

				response.setEntity(entity);
			}
		}
	}

	/**
	 * 删除文件的处理
	 * 
	 * @author neo
	 */
	private static class HttpDeleteHandler implements HttpRequestHandler {

		@Override
		public void handle(HttpRequest request, HttpResponse response,
				HttpContext context) throws HttpException, IOException {

			String target = URLDecoder.decode(request.getRequestLine().getUri()
					.replace(SUF_DEL, ""), "UTF-8");
			File file = new File(DOC_ROOT, target);

			String alertString = "";
			if (false != Files.delete(file)) {
				alertString = "<script>alert(\"Delete success!\");window.location.href=\""
						+ target.substring(0, target.lastIndexOf("/") + 1)
						+ "\";</script>";
			} else {
				alertString = "<script>alert(\"Delete failed!\");window.location.href=\""
						+ target.substring(0, target.lastIndexOf("/") + 1)
						+ "\";</script>";
			}

			StringEntity entity = new StringEntity(alertString, "UTF-8");
			response.setStatusCode(HttpStatus.SC_OK);
			response.setHeader("Content-Type", "text/html");
			response.setEntity(entity);
		}
	}

	/**
	 * 添加要显示的文件信息
	 * 
	 * @param file
	 * @return
	 */
	private static String addFile(File file) {
		if (false != file.isDirectory()) {
			return ("\t\t<tr><td colspan=\"2\"><a href=\""
					+ file.getName()
					+ "/\">[+]"
					+ file.getName()
					+ "</a></td><td>"
					+ Strings.getTimeStringFromStamp(file.lastModified(),
							"yyyy-MM-dd hh:mm:ss") + "</td><td><a href=\""
					+ file.getName() + SUF_DEL + "\">Delete</a></td></tr>\n");
		} else {
			return ("\t\t<tr><td><a href=\""
					+ file.getName()
					+ "\">"
					+ file.getName()
					+ "</a></td><td>"
					+ Files.formatFileSize(file.length(), "%.2f ")
					+ "</td><td>"
					+ Strings.getTimeStringFromStamp(file.lastModified(),
							"yyyy-MM-dd hh:mm:ss") + "</td><td><a href=\""
					+ file.getName() + SUF_DEL + "\">Delete</a></td></tr>\n");
		}
	}

}
