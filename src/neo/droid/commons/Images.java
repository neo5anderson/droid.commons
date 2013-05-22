package neo.droid.commons;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;

import neo.java.commons.Nets;
import neo.java.commons.Strings;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;
import android.os.Build;

/**
 * 通用图形处理工具类
 * 
 * @author neo
 */
public class Images {

	/** 通用的比特图的选项对象 */
	private static BitmapFactory.Options BITMAP_FACTORY_OPTIONS;

	/**
	 * 获取一个备用的比特图选项对象
	 * 
	 * @return 选项对象
	 */
	private static BitmapFactory.Options getOptions() {
		if (null == BITMAP_FACTORY_OPTIONS) {
			BITMAP_FACTORY_OPTIONS = new BitmapFactory.Options();
			BITMAP_FACTORY_OPTIONS.inPurgeable = true;
			if (Build.VERSION.SDK_INT < 14) {
				try {
					// [Neo] 可能会有安全异常
					BitmapFactory.Options.class.getField("inNativeAlloc")
							.setBoolean(BITMAP_FACTORY_OPTIONS, true);
				} catch (Exception e) {
					// [Neo] Empty
				}
			}
		}

		return BITMAP_FACTORY_OPTIONS;
	}

	/**
	 * 通过路径获取比特图
	 * 
	 * @param filePath
	 *            文件路径
	 * @return 比特图对象
	 */
	public static Bitmap decodeFile(String filePath) {
		if (false == Strings.isEmpty(filePath)) {
			return BitmapFactory.decodeFile(filePath, getOptions());
		} else {
			return null;
		}
	}

	/**
	 * 从资源中获取比特图
	 * 
	 * @param resID
	 *            资源 ID
	 * @return 比特图对象
	 */
	public static Bitmap decodeResource(int resID) {
		return BitmapFactory.decodeResource(Resource.getResources(), resID,
				getOptions());
	}

	/**
	 * 通过 URL 获取比特图
	 * 
	 * @param url
	 *            图像地址
	 * @return 比特图对象
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static Bitmap decodeURL(String url) throws MalformedURLException,
			IOException {
		// [Neo] TODO
		// return BitmapFactory.decodeStream(new URL(url).openStream(), null,
		// getOptions());
		return BitmapFactory.decodeStream(
				Nets.getInputStreamFromURL(url, Strings.UTF_8), null,
				getOptions());
	}

	/**
	 * 从 byte 数组转化成比特图
	 * 
	 * @param bytes
	 *            字节类型数组
	 * @return 比特图对象
	 */
	public static Bitmap decodeBytes(byte[] bytes) {
		if (0 != bytes.length) {
			return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
		} else {
			return null;
		}
	}

