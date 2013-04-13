package neo.java.commons;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.io.ToNetASCIIInputStream;
import org.apache.commons.net.io.ToNetASCIIOutputStream;
import org.apache.commons.net.tftp.TFTP;
import org.apache.commons.net.tftp.TFTPAckPacket;
import org.apache.commons.net.tftp.TFTPDataPacket;
import org.apache.commons.net.tftp.TFTPErrorPacket;
import org.apache.commons.net.tftp.TFTPPacket;
import org.apache.commons.net.tftp.TFTPReadRequestPacket;
import org.apache.commons.net.tftp.TFTPWriteRequestPacket;

public class TFTPServer {

	public static final int DEFAULT_PORT = 69;
	public static final int ACCESS_WRITE = 0x01;
	public static final int ACCESS_READ = 0x10;

	private static final int BLOCK_WRAP = 64 * 1024;

	private static int PORT;
	private static int ACCESS;
	private static String WORK_FOLDER;
	private static String SERVER_FOLDER;

	private Daemon daemon = null;

	private static List<TFTPHandler> HANDLERS;

	public TFTPServer() {
		PORT = DEFAULT_PORT;
		ACCESS = ACCESS_READ | ACCESS_WRITE;
		WORK_FOLDER = ".";
		SERVER_FOLDER = ".";
	}

	public TFTPServer(String workFolder) {
		PORT = DEFAULT_PORT;
		ACCESS = ACCESS_READ | ACCESS_WRITE;
		WORK_FOLDER = workFolder;
		SERVER_FOLDER = workFolder;
	}

	public TFTPServer(int port, int access, int timeout, String workFolder,
			String serverFolder) {
		PORT = port;
		ACCESS = access;
		WORK_FOLDER = workFolder;
		SERVER_FOLDER = serverFolder;
	}

	public boolean start() {
		if (null == daemon) {
			HANDLERS = new ArrayList<TFTPServer.TFTPHandler>();
			daemon = new Daemon();
			daemon.setDaemon(false);
			daemon.start();
			return true;
		}
		return false;
	}

	public boolean stop() {
		if (null != daemon) {
			for (int i = 0; i < HANDLERS.size(); i++) {
				HANDLERS.get(i).quit();
			}
			daemon.quit();
			daemon = null;
			return true;
		}
		return false;
	}

	private static class Daemon extends Thread {

		private TFTP tftp;
		private boolean isQuit;

		public Daemon() {
			isQuit = false;
			tftp = new TFTP();
			try {
				tftp.open(TFTPServer.PORT);
				// tftp.setSoTimeout(TFTPServer.TIMEOUT);
			} catch (SocketException e) {
				e.printStackTrace();
			}
		}

