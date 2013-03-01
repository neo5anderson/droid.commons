package neo.java.commons;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 其他类别的静态方法
 * 
 * @author neo
 */
public class Others {

	/**
	 * 通过反射获取某个类的隐藏方法
	 * 
	 * @param class2do
	 *            要去操作的类
	 */
	public static void getAllHiddenMethod(Class<Object> class2do) {
		Method[] methods = class2do.getMethods();
		for (int i = 0; i < methods.length; i++) {
			// [Neo] TODO
			System.out.println("method [" + i + "]: " + methods[i].getName());
		}
		Field[] fields = class2do.getFields();
		for (int i = 0; i < fields.length; i++) {
			// [Neo] TODO
			System.out.println("field [" + i + "]: " + fields[i].getName());
		}
	}
}
