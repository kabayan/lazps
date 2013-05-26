package jpadlibjapan.android.lib.simpledb;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Hashtable;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class SimpleDbHelper {
	/*
	 * 列には必ず _id が必要
	 */

	@Retention(RetentionPolicy.RUNTIME)
	public @interface att {
		public String value();
	}

	private static final String MAX_VALUE = "2147483647";

	// データベース名
	public String dbname;
	public int DATABASE_VERSION;
	public Object mTableDefine;
	public Cursor mCursor;
	private SQLiteDatabase lDbcache;

	private DBConnection mDbConnection;

	public void create(Context ctx) {
		if (mDbConnection == null) {
			mDbConnection = getInstance(ctx);
		}
	}

	private DBConnection getInstance(Context ctx) {
		if (mDbConnection == null)
			mDbConnection = new DBConnection(ctx);
		return mDbConnection;
	}

	public class DBConnection extends SQLiteOpenHelper {
		private DBConnection(Context ctx) {
			super(ctx, dbname, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			createTable(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
			dropTable(db);
			onCreate(db);
		}

	}

	private void createTable(SQLiteDatabase db) {
		try {
			// テーブル一覧
			Field[] farray = mTableDefine.getClass().getFields();
			for (Field tf : farray) {
				String query = "";
				// テーブル名
				String tname = tf.getName();
				query = "create table " + tname + " (";
				// カラム一覧
				Field[] carray = tf.get(mTableDefine).getClass().getFields();
				for (Field cf : carray) {
					// カラム名
					String cname = cf.getName();
					// 型
					String ctype = cf.getType().getSimpleName();
					if (ctype.compareTo("String") == 0) {
						ctype = "TEXT";
					}
					// // 値
					// String ci = cf.get(TABLE_DEFINE).toString();

					// アノテーション(付加情報）
					Annotation[] canos = cf.getDeclaredAnnotations();
					String annos = "";
					if (canos.length != 0) {
						att ann = (att) canos[0];
						annos = ann.value();
					}
					query += cname + " " + ctype + " " + annos + ",";
				}
				query = query.substring(0, query.length() - 1) + ");";
				db.execSQL(query);
			}

		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		}
	}

	private void dropTable(SQLiteDatabase db) {
		try {
			// テーブル一覧
			Field[] farray = mTableDefine.getClass().getFields();
			for (Field tf : farray) {
				// テーブル名
				String tname = tf.getName();
				db.execSQL("drop table if exists " + tname);
			}
		} catch (IllegalArgumentException e) {
		}
	}

	public void close(){
		if(mCursor !=null ){
			if (!mCursor.isClosed()){
				mCursor.close();
			}
		}
		if(mDbConnection!=null){
			mDbConnection.close();
		}
	}

	public void clearTable(Object row) {
		try {
			// テーブル名
			String tname = row.getClass().getSimpleName();
			// テーブル探索
			Object tables = this.mTableDefine;
			Field[] farray = tables.getClass().getFields();
			for (Field tf : farray) {
				// テーブル名
				if (tname.compareTo(tf.getName()) == 0) {
					SQLiteDatabase db = mDbConnection.getWritableDatabase();
					db.execSQL("delete from " +tname);
					break;
				}
			}
		} catch (IllegalArgumentException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

	public void clearRow(Object row) {
		try {
			// テーブル名
			String tname = row.getClass().getSimpleName();
			// テーブル探索
			Object tables = this.mTableDefine;
			Field[] farray = tables.getClass().getFields();
			for (Field tf : farray) {
				// テーブル名
				if (tname.compareTo(tf.getName()) == 0) {
					// カラム一覧
					Object tobj = tf.get(tables);
					Field[] carray = tobj.getClass().getFields();
					for (Field cf : carray) {
						Object d = cf.get(tobj);
						cf.set(tobj, null);
					}
					break;
				}
			}
		} catch (IllegalArgumentException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

	public void query(String query){
		SQLiteDatabase db = mDbConnection.getWritableDatabase();
//		db.beginTransaction();
		db.execSQL(query);
//		db.setTransactionSuccessful();
//		db.endTransaction();
		db.close();
	}
	public int selectcount(Object row) {
		return select(row, false);
	}

	public int select(Object row) {
		return select(row, true);
	}

	public int selectRaw(Object row,String query) {
		int result = -1;
		String tname = row.getClass().getSimpleName();
		try {
			if (mCursor != null) {
				if (!mCursor.isClosed()) {
					mCursor.close();
				}
			}
			lDbcache = mDbConnection.getReadableDatabase();
			mCursor = lDbcache.rawQuery("select * from " + tname + " " + query, null);
			result = mCursor.getCount();
			if (result != 0) {
				mCursor.moveToFirst();
			} else {
				mCursor.close();
			}

		} catch (IllegalArgumentException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return result;
	}

	public int select(Object row, boolean useCursor) {
		int result = -1;
		try {
			// 条件作成
			String where = "";
			ArrayList<String> whereArgsList = new ArrayList<String>();
			String[] whereArgs = null;
			;

			// テーブル名
			String tname = row.getClass().getSimpleName();
			// テーブル探索
			Field[] farray = mTableDefine.getClass().getFields();
			for (Field tf : farray) {
				if (tname.compareTo(tf.getName()) == 0) {
					// カラム一覧
					Object tobj = tf.get(mTableDefine);
					Field[] carray = tobj.getClass().getFields();
					for (Field cf : carray) {
						Object data = cf.get(tobj);
						if (data != null) {
							String cname = cf.getName();
							where += cname + " = ? and ";
							whereArgsList.add(data.toString());
						}
					}
					break;
				}
			}
			if (where.length()!=0) {
				where = where.substring(0, where.length() - 4);
				whereArgs = (String[]) whereArgsList.toArray(new String[0]);
			}
			if (mCursor != null) {
				if (!mCursor.isClosed()) {
					mCursor.close();
				}
			}
			lDbcache = mDbConnection.getReadableDatabase();
			mCursor = lDbcache.query(tname, null, where, whereArgs, null, null,
					null, MAX_VALUE);
			result = mCursor.getCount();
			if (result != 0 && useCursor) {
				mCursor.moveToFirst();
			} else {
				mCursor.close();
			}

		} catch (IllegalArgumentException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return result;
	}

	public boolean getrow(Object row) {
		boolean result = false;
		if (!mCursor.isClosed() || !lDbcache.isOpen()) {
			try {
				// テーブル名
				String tname = row.getClass().getSimpleName();
				// テーブル探索
				Object tables = this.mTableDefine;
				Field[] farray = tables.getClass().getFields();
				for (Field tf : farray) {
					if (tname.compareTo(tf.getName()) == 0) {
						// カラム一覧
						int index = 0;
						Object tobj = tf.get(tables);
						Field[] carray = tobj.getClass().getFields();
						for (Field cf : carray) {
							cf.set(tobj, null);
							String ctype = cf.getType().getSimpleName();
							if (ctype.compareTo("Integer") == 0) {
								Integer i = mCursor.getInt(index);
								cf.set(tobj, i);
							} else if (ctype.compareTo("Double") == 0) {
								Double d = mCursor.getDouble(index);
								cf.set(tobj, new Double(d));
							} else {
								cf.set(tobj, mCursor.getString(index));
							}
							index++;
						}
						break;
					}
				}
				result = mCursor.moveToNext();
				if (!result) {
					mCursor.close();
				}

			} catch (IllegalArgumentException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		}
		return result;
	}

	public boolean getrowAtPositon(Object row, int pos) {
		boolean result = false;
		if (!mCursor.isClosed() || !lDbcache.isOpen()) {
			mCursor.moveToPosition(pos);
			return getrow(row);
		}
		return result;
	}

	public boolean movetofirst(Object row) {
		boolean result = false;
		if (!mCursor.isClosed() || !lDbcache.isOpen()) {
			mCursor.moveToFirst();
			return true;
		}
		return result;
	}

	public boolean prev(Object row) {
		boolean result = false;
		if (!mCursor.isClosed() || !lDbcache.isOpen()) {
			if (!mCursor.isFirst()) {
				mCursor.moveToPrevious();
			}
			return true;
		}
		return result;
	}

	public boolean next(Object row) {
		boolean result = false;
		if (!mCursor.isClosed() || !lDbcache.isOpen()) {
			if (!mCursor.isLast()) {
				mCursor.moveToNext();
			}
			return true;
		}
		return result;
	}

	public long insert(Object row) {
		long ret = -1;
		try {
			ContentValues values = new ContentValues();
			// テーブル名
			String tname = row.getClass().getSimpleName();
			// テーブル探索
			Field[] farray = mTableDefine.getClass().getFields();
			for (Field tf : farray) {
				// テーブル名
				if (tname.compareTo(tf.getName()) == 0) {
					// カラム一覧
					Object tobj = tf.get(mTableDefine);
					Field[] carray = tobj.getClass().getFields();
					for (Field cf : carray) {
						Object data = cf.get(tobj);
						if (data != null) {
							String res = data.toString();
							String cname = cf.getName();
							String ctype = cf.getType().getSimpleName();
							if (ctype.compareTo("Integer") == 0) {
								values.put(cname, Integer.valueOf(res));
							} else if (ctype.compareTo("double") == 0) {
								values.put(cname, Double.valueOf(res));
							} else {
								values.put(cname, res);
							}
						}
					}
					SQLiteDatabase db = mDbConnection.getWritableDatabase();
					db.beginTransaction();
					ret = db.insert(tname, null, values);
					db.setTransactionSuccessful();
					db.endTransaction();
					// db.close();
					break;
				}
			}
		} catch (IllegalArgumentException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return ret;
	}

	public int delete(Object row) {
		int res = -1;
		// 条件作成
		String where = "";
		ArrayList<String> whereArgsList = new ArrayList<String>();
		try {
			// テーブル名
			String tname = row.getClass().getSimpleName();
			// テーブル探索
			Field[] farray = mTableDefine.getClass().getFields();
			for (Field tf : farray) {
				if (tname.compareTo(tf.getName()) == 0) {
					// カラム一覧
					Object tobj = tf.get(mTableDefine);
					Field[] carray = tobj.getClass().getFields();
					for (Field cf : carray) {
						Object data = cf.get(tobj);
						if (data != null) {
							String cname = cf.getName();
							where += cname + " = ? and ";
							whereArgsList.add(data.toString());
						}
					}

					where = where.substring(0, where.length() - 4);

					String[] whereArgs = (String[]) whereArgsList
							.toArray(new String[0]);

					SQLiteDatabase db = mDbConnection.getWritableDatabase();
					db.beginTransaction();
					res = db.delete(tname, where, whereArgs);
					db.setTransactionSuccessful();
					db.endTransaction();
					// db.close();
					break;
				}
			}
		} catch (IllegalArgumentException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return res;
	}

	public int update(Object cond, SimpleDbHelper newvalue) {
		int result = -1;
		try {
			// 条件作成
			String where = "";
			ArrayList<String> whereArgsList = new ArrayList<String>();
			String[] whereArgs;

			// テーブル名
			String tname = cond.getClass().getSimpleName();
			// テーブル探索
			Field[] farray = mTableDefine.getClass().getFields();
			for (Field tf : farray) {
				if (tname.compareTo(tf.getName()) == 0) {
					// カラム一覧
					Object tobj = tf.get(mTableDefine);
					Field[] carray = tobj.getClass().getFields();
					for (Field cf : carray) {
						Object data = cf.get(tobj);
						if (data != null) {
							String cname = cf.getName();
							where += cname + " = ? and ";
							whereArgsList.add(data.toString());
						}
					}
					where = where.substring(0, where.length() - 4);
					break;
				}
			}

			// 変更する値
			ContentValues values = new ContentValues();
			// テーブル探索
			Object newtable = newvalue.mTableDefine;

			farray = newtable.getClass().getFields();
			for (Field tf : farray) {
				// テーブル名が同じものを探す
				if (tname.compareTo(tf.getName()) == 0) {
					// カラム一覧
					Object tobj = tf.get(newtable);
					Field[] carray = tobj.getClass().getFields();
					for (Field cf : carray) {
						Object data = cf.get(tobj);
						String cname = cf.getName();
						if (data != null) {
							String res = data.toString();
							String ctype = cf.getType().getSimpleName();
							if (ctype.compareTo("Integer") == 0) {
								values.put(cname, Integer.valueOf(res));
							} else if (ctype.compareTo("double") == 0) {
								values.put(cname, Double.valueOf(res));
							} else {
								values.put(cname, res);
							}
						}
						// else {
						// values.put(cname, (String)null);
						// }
					}
					break;
				}
			}
			whereArgs = (String[]) whereArgsList.toArray(new String[0]);
			SQLiteDatabase db = mDbConnection.getWritableDatabase();
			result = db.update(tname, values, where, whereArgs);
			// db.close();

		} catch (IllegalArgumentException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return result;
	}

	public int simpledelete(Object row) {
		int res = -1;
		try {
			String where = "";
			ArrayList<String> whereArgsList = new ArrayList<String>();

			// テーブル名
			String tname = row.getClass().getSimpleName();
			// テーブル探索
			Field[] farray = mTableDefine.getClass().getFields();
			for (Field tf : farray) {
				if (tname.compareTo(tf.getName()) == 0) {
					// カラム一覧
					Object tobj = tf.get(mTableDefine);
					Field[] carray = tobj.getClass().getFields();
					for (Field cf : carray) {
						Object data = cf.get(tobj);
						if (data != null) {
							String cname = cf.getName();
							where += cname + " = ? and ";
							whereArgsList.add(data.toString());
						}
					}

					where = where.substring(0, where.length() - 4);

					String[] whereArgs = (String[]) whereArgsList
							.toArray(new String[0]);

					SQLiteDatabase db = mDbConnection.getWritableDatabase();
					res = db.delete(tname, where, whereArgs);
					// db.close();

					break;
				}
			}
		} catch (IllegalArgumentException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return res;
	}

	public boolean bindDataToListView(Context con, ListView lv, Object table,
			int layout, String[] from, int[] to) {
		if (select(table, true) < 1) {
			// 結果がないかエラー
			return false;
		}

		SimpleCursorAdapter cursoradapter = new SimpleCursorAdapter(con,
				layout, mCursor, from, to);
		lv.setAdapter(cursoradapter);
		return true;
	}
}