		public void quit() {
			try {
				if (null != tftp) {
					tftp.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			isQuit = true;
		}

		@Override
		public void run() {
			while (false == isQuit) {
				try {
					TFTPHandler handler = null;
					TFTPPacket packet = tftp.receive();
					if (packet instanceof TFTPReadRequestPacket) {
						handler = new TFTPReadHandler(packet);
					} else if (packet instanceof TFTPWriteRequestPacket) {
						handler = new TFTPWriteHandler(packet);
					}
					if (null != handler) {
						handler.setDaemon(true);
						handler.start();
						HANDLERS.add(handler);
					}

				} catch (SocketTimeoutException e) {
					// [Neo] Empty
				} catch (SocketException e) {
					e.printStackTrace();
					quit();
					break;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

	private static abstract class TFTPHandler extends Thread {

		protected TFTP tftp;
		protected TFTPPacket packet;
		protected boolean isQuit;

		public TFTPHandler(TFTPPacket packet) {
			tftp = new TFTP();
			this.packet = packet;
			this.isQuit = false;
		}

		public void quit() {
			isQuit = true;
		}

		@Override
		public void run() {
			try {
				tftp.open();
				// tftp.setSoTimeout(TFTPServer.TIMEOUT);
				tftp.beginBufferedOps();
			} catch (Exception e) {
				e.printStackTrace();
				tftp.endBufferedOps();
				tftp.close();
			}

			if (false == isQuit) {
				try {
					handle();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					tftp.close();
				}
			}

		}

		protected abstract void handle();

		protected File createFile(String baseFolder, String fileName)
				throws IOException {
			File folder = new File(baseFolder);
			if (false == folder.exists()) {
				folder.mkdirs();
			} else {
				if (false == folder.isDirectory()) {
					folder.delete();
					folder.mkdirs();
				}
			}

			File file = new File(folder, fileName);
			file.createNewFile();
			return file;
		}
	}

	private static class TFTPReadHandler extends TFTPHandler {

		public TFTPReadHandler(TFTPPacket packet) {
			super(packet);
		}

		@Override
		protected void handle() {
			InputStream inputStream = null;
			TFTPReadRequestPacket readRequestPacket = (TFTPReadRequestPacket) packet;

			switch (TFTPServer.ACCESS) {
			case TFTPServer.ACCESS_WRITE:
			case (TFTPServer.ACCESS_READ | TFTPServer.ACCESS_WRITE):
				try {
					inputStream = new BufferedInputStream(new FileInputStream(
							createFile(TFTPServer.WORK_FOLDER,
									readRequestPacket.getFilename())));
					if (TFTP.ASCII_MODE == readRequestPacket.getMode()) {
						inputStream = new ToNetASCIIInputStream(inputStream);
					}

					TFTPPacket responsePacket = null;
					TFTPDataPacket dataPacket = null;

					byte[] buffer = new byte[TFTPDataPacket.MAX_DATA_LENGTH];
					int perLength = inputStream.read(buffer);

					int blockNumber = 1;
					int socketTimeoutCount = 0;
					boolean hasSended = true;

					while (false == isQuit && -1 != perLength) {
						if (false != hasSended) {
							dataPacket = new TFTPDataPacket(
									readRequestPacket.getAddress(),
									readRequestPacket.getPort(), blockNumber,
									buffer, 0, perLength);

							hasSended = false;
						}

						tftp.bufferedSend(dataPacket);

						socketTimeoutCount = 0;
						responsePacket = null;

						while (false == isQuit && null == responsePacket) {
							try {
								responsePacket = tftp.bufferedReceive();
								if (null == responsePacket) {
									continue;
								}
								if (false == (responsePacket.getAddress()
										.equals(readRequestPacket.getAddress()) && responsePacket
										.getPort() == readRequestPacket
										.getPort())) {
									responsePacket = null;
									continue;
								}

								if (responsePacket instanceof TFTPAckPacket) {
									if (blockNumber == ((TFTPAckPacket) responsePacket)
											.getBlockNumber()) {
										blockNumber = (blockNumber + 1)
												% TFTPServer.BLOCK_WRAP;
										hasSended = true;
									}
								}

							} catch (SocketTimeoutException e) {
								socketTimeoutCount++;

								if (socketTimeoutCount > 3) {
									break;
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}

						perLength = inputStream.read(buffer);

					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (null != inputStream) {
						try {
							inputStream.close();
						} catch (Exception e2) {
							e2.printStackTrace();
						}
					}
				}

				break;

			case TFTPServer.ACCESS_READ:
			default:
				try {
					tftp.bufferedSend(new TFTPErrorPacket(tftp
							.getLocalAddress(), tftp.getLocalPort(),
							TFTPErrorPacket.ILLEGAL_OPERATION,
							"read-only not allowed"));
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}

		}
	}

	private static class TFTPWriteHandler extends TFTPHandler {

		public TFTPWriteHandler(TFTPPacket packet) {
			super(packet);
		}

		@Override
		protected void handle() {
			OutputStream outputStream = null;
			TFTPWriteRequestPacket writeRequestPacket = (TFTPWriteRequestPacket) packet;

			switch (TFTPServer.ACCESS) {
			case TFTPServer.ACCESS_WRITE:
			case (TFTPServer.ACCESS_WRITE | TFTPServer.ACCESS_READ):
				int blockNumber = 0;

				try {
					outputStream = new BufferedOutputStream(
							new FileOutputStream(createFile(
									TFTPServer.SERVER_FOLDER,
									writeRequestPacket.getFilename())));
					if (TFTP.ASCII_MODE == writeRequestPacket.getMode()) {
						outputStream = new ToNetASCIIOutputStream(outputStream);
					}

					TFTPAckPacket requestPacket = new TFTPAckPacket(
							writeRequestPacket.getAddress(),
							writeRequestPacket.getPort(), blockNumber);
					tftp.bufferedSend(requestPacket);

					int socketTimeoutCount = 0;
					TFTPPacket responsePacket = null;

					while (true) {
						responsePacket = null;
						// [Neo] get the right package
						while (false == isQuit
								&& (null == responsePacket
										|| false == responsePacket.getAddress()
												.equals(writeRequestPacket
														.getAddress()) || responsePacket
										.getPort() != writeRequestPacket
										.getPort())) {

							if (null != responsePacket) {
								tftp.bufferedSend(new TFTPErrorPacket(
										writeRequestPacket.getAddress(),
										writeRequestPacket.getPort(),
										TFTPErrorPacket.UNKNOWN_TID, "I want "
												+ writeRequestPacket
														.getAddress()
														.getHostAddress() + ":"
												+ writeRequestPacket.getPort()));
							}

							try {
								responsePacket = tftp.bufferedReceive();
							} catch (Exception e) {
								e.printStackTrace();
								socketTimeoutCount++;

								tftp.bufferedSend(requestPacket);
								if (socketTimeoutCount > 3) {
									throw e;
								}
							}

						}

						if (responsePacket instanceof TFTPWriteRequestPacket) {
							blockNumber = 0;
							requestPacket = new TFTPAckPacket(
									writeRequestPacket.getAddress(),
									writeRequestPacket.getPort(), blockNumber);
							tftp.bufferedSend(requestPacket);
							continue;
						}

						if (responsePacket instanceof TFTPDataPacket) {
							TFTPDataPacket dataPacket = (TFTPDataPacket) responsePacket;

							int block = dataPacket.getBlockNumber();
							int length = dataPacket.getDataLength();
							int offset = dataPacket.getDataOffset();
							byte[] buffer = dataPacket.getData();

							if ((blockNumber + 1) % TFTPServer.BLOCK_WRAP == block) {
								blockNumber = block;
								outputStream.write(buffer, offset, length);
								requestPacket = new TFTPAckPacket(
										writeRequestPacket.getAddress(),
										writeRequestPacket.getPort(),
										blockNumber);
							}

							tftp.bufferedSend(requestPacket);

							if (length < TFTPDataPacket.MAX_DATA_LENGTH) {
								outputStream.close();
								break;
							}

						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (null != outputStream) {
						try {
							outputStream.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}

				break;

			case TFTPServer.ACCESS_READ:
			default:
				try {
					tftp.bufferedSend(new TFTPErrorPacket(writeRequestPacket
							.getAddress(), writeRequestPacket.getPort(),
							TFTPErrorPacket.ILLEGAL_OPERATION,
							"write not allowed"));
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}

		}

	}

}
