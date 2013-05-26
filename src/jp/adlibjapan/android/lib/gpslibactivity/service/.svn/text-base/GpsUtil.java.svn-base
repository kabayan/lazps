package jp.adlibjapan.android.lib.gpslibactivity.service;

import java.io.IOException;
import java.security.spec.MGF1ParameterSpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jp.adlibjapan.android.lib.gpslibactivity.GpsLibSettingActivity;
import jp.adlibjapan.android.lib.gpslibactivity.R;
import jp.adlibjapan.android.lib.gpslibactivity.Utils;
import jp.adlibjapan.android.lib.matrix.Main;
import jpadlibjapan.android.lib.simpledb.GpsLibDb;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.SystemClock;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;

public class GpsUtil {

	public static final int WIFI = 1;
	public static final int GPS = 2;
	private static final float DISTANCELIMIT = 0;

	private Utils mUtil;
	private GpsLibDb mDb;
	private Context mContext;

	public GpsUtil(Context con) {
		mUtil = new Utils(con);
		mDb = new GpsLibDb(con);
		mContext = con;
	}

	// Preferenceの更新
	public void setLocationsToPreference(double lat, double lon,
			double accuracy, double time) {

		double time0 = getTime();
		if (time0 == 0) {
			// SharedPreferencesがないので入れるしかない
			setLocationsToPreference1(lat, lon, accuracy, time);
		} else {
			float[] results = new float[1];
			Location.distanceBetween(lat, lon, getLat(), getLon(), results);
			if (results[0] < DISTANCELIMIT) {
				// 異常値なのでなにもしない
				// TODO
				// 本来は時間から移動速度を計算すべき
				// calc(System.currentTimeMillis());
			} else {
				// 正常なのでSPに入れる
				setLocationsToPreference1(lat, lon, accuracy, time);
			}
		}
	}

	private void setLocationsToPreference1(double lat, double lon,
			double accuracy, double time) {
		double llat = getLat();
		double llon = getLon();

		setLat(lat);
		setLon(lon);
		setAcc(accuracy);
		setTime(time);
		String g = "";
		if (accuracy != 0D) {
			int level = GeoHex.calcLevelSize(accuracy);
			g = GeoHex.encode(lat, lon, level);
			// http://geohex.net/v3.html?code=XM48851848
		}
		setGeohex(g);

		String address = "";
		if (llat == lat && llon == lon) {
			// 移動していないのでなにもしない
		} else {
			if (getGeoCordingEnable()) {
				address = GpsUtil.point2address(lat, lon, mContext);
				setGeoCording(address);
			}
		}

		Utils.logText("GpsLib: setLocationsToPreference lat = " + lat
				+ " lon = " + lon + " acc =" + accuracy + " address = "
				+ address + " geohex =" + g + " time="
				+ Utils.printdatetime((long) time));
		Utils.writeFile2csv(time + "," + g + " " + address + "," + lon + ","
				+ lat + "," + accuracy);
		// }
	}

