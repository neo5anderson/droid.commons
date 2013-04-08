package neo.droid.commons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLite 工具类
 * 
 * @author neo
 */
public class SQLiteUtils {

	private Map<String, String> tablesMap;
	private SQLiteOpenUtils openUtils;
	private SQLiteDatabase database;

	private boolean isOpen;

	/**
	 * 构造
	 * 
	 */
	public SQLiteUtils() {
		isOpen = false;
	}

	/**
	 * 打开数据库
	 * 
	 * @param context
	 * @param name
	 *            数据库名称
	 * @param tablesMap
	 *            表名与列的 map
	 * @param version
	 *            版本号
	 */
	public void open(Context context, String name,
			Map<String, String> tablesMap, int version) {
		if (false == isOpen) {
			this.tablesMap = tablesMap;
			openUtils = new SQLiteOpenUtils(context, name, null, version);
			database = openUtils.getWritableDatabase();
			isOpen = true;
		}
	}

	/**
	 * 关闭
	 * 
	 */
	public void close() {
		if (false != isOpen) {
			openUtils.close();
			database.close();
			isOpen = false;
		}
	}

	/**
	 * 执行 sql 语句，已同步
	 * 
	 * @param sql
	 */
	public synchronized void execSQL(String sql) {
		if (false != isOpen) {
			database.execSQL(sql);
		}
	}

	/**
	 * 查询
	 * 
	 * @param sql
	 * @param selectionArgs
	 * @return
	 */
	public Cursor select(String sql, String[] selectionArgs) {
		if (false != isOpen) {
			return database.rawQuery(sql, selectionArgs);
		} else {
			return null;
		}
	}

	/**
	 * 游标转列表
	 * 
	 * @param cursor
	 * @return
	 */
	public List<Map<String, String>> cursor2list(Cursor cursor) {
		int rows = cursor.getCount();
		int cols = cursor.getColumnCount();
		if (0 == rows) {
			return null;
		}

		String[] colStrings = new String[cols];
		for (int i = 0; i < cols; i++) {
			colStrings[i] = cursor.getColumnName(i);
		}

		List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();

		for (cursor.moveToFirst(); false == cursor.isAfterLast(); cursor
				.moveToNext()) {
			Map<String, String> map = new HashMap<String, String>();
			for (int j = 0; j < cols; j++) {
				map.put(colStrings[j], cursor.getString(j));
			}
			resultList.add(map);
		}

		cursor.close();
		return resultList;
	}

	/**
	 * 查询后结果集用列表对象返回
	 * 
	 * @param sql
	 * @param selectionArgs
	 * @return
	 */
	public List<Map<String, String>> select2list(String sql,
			String[] selectionArgs) {
		if (false != isOpen) {
			return cursor2list(select(sql, selectionArgs));
		} else {
			return null;
		}
	}

	/**
	 * SQLite 打开工具类
	 * 
	 * @author neo
	 */
	class SQLiteOpenUtils extends SQLiteOpenHelper {

		/**
		 * 构造
		 * 
		 * @param context
		 * @param name
		 * @param factory
		 * @param version
		 */
		public SQLiteOpenUtils(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase database) {
			String tableName = null;
			Iterator<String> iterator = tablesMap.keySet().iterator();
			while (iterator.hasNext()) {
				tableName = iterator.next();
				database.execSQL("CREATE TABLE " + tableName + "("
						+ tablesMap.get(tableName) + ")");
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase database, int oldVersion,
				int newVersion) {
			Iterator<String> iterator = tablesMap.keySet().iterator();
			while (iterator.hasNext()) {
				database.execSQL("DROP TABLE IF EXISTS " + iterator.next());
			}
			openUtils.onCreate(database);
		}

	}
}
