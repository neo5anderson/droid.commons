package neo.java.commons;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 其他类别的静态方法
 * 
 * @author neo
 */
public class Others {

	private static int LAYER = 0;
	private static StringBuilder STRING_BUILDER = new StringBuilder();

	/**
	 * 通过反射获取某个类的字段、方法、内部类，输出到终端
	 * 
	 * @param class2do
	 *            要去操作的类
	 */
	public static void getAllFieldMethodClassFromClass(Class<?> class2do) {
		String tabwidthString = "";
		String widthString = "[%d]";

		for (int i = 0; i < LAYER; i++) {
			tabwidthString += "\t";
		}

		LAYER++;

		Field[] fields = class2do.getDeclaredFields();
		widthString = formatArrayOutput(fields.length);
		for (int i = 0; i < fields.length; i++) {
			STRING_BUILDER.append(String.format(tabwidthString + "field"
					+ widthString + ": %s %s\n", i, fields[i].getType()
					.getCanonicalName(), fields[i].getName()));
		}

		Class<?>[] classes = null;
		Method[] methods = class2do.getDeclaredMethods();
		widthString = formatArrayOutput(methods.length);
		for (int i = 0; i < methods.length; i++) {
			STRING_BUILDER.append(String.format(tabwidthString + "method"
					+ widthString + ": %s %s(", i, methods[i].getReturnType()
					.getCanonicalName(), methods[i].getName()));
			classes = methods[i].getParameterTypes();
			for (int j = 0; j < classes.length; j++) {
				STRING_BUILDER.append(classes[j].getCanonicalName() + ", ");
			}

			STRING_BUILDER.append(")\n");
		}

		classes = class2do.getDeclaredClasses();
		widthString = formatArrayOutput(classes.length);
		for (int i = 0; i < classes.length; i++) {
			STRING_BUILDER.append(String.format(tabwidthString + "class"
					+ widthString + ": %s\n", i, classes[i].getName()));

			try {
				getAllFieldMethodClassFromClass(Class.forName(classes[i]
						.getName()));
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		LAYER--;
		if (0 == LAYER) {
			System.out.println(STRING_BUILDER.toString());
			STRING_BUILDER.delete(0, STRING_BUILDER.length());
		}
	}

	private static String formatArrayOutput(int length) {
		String widthString = "[%d]";

		if (length > 99) {
			widthString = "[%-3d]";
		} else if (length > 9) {
			widthString = "[%-2d]";
		}

		return widthString;
	}

}
