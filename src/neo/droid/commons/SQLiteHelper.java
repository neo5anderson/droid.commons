package neo.droid.commons;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLite 协助类
 * 
 * @author neo
 */
public class SQLiteHelper {
	/** 数据库名称 */
	private String mDBName;
	/** 数据表名称 */
	private String mTableName;
	/** 数据列名称数组 */
	private String[] mCols;

	/** 执行 SQL 的字符串 */
	private String mSQLString;
	/** SQLite数据库 */
	private SQLiteDatabase mDatabase;
	/** SQLiteOpenHelper 派生类 */
	private SQLiteOpenSubHelper mDBHelper;

	/**
	 * SQLite 帮助类构造方法
	 * 
	 * @param context
	 *            内容提供者
	 * @param db
	 *            数据库名称
	 * @param table
	 *            数据表名称
	 * @param cols
	 *            数据列名数组
	 * @param ver
	 *            数据库版本号
	 */
	public SQLiteHelper(Context context, String db, String table,
			String[] cols, int ver) {
		mDBName = db;
		mTableName = table;
		mCols = cols;

		// [Neo] 使用 SQLiteOpenHelper 的派生类创建数据库
		mDBHelper = new SQLiteOpenSubHelper(context, mDBName, null, ver);
		// [Neo] 获取可写的数据库实例
		mDatabase = mDBHelper.getWritableDatabase();
	}

	/**
	 * 关闭数据库相关资源
	 */
	public void close() {
		mDBHelper.close();
		mDatabase.close();
	}

	/**
	 * SQLiteOpenHelper 派生类
	 * 
	 * @author neo
	 */
	class SQLiteOpenSubHelper extends SQLiteOpenHelper {
		/** SQLiteOpenSubHelper 构造方法 */
		public SQLiteOpenSubHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) throws SQLException {
			mSQLString = "create table " + mTableName + " (";
			for (int i = 0; i < mCols.length - 1; i++) {
				// [Neo] 每一列累加进 SQL 字符串中
				mSQLString += mCols[i] + ", ";
			}
			// [Neo] SQL 不允许使用 , 结尾，所以，最后一个手动添加
			mSQLString += mCols[mCols.length - 1] + ")";

			// [Neo] 注意：execSQL 可能会抛出异常
			db.execSQL(mSQLString);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
				throws SQLException {
			mSQLString = "drop table if exists " + mTableName;
			db.execSQL(mSQLString);

			mDBHelper.onCreate(db);
		}
	}

	/**
	 * 类似于 SQLiteDatabase.query 的方法，注意 distinct 被放在了最后
	 * 
	 * @param table
	 * @param cols
	 * @param where
	 * @param selectionArgs
	 * @param groupBy
	 * @param having
	 * @param orderBy
	 * @param limit
	 * @param distinct
	 * @return
	 */
	public Cursor query(String table, String[] cols, String where,
			String[] selectionArgs, String groupBy, String having,
			String orderBy, String limit, boolean distinct) {
		return mDatabase.query(distinct, table, cols, where, selectionArgs,
				groupBy, having, orderBy, limit);
	}

	/**
	 * 将 Cursor 类型的查询结果转成 String，含 Title，可能是空值
	 * 
	 * @param cursor
	 * @return
	 */
	public String cursor2String(Cursor cursor) {
		if (0 == cursor.getColumnCount()) {
			// [Neo] 注意，可能是空值
			return "";
		}

		String result = "(" + cursor.getColumnName(0);
		int cols = cursor.getColumnCount();

		for (int i = 1; i < cols; i++) {
			result += "|" + cursor.getColumnName(i);
		}
		result += ")\n";

		for (cursor.moveToFirst(); false == cursor.isAfterLast(); cursor
				.moveToNext()) {
			result += cursor.getString(0);
			for (int j = 1; j < cols; j++) {
				result += "|" + cursor.getString(j);
			}
			result += "\n";
		}

		cursor.close();
		return result;
	}

	/**
	 * 执行 SQL 语句，无法返回值
	 * 
	 * 
	 * @param sql
	 * @throws SQLException
	 */
	public synchronized void execSQL(String sql) throws SQLException {
		if (null != mDatabase) {
			mDatabase.execSQL(sql);
		}
	}

	/**
	 * 执行 select SQL 语句，返回 Cursor
	 * 
	 * @param sql
	 * @return
	 */
	public Cursor selectSQL(String sql) {
		return mDatabase.rawQuery(sql, null);
	}

	/**
	 * 执行 select SQL 语句，返回 String
	 * 
	 * @param sql
	 * @return
	 */
	public String selectSQLString(String sql) {
		return cursor2String(selectSQL(sql));
	}

}