	/**
	 * 将比特图转化成 byte 数组
	 * 
	 * @param bitmap
	 *            比特图对象
	 * @return 字节数组
	 */
	public static byte[] save2bytes(Bitmap bitmap) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream);
		return outputStream.toByteArray();
	}

	/**
	 * 由 Drawable 转换成比特图
	 * 
	 * @param drawable
	 *            Drawable 对象
	 * @return 比特图对象
	 */
	public static Bitmap decodeDrawable(Drawable drawable) {
		final int width = drawable.getIntrinsicWidth();
		final int height = drawable.getIntrinsicHeight();

		Bitmap bitmap = Bitmap.createBitmap(width, height, drawable
				.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565);

		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, width, height);
		drawable.draw(canvas);
		return bitmap;
	}

	/**
	 * 从资源中获取 Drawable 对象
	 * 
	 * @param resID
	 *            资源 ID
	 * @return Drawable 对象
	 * @throws NullPointerException
	 */
	public static Drawable getDrawable(int resID) throws NullPointerException {
		if (null == Resource.CONTEXT) {
			return null;
		}

		return Resource.getResources().getDrawable(resID);
	}

	/**
	 * 修改比特图的尺寸
	 * 
	 * @param bitmap
	 *            待修改的比特图对象
	 * @param width
	 *            长
	 * @param height
	 *            高
	 * @return 修改后的比特图对象
	 */
	public static Bitmap resizeBitmap(Bitmap bitmap, int width, int height) {
		final float scaleWidht = ((float) width / bitmap.getWidth());
		final float scaleHeight = ((float) height / bitmap.getHeight());

		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidht, scaleHeight);

		Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
				bitmap.getHeight(), matrix, true);

		return newBitmap;
	}

	/**
	 * 水平方向合并比特图
	 * 
	 * @param leftBitmap
	 *            左边的图
	 * @param rightBitmap
	 *            右边的图
	 * @param isAutoMargin
	 *            是否自动居中对齐
	 * @return 合并后的比特图对象
	 */
	public static Bitmap joinHorizontal(Bitmap leftBitmap, Bitmap rightBitmap,
			boolean isAutoMargin) {
		final int width = leftBitmap.getWidth() + rightBitmap.getWidth();
		final int height = Math.max(leftBitmap.getHeight(),
				rightBitmap.getHeight());

		Bitmap joinedBitmap = Bitmap.createBitmap(width, height,
				Config.ARGB_8888);
		Canvas canvas = new Canvas(joinedBitmap);
		if (false != isAutoMargin) {
			canvas.drawBitmap(leftBitmap, 0,
					(height - leftBitmap.getHeight()) / 2.0f, null);
			canvas.drawBitmap(rightBitmap, leftBitmap.getWidth(),
					(height - rightBitmap.getHeight()) / 2.0f, null);
		} else {
			canvas.drawBitmap(leftBitmap, 0, 0, null);
			canvas.drawBitmap(rightBitmap, leftBitmap.getWidth(), 0, null);
		}

		return joinedBitmap;
	}

	/**
	 * 垂直方向合并比特图
	 * 
	 * @param topBitmap
	 *            上面的图形
	 * @param bottomBitmap
	 *            下面的图形
	 * @param isAutoMargin
	 *            是否自动居中
	 * @return 合并后的比特图
	 */
	public static Bitmap joinVertical(Bitmap topBitmap, Bitmap bottomBitmap,
			boolean isAutoMargin) {
		final int width = Math.max(topBitmap.getHeight(),
				bottomBitmap.getHeight());
		final int height = topBitmap.getHeight() + bottomBitmap.getHeight();

		Bitmap joinedBitmap = Bitmap.createBitmap(width, height,
				Config.ARGB_8888);
		Canvas canvas = new Canvas(joinedBitmap);
		if (false != isAutoMargin) {
			canvas.drawBitmap(topBitmap, (width - topBitmap.getWidth()) / 2.0f,
					0, null);
			canvas.drawBitmap(bottomBitmap,
					(width - bottomBitmap.getWidth()) / 2.0f,
					topBitmap.getHeight(), null);
		} else {
			canvas.drawBitmap(topBitmap, 0, 0, null);
			canvas.drawBitmap(bottomBitmap, 0, topBitmap.getHeight(), null);
		}
		return joinedBitmap;
	}

	/**
	 * 生成圆角的比特图
	 * 
	 * @param bitmap
	 *            待处理的比特图
	 * @param roundPixel
	 *            圆角的像素大小
	 * @return 处理后的比特图对象
	 */
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPixel) {
		Bitmap roundedBitmap = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		Canvas canvas = new Canvas(roundedBitmap);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		canvas.drawRoundRect(new RectF(rect), roundPixel, roundPixel, paint);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		return roundedBitmap;
	}

	// int reflectionMarginTop =

	/**
	 * 生成有阴影的比特图
	 * 
	 * @param bitmap
	 *            待处理的比特图
	 * @param heightStart
	 *            阴影开始位置
	 * @param heightEnd
	 *            阴影结束位置
	 * @param reflectionMarginTop
	 *            阴影与图形之间的间隔，比如 (int) Math.min(Math.max(bitmap.getHeight *
	 *            0.03f, 3), 5);
	 * @return 处理后的比特图对象
	 */
	public static Bitmap getReflectedBitmap(Bitmap bitmap, int heightStart,
			int heightEnd, int reflectionMarginTop) {

		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		if (heightStart < 0)
			heightStart = 0;

		if (heightEnd > height)
			heightEnd = height;

		if (heightEnd <= heightStart)
			heightEnd = heightStart + 1;

		// [Neo] 倒置图像
		Matrix matrix = new Matrix();
		matrix.preScale(1, -1);

		// [Neo] 阴影布局的 Bitmap
		Bitmap reflectionBitmap = Bitmap.createBitmap(bitmap, 0, heightStart,
				width, heightEnd - heightStart, matrix, false);

		// [Neo] 原图与阴影组合后的 Bitmap
		Bitmap fullBitmap = Bitmap.createBitmap(width, height + heightEnd
				- heightStart, Config.ARGB_8888);

		Canvas canvas = new Canvas(fullBitmap);

		// [Neo] 绘制原图
		canvas.drawBitmap(bitmap, 0, 0, null);

		// [Neo] 设置阴影颜色梯度
		LinearGradient gradient = new LinearGradient(0, bitmap.getHeight(), 0,
				fullBitmap.getHeight() + reflectionMarginTop, 0x80FFFFFF,
				0x30FFFFFF, TileMode.CLAMP);

		Paint paint = new Paint();
		paint.setShader(gradient);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

		// [Neo] 绘制阴影
		canvas.drawBitmap(reflectionBitmap, 0, height + reflectionMarginTop,
				null);
		canvas.drawRect(0, height, width, fullBitmap.getHeight()
				+ reflectionMarginTop, paint);

		return fullBitmap;
	}

	/**
	 * 旋转比特图
	 * 
	 * @param bitmap
	 *            待处理的比特图
	 * @param offsetLeft
	 *            图像左偏移
	 * @param offsetTop
	 *            图像上偏移
	 * @param newWidth
	 *            生成图形新长度
	 * @param newHeight
	 *            生成图形新高度
	 * @param degrees
	 *            旋转角度
	 * @param truningX
	 *            画板旋转 X 坐标
	 * @param truningY
	 *            画板旋转 Y 坐标
	 * @return 处理后的比特图对象
	 */
	public static Bitmap trunAround(Bitmap bitmap, int offsetLeft,
			int offsetTop, int newWidth, int newHeight, float degrees,
			float truningX, float truningY) {
		Bitmap newBitmap = Bitmap.createBitmap(newWidth, newHeight,
				Config.ARGB_8888);
		Canvas canvas = new Canvas(newBitmap);
		canvas.save();
		canvas.rotate(degrees, truningX, truningY);
		canvas.drawBitmap(bitmap, truningX - offsetLeft, truningY - offsetTop,
				new Paint());
		canvas.restore();
		return newBitmap;
	}

	/**
	 * 另外一种高效的从文件获取比特图方案
	 * 
	 * @param filename
	 *            文件名
	 * @param reqWidth
	 *            目标长度
	 * @param reqHeight
	 *            目标高度
	 * @param strictInSampleSize
	 *            是否严格规定尺寸
	 * @return 处理后的比特图
	 */
	public static synchronized Bitmap decodeSampledBitmapFromFile(
			String filename, int reqWidth, int reqHeight,
			boolean strictInSampleSize) {
		try {
			// First decode with inJustDecodeBounds=true to check dimensions
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(filename, options);

			// Calculate inSampleSize
			options.inSampleSize = calculateInSampleSize(options, reqWidth,
					reqHeight, strictInSampleSize);

			// Decode bitmap with inSampleSize set
			options.inJustDecodeBounds = false;

			return BitmapFactory.decodeFile(filename, options);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			// System.runFinalizersOnExit(true);
			System.exit(0);
			return null;
		}
	}

	/**
	 * 计算尺寸的算法
	 * 
	 * @param options
	 *            选项
	 * @param reqWidth
	 *            指定的长度
	 * @param reqHeight
	 *            指定的高度
	 * @param strictInSampleSize
	 *            是否严格指定尺寸
	 * @return 整数计算值
	 */
	private static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight, boolean strictInSampleSize) {
		if (reqWidth <= 0 && reqHeight <= 0) {
			return 1;
		}
		if (options.outWidth <= 0 || options.outHeight <= 0) {
			return 1;
		}
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (reqWidth <= 0) {
			reqWidth = Math.round(((float) width / (float) height) * reqHeight);
		} else if (reqHeight <= 0) {
			reqHeight = Math.round(((float) height / (float) width) * reqWidth);
		}

		if (height > reqHeight || width > reqWidth) {
			if (width > height) {
				inSampleSize = strictInSampleSize ? Math.round((float) height
						/ (float) reqHeight) : height / reqHeight;
			} else {
				inSampleSize = strictInSampleSize ? Math.round((float) width
						/ (float) reqWidth) : width / reqWidth;
			}
			if (strictInSampleSize) {
				final float totalPixels = width * height;
				final float totalReqPixelsCap = reqWidth * reqHeight * 2;
				while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
					inSampleSize++;
				}
			}
		}
		return inSampleSize;
	}
}