	/*
	 * 特定時刻の位置を計算してprefrenceに設定。
	 */
	public void calc(double timetoget) {
		Log.d("GpsLib", "Caliculateing...");

		// １時間以上前の履歴をクリア
		// mDb.clearRow(mDb.table.geo_table);
		// mDb.select(mDb.table.geo_table);
		// mDb.getrow(mDb.table.geo_table);
		String query = "delete from geo_table where time < "
				+ (System.currentTimeMillis() - 11 * 60 * 1000);
		mDb.query(query);
		// mDb.clearRow(mDb.table.geo_table);
		// Utils.logText("count = " + mDb.selectcount(mDb.table.geo_table));
		Utils.logText("startGPS:clear old logs");

		ArrayList<Double> latarray = new ArrayList<Double>();
		ArrayList<Double> lonarray = new ArrayList<Double>();
		ArrayList<Double> timearray = new ArrayList<Double>();
		ArrayList<Double> accarray = new ArrayList<Double>();

		mDb.clearRow(mDb.table.geo_table);
		int resultcount = mDb.selectRaw(mDb.table.geo_table,
				" order by time DESC limit 3");

		switch (resultcount) {
		case 0:
			// 履歴なし
			return;
		case 1:
			// 3つとも同じ
			mDb.getrow(mDb.table.geo_table);
			for (int i = 0; i < 3; i++) {
				latarray.add(mDb.table.geo_table.lat * 1E6);
				lonarray.add(mDb.table.geo_table.lon * 1E6);
				accarray.add(mDb.table.geo_table.acc);
				timearray.add(mDb.table.geo_table.time);
			}
			break;
		case 2:
			// 1回前
			mDb.getrow(mDb.table.geo_table);
			latarray.add(mDb.table.geo_table.lat * 1E6);
			lonarray.add(mDb.table.geo_table.lon * 1E6);
			accarray.add(mDb.table.geo_table.acc);
			timearray.add(mDb.table.geo_table.time);
			// 2回前と３回前
			mDb.getrow(mDb.table.geo_table);
			for (int i = 0; i < 2; i++) {
				latarray.add(mDb.table.geo_table.lat * 1E6);
				lonarray.add(mDb.table.geo_table.lon * 1E6);
				accarray.add(mDb.table.geo_table.acc);
				timearray.add(mDb.table.geo_table.time);
			}
			break;
		case 3:
			for (int i = 0; i < 3; i++) {
				mDb.getrow(mDb.table.geo_table);
				latarray.add(mDb.table.geo_table.lat * 1E6);
				lonarray.add(mDb.table.geo_table.lon * 1E6);
				accarray.add(mDb.table.geo_table.acc);
				timearray.add(mDb.table.geo_table.time);
			}
			break;
		}

		// 判定ロジック
		long now = System.currentTimeMillis();
		final int ONEMINUTE = 60 * 1000;
		final long GPSRANGE = getGPSrange();
		final long INTERVAL = getInterval();
		double interval;

		if (now - timearray.get(2) < ONEMINUTE) {
			// 最新が１分以内
			if (accarray.get(2) < GPSRANGE) {
				// 十分な精度
				interval = timearray.get(2) - timearray.get(1);
				if (interval == 0
						|| (interval - INTERVAL) * (interval - INTERVAL) < 1000) {
					// 更新頻度通りに値が取れている
					if (accarray.get(1) < GPSRANGE) {
						// 精度が良い
						calcline(2, 1, now, latarray, lonarray, accarray,
								timearray);
						return;
					} else {
						calcsection2(INTERVAL, GPSRANGE, now, latarray,
								lonarray, accarray, timearray);
						return;
					}
				} else {
					// 更新頻度通りに値が取れていない
					calcsection2(INTERVAL, GPSRANGE, now, latarray, lonarray,
							accarray, timearray);
				}
			} else {
				// 精度が良くない
				// 十分な精度
				interval = timearray.get(2) - timearray.get(1);
				if (interval == 0
						|| (interval - INTERVAL) * (interval - INTERVAL) < 1000) {
					// 更新頻度通りに値が取れている
					if (accarray.get(1) < GPSRANGE) {
						// 精度が良い
						calcsection2(INTERVAL, GPSRANGE, now, latarray,
								lonarray, accarray, timearray);
						return;
					} else {
						// 精度が悪い
						calcline(2, 1, now, latarray, lonarray, accarray,
								timearray);
						return;
					}
				} else {
					// 更新頻度通りに値が取れていない
					interval = timearray.get(1) - timearray.get(0);
					if (interval == 0
							|| (interval - INTERVAL) * (interval - INTERVAL) < 1000) {
						calcline(2, 0, now, latarray, lonarray, accarray,
								timearray);
						return;
					} else {
						calcline(2, 1, now, latarray, lonarray, accarray,
								timearray);
						return;
					}
				}

			}

		} else {
			interval = timearray.get(2) - timearray.get(1);
			if (accarray.get(2) < GPSRANGE) {
				// 十分な精度
				if (interval == 0
						|| (interval - INTERVAL) * (interval - INTERVAL) < 1000) {
					// 更新頻度通りに値が取れている
					if (accarray.get(1) < GPSRANGE) {
						// 精度が良い
						calcline(2, 1, now, latarray, lonarray, accarray,
								timearray);
						return;
					} else {
						calcsection3(INTERVAL, GPSRANGE, now, latarray,
								lonarray, accarray, timearray);
						return;
					}
				} else {
					// 更新頻度通りに値が取れていない
					calcline(2, 1, now, latarray, lonarray, accarray, timearray);
					return;
				}
			} else {
				if (interval == 0
						|| (interval - INTERVAL) * (interval - INTERVAL) < 1000) {
					// 更新頻度通りに値が取れている
					if (accarray.get(1) < GPSRANGE) {
						// 精度が良い
						calcsection2(INTERVAL, GPSRANGE, now, latarray,
								lonarray, accarray, timearray);
						return;
					} else {
						calcline(2, 1, now, latarray, lonarray, accarray,
								timearray);
						return;
					}
				} else {
					interval = timearray.get(1) - timearray.get(0);
					if (interval == 0
							|| (interval - INTERVAL) * (interval - INTERVAL) < 1000) {
						calcline(2, 0, now, latarray, lonarray, accarray,
								timearray);
						return;
					} else {
						calcline(2, 1, now, latarray, lonarray, accarray,
								timearray);
						return;
					}

				}
			}
		}
	}

