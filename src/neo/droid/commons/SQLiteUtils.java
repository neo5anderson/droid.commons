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

public class SQLiteUtils {

	private Map<String, String> tablesMap;
	private SQLiteOpenUtils openUtils;
	private SQLiteDatabase database;

	private boolean isOpen;

	public SQLiteUtils() {
		isOpen = false;
	}

	public void open(Context context, String name,
			Map<String, String> tablesMap, int version) {
		if (false == isOpen) {
			this.tablesMap = tablesMap;
			openUtils = new SQLiteOpenUtils(context, name, null, version);
			database = openUtils.getWritableDatabase();
			isOpen = true;
		}
	}

	public void close() {
		if (false != isOpen) {
			openUtils.close();
			database.close();
			isOpen = false;
		}
	}

	public synchronized void execSQL(String sql) {
		if (false != isOpen) {
			database.execSQL(sql);
		}
	}

	public Cursor select(String sql, String[] selectionArgs) {
		if (false != isOpen) {
			return database.rawQuery(sql, selectionArgs);
		} else {
			return null;
		}
	}

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

	public List<Map<String, String>> select2list(String sql,
			String[] selectionArgs) {
		if (false != isOpen) {
			return cursor2list(select(sql, selectionArgs));
		} else {
			return null;
		}
	}

	class SQLiteOpenUtils extends SQLiteOpenHelper {

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
