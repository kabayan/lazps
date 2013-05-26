package jp.adlibjapan.android.lib.gpslibactivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.test.PerformanceTestCase;
import android.util.Log;

public class Utils {

	private Context mContext;
	private SharedPreferences mSp;
	private Editor ed;

	public Utils(Context context) {
		mContext = context;
		mSp = context.getSharedPreferences(context.getPackageName(), Context.MODE_WORLD_READABLE);
		ed = mSp.edit();
	}

	public String getKey(int key) {
		return mSp.getString(mContext.getString(key), "");
	}

	public boolean getKeyBoolean(int key) {
		return mSp.getBoolean(mContext.getString(key), false);
	}

	public void setKey(String key, String value) {
		ed.putString(key, value);
		ed.commit();
	}

	public void setKey(int key, String value) {
		ed.putString(mContext.getString(key), value);
		ed.commit();
	}

	public void setKeyBoolean(int key, boolean value) {
		ed.putBoolean(mContext.getString(key), value);
		ed.commit();
	}

	public void setKeyDouble(int key, double lat) {
		ed.putString(mContext.getString(key), Double.toString(lat));
		ed.commit();
	}

	public double getKeyDouble(int key) {
		String v = mSp.getString(mContext.getString(key), null);
		if (v != null) {
			return Double.valueOf(v);
		} else {
			return -1;
		}
	}

	public void setKeyLong(int key, long lat) {
		ed.putString(mContext.getString(key), Long.toString(lat));
		ed.commit();
	}

	public long getKeyLong(int key) {
		String v = mSp.getString(mContext.getString(key), null);
		if (v != null) {
			return Math.round(Double.valueOf(v));
		} else {
			return -1;
		}
	}

	/**
	 * ファイル書き込み処理（String文字列⇒ファイル）
	 *
	 * @param sFilepath
	 *            　書き込みファイルパス
	 * @param sOutdata
	 *            　ファイル出力するデータ
	 * @param sEnctype
	 *            　文字エンコード
	 */
	public static void writeFile(String sOutdata) {
		String sdcard = Environment.getExternalStorageDirectory().getPath();
	    String path = sdcard + File.separator + "log.txt";

	    final String s = new SimpleDateFormat("yyyyMMdd HHmmss")
		.format(new Date())
		+ " " + sOutdata + "\n";

		writeFile(path, s, "UTF8");
	}

	public static void writeFile2csv(String string) {
		String sdcard = Environment.getExternalStorageDirectory().getPath();
	    String path = sdcard + File.separator + "log.csv";
		writeFile(path, string + "\n", "UTF8");
	}

	public static void writeFileClear() {
		String sdcard = Environment.getExternalStorageDirectory().getPath();
	    String path = sdcard + File.separator + "log.txt";
		writeFile(path, "", "UTF8",false);
	    path = sdcard + File.separator + "log.csv";
		writeFile(path, "", "UTF8",false);
	}

	public static void writeFile(String sFilepath, String sOutdata,
			String sEnctype) {
		writeFile(sFilepath, sOutdata, sEnctype,true);
	}

	public static void writeFile(String sFilepath, String sOutdata,
			String sEnctype,boolean add) {


		BufferedWriter bufferedWriterObj = null;
		try {
			// ファイル出力ストリームの作成
			bufferedWriterObj = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(sFilepath, add), sEnctype));

			bufferedWriterObj.write(sOutdata);
			bufferedWriterObj.flush();

		} catch (Exception e) {
		} finally {
			try {
				if (bufferedWriterObj != null)
					bufferedWriterObj.close();
			} catch (IOException e2) {
			}
		}
	}

	/**
	 * ファイル読み込み処理（ファイル⇒String文字列）
	 *
	 * @param sFilepath
	 *            　書き込みファイルパス
	 * @param sEnctype
	 *            　文字エンコード
	 * @return　読み込みだファイルデータ文字列
	 */
	public static String readFile(String sFilepath, String sEnctype) {

		String sData = "";
		BufferedReader bufferedReaderObj = null;

		try {
			// 入力ストリームの作成
			bufferedReaderObj = new BufferedReader(new InputStreamReader(
					new FileInputStream(sFilepath), sEnctype));

			String sLine;
			while ((sLine = bufferedReaderObj.readLine()) != null) {
				sData += sLine + "\n";
			}

		} catch (Exception e) {
		} finally {
			try {
				if (bufferedReaderObj != null)
					bufferedReaderObj.close();
			} catch (IOException e2) {
			}
		}

		return sData;
	}

	public static String printdatetime(long mills){
		return  new SimpleDateFormat("yyyyMMdd HH:mm:ss")
		.format(mills);
	}

	public static void logText(String string) {
		Log.i("GpsLib", string);
//		Utils.writeFile(string);
//		if (mGpsLibSettingActivity != null) {
//			mGpsLibSettingActivity.logText(string);
//		}
	}

}
