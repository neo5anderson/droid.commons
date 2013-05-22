package neo.droid.commons;

import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

/**
 * 给 Gallery 用的适配器
 * 
 * @author neo
 */
public class CoverGalleryAdapter extends BaseAdapter {

	/** 图像资源标识的数组 */
	private int[] drawableArrays;
	private int phoneWidth;

	/** 获取到图像资源标识的数组的构造方法 */
	public CoverGalleryAdapter(int[] arrays, int phoneWidth) {
		super();
		drawableArrays = arrays;
		this.phoneWidth = phoneWidth;
	}

	@Override
	public int getCount() {
		if (CoverGallery.isCircleMode()) {
			return Integer.MAX_VALUE;
		} else {
			return drawableArrays.length;
		}
	}

	@Override
	public Object getItem(int position) {
		return drawableArrays[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		Bitmap bitmap = Images.decodeResource(drawableArrays[position
				% drawableArrays.length]);

		// [Neo] 制作倒影效果的 ImageView
		ImageView imageView = Resource.getImageViewAttachedBitmap(Images
				.getReflectedBitmap(bitmap, (int) (bitmap.getHeight() * 0.4f),
						bitmap.getHeight(), (int) Math.min(
								Math.max(bitmap.getHeight() * 0.03f, 3), 5)));

		imageView.setScaleType(ScaleType.CENTER_INSIDE);
		// [Neo] 这个布局会稍微好看一些，注意，这里的单位是 dip
		imageView.setLayoutParams(new Gallery.LayoutParams(
				(int) (phoneWidth * 0.375), (int) (phoneWidth * 0.25)));

		return imageView;
	}

}