	private void calcsection2(long INTERVAL, long GPSRANGE, long now,
			ArrayList<Double> latarray, ArrayList<Double> lonarray,
			ArrayList<Double> accarray, ArrayList<Double> timearray) {
		double interval = timearray.get(1) - timearray.get(0);
		if (interval == 0
				|| (interval - INTERVAL) * (interval - INTERVAL) < 1000) {
			if (accarray.get(0) < GPSRANGE) {
				calcline(2, 0, now, latarray, lonarray, accarray, timearray);
				return;
			} else {
				calcline(2, 1, now, latarray, lonarray, accarray, timearray);
				return;
			}
		} else {
			calcline(2, 1, now, latarray, lonarray, accarray, timearray);
			return;
		}
	}

	private void calcsection3(long INTERVAL, long GPSRANGE, long now,
			ArrayList<Double> latarray, ArrayList<Double> lonarray,
			ArrayList<Double> accarray, ArrayList<Double> timearray) {
		double interval = timearray.get(1) - timearray.get(0);
		if (interval == 0
				|| (interval - INTERVAL) * (interval - INTERVAL) < 1000) {
			if (accarray.get(0) < GPSRANGE) {
				calcline(2, 0, now, latarray, lonarray, accarray, timearray);
				return;
			} else {
				calcline(2, 1, now, latarray, lonarray, accarray, timearray);
				return;
			}
		} else {
			if (accarray.get(0) < GPSRANGE) {
				calcline(2, 0, now, latarray, lonarray, accarray, timearray);
				return;
			} else {
				calcline(2, 1, now, latarray, lonarray, accarray, timearray);
				return;
			}
		}
	}

	private void calcline(int first, int last, long now,
			ArrayList<Double> latarray, ArrayList<Double> lonarray,
			ArrayList<Double> accarray, ArrayList<Double> timearray) {

		double lat = calclinesub(now, first, last, latarray, timearray) / 1E6;
		double lon = calclinesub(now, first, last, lonarray, timearray) / 1E6;
		double acc = calclinesub(now, first, last, accarray, timearray);

		setLocationsToPreference(lat, lon, acc, now);
	}

	private double calclinesub(long now, int first, int last,
			ArrayList<Double> geoarray, ArrayList<Double> timearray) {
		// y = (y1 - y2) (x - x1)/(x1 - x2) + y1

		double x1x2 = timearray.get(first) - timearray.get(last);
		double y1y2 = geoarray.get(first) - geoarray.get(last);

		if (x1x2 == 0 || y1y2 == 0) {
			return geoarray.get(first);
		} else {
			return y1y2 * (now - timearray.get(first)) / x1x2
					+ geoarray.get(first);
		}
	}

	public void calc0(double timetoget) {
		Log.d("GpsLib", "Caliculateing...");

		ArrayList<Double> latarray = new ArrayList<Double>();
		ArrayList<Double> lonarray = new ArrayList<Double>();
		ArrayList<Double> timearray = new ArrayList<Double>();
		ArrayList<Double> accarray = new ArrayList<Double>();

		mDb.clearRow(mDb.table.geo_table);
		if (mDb.select(mDb.table.geo_table) > 0) {
			boolean eod = false;
			do {
				eod = mDb.getrow(mDb.table.geo_table);
				latarray.add(mDb.table.geo_table.lat * 1E6);
				lonarray.add(mDb.table.geo_table.lon * 1E6);
				accarray.add(mDb.table.geo_table.acc);
				timearray.add(mDb.table.geo_table.time);
			} while (eod);

			Main mat = new Main();
			mat.add(timearray, latarray);
			double lat = mat.calc(timetoget) / 1E6;

			mat = new Main();
			mat.add(timearray, lonarray);
			double lon = mat.calc(timetoget);
			lon = lon / 1E6;

			mat = new Main();
			mat.add(timearray, accarray);
			double acc = mat.calc(timetoget);

			setLocationsToPreference(lat, lon, acc, timetoget);
		}
	}

