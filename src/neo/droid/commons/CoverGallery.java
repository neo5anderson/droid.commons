package neo.droid.commons;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.View;
import android.view.animation.Transformation;
import android.widget.Gallery;
import android.widget.ImageView;

/**
 * 定制过的画廊类
 * 
 * @author neo
 */
public class CoverGallery extends Gallery {

	/** Gallery 的中间位置 */
	private int mGalleryCenter;
	/** 是否开启环绕模式 */
	private static boolean isCircleMode = false;
	/** 是否开启半透明模式 */
	private static boolean isAlphaMode = false;

	/** 类似于 Canvas 画布 */
	private Camera camera = new Camera();

	/** Gallery 构造 */
	public CoverGallery(Context context) {
		super(context);
		setStaticTransformationsEnabled(true);
		setSelection(Integer.MAX_VALUE / 2 / 10 * 10);
	}

	/**
	 * 判断是否启用循环模式
	 * 
	 * @return
	 */
	public static boolean isCircleMode() {
		return isCircleMode;
	}

	/**
	 * 设置使用循环模式
	 * 
	 * @param isCircleMode
	 */
	public static void setCircleMode(boolean isCircleMode) {
		CoverGallery.isCircleMode = isCircleMode;
	}

	/**
	 * 是否有透明效果
	 * 
	 * @return
	 */
	public static boolean isAlphaMode() {
		return isAlphaMode;
	}

	/**
	 * 设置是否使用透明效果
	 * 
	 * @param isAlphaMode
	 */
	public static void setAlphaMode(boolean isAlphaMode) {
		CoverGallery.isAlphaMode = isAlphaMode;
	}

	@Override
	protected boolean getChildStaticTransformation(View child, Transformation t) {

		int angle = 0;
		int childWidth = child.getWidth();
		int childCenter = child.getLeft() + childWidth / 2;

		t.clear();
		// [Neo] alpha = 1
		t.setTransformationType(Transformation.TYPE_MATRIX);

		if (mGalleryCenter == childCenter) {
			transferImage(child, t, 0);
		} else {
			// [Neo] 调整角度
			angle = (int) (((float) (mGalleryCenter - childCenter) / mGalleryCenter) * 60);

			if (Math.abs(angle) > 60) {
				angle = (angle > 0) ? 60 : -60;
			}

			transferImage(child, t, angle);
		}

		return true;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// [Neo] 一般情况都是屏幕的中间
		mGalleryCenter = (getWidth() - getPaddingLeft() - getPaddingRight())
				/ 2 + getPaddingLeft();

		super.onSizeChanged(w, h, oldw, oldh);
	}

	/**
	 * 变换图形
	 * 
	 * @param child
	 * @param t
	 * @param angle
	 */
	private void transferImage(View child, Transformation t, int angle) {
		Matrix imageMatrix = t.getMatrix();
		int imageHeight = child.getLayoutParams().height;
		int imageWidth = child.getLayoutParams().width;

		// [Neo] 绝对角度 最大 60
		int absAngle = Math.abs(angle);

		camera.save();
		camera.translate(0, 0, (float) (absAngle * 2 - 400));

		if (isAlphaMode) {
			((ImageView) (child)).setAlpha((int) (255 - absAngle * 1.5));
		}

		camera.rotateY(angle);
		camera.getMatrix(imageMatrix);
		imageMatrix.preTranslate(-imageWidth / 2, -imageHeight / 2);
		imageMatrix.postTranslate(imageWidth / 2, imageHeight / 2);
		camera.restore();
	}

}