	public static String point2address(double latitude, double longitude,
			Context context) {

		String string = new String();

		// geocoedrの実体化
		Geocoder geocoder = new Geocoder(context, Locale.JAPAN);
		List<Address> list_address;
		try {
			list_address = geocoder.getFromLocation(latitude, longitude, 5);
			// ジオコーディングに成功したらStringへ
			if (!list_address.isEmpty()) {

				Address address = list_address.get(0);
				StringBuffer strbuf = new StringBuffer();

				// adressをStringへ
				String buf;
				for (int i = 0; (buf = address.getAddressLine(i)) != null; i++) {
					// strbuf.append("address.getAddressLine("+i+"):"+buf+"\n");
					strbuf.append(buf + " ");
				}
				string = strbuf.toString();
			}
			// 失敗（Listが空だったら）
			else {
				Log.d("GpsLib", "Fail Geocoding");
			}
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} // 引数末尾は返す検索結果数

		return string;
	}

	public void setGPSProvider(String status) {
		mUtil.setKey(R.string.gpsstatus, status);
	}

	public String getGPStatus() {
		return mUtil.getKey(R.string.gpsstatus);
	}

	public void setGPS(boolean use) {
		mUtil.setKeyBoolean(R.string.gps, use);
	}

	public boolean getGPS() {
		return mUtil.getKeyBoolean(R.string.gps);
	}

	public void setWifi(boolean use) {
		mUtil.setKeyBoolean(R.string.wifi, use);
	}

	public boolean getWifi() {
		return mUtil.getKeyBoolean(R.string.wifi);
	}

	public void setLat(double lat) {
		mUtil.setKeyDouble(R.string.lat, lat);
	}

	public void setLon(double lon) {
		mUtil.setKeyDouble(R.string.lon, lon);
	}

	public void setAcc(double acc) {
		mUtil.setKeyDouble(R.string.acc, acc);
	}

	public void setTime(double time) {
		mUtil.setKeyDouble(R.string.time, time);
	}

	public double getLat() {
		return mUtil.getKeyDouble(R.string.lat);
	}

	public double getLon() {
		return mUtil.getKeyDouble(R.string.lon);
	}

	public double getAcc() {
		return mUtil.getKeyDouble(R.string.acc);
	}

	public double getTime() {
		return mUtil.getKeyDouble(R.string.time);
	}

	public void setSatteliteCount(int c) {
		mUtil.setKeyDouble(R.string.sat, c);
	}

	public double getSatteliteCount() {
		return mUtil.getKeyDouble(R.string.sat);
	}

	public void setGpsLibServiceIsActive(boolean b) {
		mUtil.setKeyBoolean(R.string.service, b);
	}

	public boolean getGpsLibServiceIsActive() {
		return mUtil.getKeyBoolean(R.string.service);
	}

	public void setGpsLibIsRunning(boolean b) {
		mUtil.setKeyBoolean(R.string.running, b);
	}

	public boolean getGpsLibIsRunning() {
		return mUtil.getKeyBoolean(R.string.running);
	}

	public void setGpsRange(long value) {
		mUtil.setKeyDouble(R.string.gpsrange, value);
	}

	public void setNetworkRange(long value) {
		mUtil.setKeyDouble(R.string.networkrange, value);
	}

	public void setInterval(long value) {
		mUtil.setKeyDouble(R.string.interval, value);
	}

	public long getGPSrange() {
		return mUtil.getKeyLong(R.string.gpsrange);
	}

	public long getNetworkRange() {
		return mUtil.getKeyLong(R.string.networkrange);
	}

	public long getInterval() {
		return mUtil.getKeyLong(R.string.interval);
	}

	public boolean getInit() {
		return mUtil.getKeyBoolean(R.string.app_name);
	}

	public String getGeoCording() {
		return mUtil.getKey(R.string.geocoding);
	}

	public String getGeohex() {
		return mUtil.getKey(R.string.geohex);
	}

	public void setGeoCording(String geocoding) {
		mUtil.setKey(R.string.geocoding, geocoding);
	}

	public void setGeohex(String geohex) {
		mUtil.setKey(R.string.geohex, geohex);
	}

	public void setNextInterval(long l) {
		mUtil.setKeyLong(R.string.nextinterval, l);
	}

	public long getNextInterval() {
		return mUtil.getKeyLong(R.string.nextinterval);
	}

	public void setGeoCordingEnable(boolean isChecked) {
		mUtil.setKeyBoolean(R.string.geocodingenable, isChecked);
	}

	public boolean getGeoCordingEnable() {
		return mUtil.getKeyBoolean(R.string.geocodingenable);
	}

}
