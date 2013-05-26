package jp.adlibjapan.android.lib.gpslibactivity.service;

import java.security.spec.MGF1ParameterSpec;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import jp.adlibjapan.android.lib.gpslibactivity.GpsLibSettingActivity;
import jp.adlibjapan.android.lib.gpslibactivity.R;
import jp.adlibjapan.android.lib.gpslibactivity.Utils;
import jp.adlibjapan.android.lib.matrix.Main;
import jpadlibjapan.android.lib.simpledb.GpsLibDb;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;
import android.widget.ToggleButton;

public class GpsLib {

	private static final long NETWORKTIMER = 20 * 1000;
	private static final long GPSWORKTIMER = 10 * 1000;
	protected static final long GPSSATTELITETIMER = 10 * 1000;

	protected static final long TIMERANGE = 5 * 60 * 1000;
	protected static final double NETWORKRANGE = 500;
	protected static final double GPSRANGE = 50;
	protected static final int INTERVAL = 3 * 60 * 1000;

	private static final float LASTPOSRANGE = 100;
	protected static final long SATTELITECOUNT = 5;
	private static final long POSTDELAYED = 1000;
	private static final long TICKERINTERVAL = 2000;
	private static final float DISTANCELIMIT = 10000;
	// protected static final int TIMEOUT = 0;

	private double mLat;
	private double mLon;
	private double mAcc;
	private long mTime;
	private double mRange;

	private long mNextTime;

	private int nId = R.string.app_name;

	private LocationManager mLocationManager;
	private Utils mUtil;
	private Context mContext;
	private GpsLibDb mDb;
	private TimerTask mTask;

	private Handler handler = new Handler();
	private Runnable runnable;
	private NotificationManager nNm;
	private Notification nNo;
	private long mNow;
	private boolean mWifi;
	private boolean mGps;
	private NewCountdownTimer mNewTimer;
	private GpsUtil mGpsUtil;
	public static GpsLibSettingActivity mGpsLibSettingActivity = null;

	public GpsLib(Context context, GpsLibDb gpsLibDb) {
		mUtil = new Utils(context);
		mContext = context;
		mDb = gpsLibDb;
		nNm = (NotificationManager) mContext
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mGpsUtil = new GpsUtil(context);

		initParams();
	}

	protected void logText(String string) {
		Log.i("GpsLib", string);
		Utils.writeFile(string);
		if (mGpsLibSettingActivity != null) {
			mGpsLibSettingActivity.logText(string);
		}
	}

	private void initParams() {
		if (!mGpsUtil.getInit()) {
			// 初回起動時
			mUtil.setKeyDouble(R.string.interval, INTERVAL);
			mUtil.setKeyDouble(R.string.networkrange, NETWORKRANGE);
			mUtil.setKeyDouble(R.string.timerange, TIMERANGE);
			mUtil.setKeyDouble(R.string.gpsrange, GPSRANGE);
			mUtil.setKeyBoolean(R.string.app_name, true);
		}
	}

	void startGps(final LocationManager localManager) {
		logText("startGPS() init");
		showNotification();
		mGpsUtil.setGpsLibIsRunning(true);
		mLocationManager = localManager;
		startGps(mUtil.getKeyBoolean(R.string.wifi), mUtil
				.getKeyBoolean(R.string.gps));
	}

	/**
	 * GPS測位タイマーを止める
	 */
	void cancelGps() {
		logText("startGPS cancelGPS");
		mGpsUtil.setGpsLibIsRunning(false);
		if (mNewTimer != null) {
			mNewTimer.cancel();
		}
		stopGps();
	}

	/**
	 * GPS測位を止める(タイマーは止めずに測位だけ）
	 */
	void stopGps() {
		logText("GpsLib stopGps()");
		cancelNotification();

		if (mLocationManager != null) {
			// 位置情報の更新を止める (省電力対策)
			logText("GpsLib removelistener");
			mLocationManager.removeUpdates(locationListener);
			mLocationManager.removeGpsStatusListener(gpsListener);
			// // 位置情報サービス登録を破棄
			// mLocationManager = null;
		}
		// logText("Next task start at " + Utils.printdatetime(mNextTime));
	}

	/**
	 * GPS再スタート
	 *
	 * @param mLocationManager2
	 */
	void restartGps(LocationManager locationManager) {
		logText("restartGps()");
		// 測位停止
		cancelGps();
		// 測位開始
		startGps(locationManager);
	}

	public void showNotification() {
		nNo = new Notification(R.drawable.icon, mContext
				.getString(R.string.app_name), System.currentTimeMillis());
		nNo.flags = Notification.FLAG_ONGOING_EVENT;

		Intent i = new Intent(mContext, GpsLibSettingActivity.class);
		PendingIntent pi = PendingIntent.getActivity(mContext, 0, i,
				Intent.FLAG_ACTIVITY_NEW_TASK);
		nNo.setLatestEventInfo(mContext, mContext.getString(R.string.app_name),
				mContext.getString(R.string.app_name), pi);
		nNm.notify(nId, nNo);
	}

	public void cancelNotification() {
		nNm.cancel(nId);
	}

	/*
	 * --- 位置Activity (by Intent) lat,lon, geocording, 精度、取得時刻、実測か予測か
	 *
	 * 最新履歴が５分以内なら返す。
	 *
	 * 保存されている履歴を使って最小二乗法で予測位置を返す。
	 *
	 * 履歴がない場合は最終位置を返す。
	 */

	/**
	 * GPS測位を開始する
	 */
	void startGps(final boolean wifi, final boolean gps) {
		mWifi = wifi;
		mGps = gps;

		logText("startGPS");

		// キャンセル処理
		if (!mGpsUtil.getGpsLibIsRunning()) {
			logText("startGPS was canceled 1");
			return;
		}

		mNow = System.currentTimeMillis();

		Criteria criteria = new Criteria();
		String bestProvider_ = "stop";

		// 利用可能かチェック
		if (!mWifi && !mGps) {
			stopGps();
			mGpsUtil.setGPSProvider(bestProvider_);
			return;
		}

		/*
		 * 最終位置取得 (A) getLastKnownLocation
		 */
		float accuracy = -1;
		long lTime = 0;
		List<String> providers = mLocationManager.getProviders(criteria, false);
		for (String proviver : providers) {
			 Location location = mLocationManager
					.getLastKnownLocation(proviver);
			if (location != null) {
				mLastLocation = location;
				mLastLocationMillis = SystemClock.elapsedRealtime();
				if (location.hasAccuracy()) {
					accuracy = location.getAccuracy();
				}
				mLat = location.getLatitude();
				mLon = location.getLongitude();
				mTime = location.getTime();
				logText("startGPS: getLastKnownLocation lat = " + mLat
						+ " lon = " + mLon + " acc =" + accuracy, lTime);
			}
			/*
			 * Aの時刻チェック timerange秒以内なら、精度チェック 精度がnetworkrange未満以下ならPreferenceにセット
			 */
			checkTimeAndRange(TIMERANGE,mGpsUtil.getNetworkRange());
		}

		// networkチェック
		logText("startGPS:check network provider");

		criteria = new Criteria();
		// criteria.setAccuracy(Criteria.ACCURACY_COARSE); // 要求精度
		// criteria.setPowerRequirement(Criteria.POWER_LOW); // 許容電力消費
		criteria.setAccuracy(Criteria.ACCURACY_COARSE); // 要求精度
		criteria.setPowerRequirement(Criteria.POWER_HIGH); // 許容電力消費

		criteria.setSpeedRequired(false); // 速度不要
		criteria.setAltitudeRequired(false); // 高度不要
		criteria.setBearingRequired(false); // 方位不要
		criteria.setCostAllowed(false); // 費用の発生不可？

		mNewTimer = new NewCountdownTimer(NETWORKTIMER, TICKERINTERVAL) {
			public void onTick(long millisUntilFinished) {
				logText("startGPS:wating network provider..."
						+ millisUntilFinished);
			}

			@Override
			public void onCancel() {
				// GPS計測に移行
				// キャンセル処理
				if (!mGpsUtil.getGpsLibIsRunning()) {
					logText("startGPS was canceled");
				} else {
					logText("startGPS: good data was taken. move to GPS mode");
					checkNetworkGpsData();
				}
			}

			@Override
			public void onFinish() {
				// GPS計測に移行
				checkNetworkGpsData();
			}

		};
		mRange = mGpsUtil.getNetworkRange();

		// ネットワーク測位の情報を取得する
		bestProvider_ = mLocationManager.getBestProvider(criteria, true);
		mLocationManager.requestLocationUpdates(bestProvider_, 1000, 1,
				locationListener);
		logText("startGPS: Using " + bestProvider_);

		// 現状のプロバイダーをセット
		mGpsUtil.setGPSProvider(bestProvider_);

		// NETWORKTIMER秒待つ。取得データの精度がnetworkrangeｍ未満以下なら採用。履歴に保存して終了。
		logText("startGPS:start network provider...");

		mNewTimer.start();
	}

	protected void checkNetworkGpsData() {
		logText("startGPS:wating network provider...finish");
		// 取得データチェック
		checkTimeAndRange(TIMERANGE, mGpsUtil.getNetworkRange());

		// キャンセル処理
		if (!mGpsUtil.getGpsLibIsRunning()) {
			logText("startGPS was canceled on checkNetworkGpsData");
			return;
		}

		// GPSチェック
		if (mGps) {
			// GPSに切り替え
			logText("startGPS:switch to GPS");
			waitSatteliteCount();
		} else {
			// GPSが使えないので終了
			logText("startGPS:GPS not avaiable");
			stopGps();
			return;
		}

	}

	private void waitSatteliteCount() {
		mNewTimer = new NewCountdownTimer(GPSSATTELITETIMER, TICKERINTERVAL) {
			public void onTick(long millisUntilFinished) {
				logText("startGPS:wating sattelite count..."
						+ millisUntilFinished);
			}

			@Override
			public void onCancel() {
				if (!mGpsUtil.getGpsLibIsRunning()) {
					logText("startGPS was canceled");
				} else {
					waitGpsLocation();
				}

			}

			@Override
			public void onFinish() {
				waitGpsLocation();
			}

		};

		// GPSが使えるので
		// GPSSATTELITETIMER秒,衛星数をカウント
		mLocationManager.removeUpdates(locationListener);
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				1000, 1, locationListener);
		mLocationManager.addGpsStatusListener(gpsListener);
		logText("startGPS:counting sattelites...");

		mNewTimer.start();
	}

	protected void waitGpsLocation() {
		logText("startGPS:counting sattelites..."
				+ mGpsUtil.getSatteliteCount());
		mLocationManager.removeGpsStatusListener(gpsListener);

		// 取得に十分ならGPSWORKTIMER秒待つ
		if (mGpsUtil.getSatteliteCount() >= SATTELITECOUNT) {
			mNewTimer = new NewCountdownTimer(GPSWORKTIMER, TICKERINTERVAL) {
				@Override
				public void onTick(long millisUntilFinished) {
					logText("startGPS:wating gps location..."
							+ millisUntilFinished);
				}

				@Override
				public void onCancel() {
					if (!mGpsUtil.getGpsLibIsRunning()) {
						logText("startGPS was canceled");
					} else {
						checkGpsLocation();
					}
				}

				@Override
				public void onFinish() {
					checkGpsLocation();
				}

			};
			mRange = mGpsUtil.getGPSrange();
			mLocationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
			logText("startGPS:waiting GPS...");
			mNewTimer.start();
		} else {
			// 衛星が取れないので終了
			logText("startGPS:GPS:sattelites aren't enough.");
			stopGps();
		}

	}

	protected void checkGpsLocation() {
		logText("startGPS:waiting GPS...finish");
		stopGps();
		checkTimeAndRange(mUtil.getKeyLong(R.string.timerange), mUtil
				.getKeyDouble(R.string.gpsrange));
	}

	private boolean checkTimeAndRange(long timerange, double range) {
		long diff = timerange - (mNow - mTime);
		logText("checkTimeAndRange:acc = " + mAcc + " now="
				+ Utils.printdatetime(mNow) + " last="
				+ Utils.printdatetime(mLastLocationMillis) + " diff=" + diff);
		if (diff > 0) {
			if (mAcc < range) {
				// 精度もいいので履歴とPreferenceに入れる
				logText("startGPS:good range");
				mLastLocationMillis = mNow;
				setLocations(mLat, mLon, mAcc, mTime);
				return true;
			} else {
				// 精度が悪いので履歴にのみ入れる
				logText("startGPS::bad range");
				setLocationsToHistory(mLat, mLon, mAcc, mTime);
				return false;
			}
		} else {
			// 古い場合は履歴にも入れない
			logText("startGPS:too old");
			return false;
		}
	}

	private LocationListener locationListener = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {
			if (location == null) {
				return;
			}
			mLastLocationMillis = System.currentTimeMillis();

			// Do something.
			mLat = location.getLatitude();
			mLon = location.getLongitude();
			mAcc = location.getAccuracy();
			mTime = location.getTime();

			logText("onLocationChanged: lat = " + mLat + " lon = " + mLon
					+ " acc = " + mAcc);
			mLastLocation = location;

			if (checkTimeAndRange(mUtil.getKeyLong(R.string.timerange), mRange)) {
				// いい値がとれたのでタイマーをキャンセル
				logText("LocationLisner: good data! Timer cancel.");
				mNewTimer.cancel();
			}
		}

		@Override
		public void onProviderDisabled(String arg0) {
			logText("onProviderDisabled:" + arg0);
		}

		@Override
		public void onProviderEnabled(String arg0) {
			logText("onProviderEnabled:" + arg0);

		}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			logText("onStatusChanged:" + arg0);

		}
	};

	private long mLastLocationMillis;
	private Location mLastLocation;
	private boolean isGPSFix;

	private GpsStatus.Listener gpsListener = new GpsStatus.Listener() {

		public void onGpsStatusChanged(int event) {
			switch (event) {
			case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
				logText("onGpsStatusChange: GPS_EVENT_SATELLITE_STATUS");
				if (mLastLocation != null) {
					isGPSFix = (SystemClock.elapsedRealtime() - mLastLocationMillis) < 3000;
				}

				GpsStatus xGpsStatus = mLocationManager.getGpsStatus(null);
				Iterable<GpsSatellite> iSatellites = xGpsStatus.getSatellites();
				Iterator<GpsSatellite> it = iSatellites.iterator();
				int c = 0;
				while (it.hasNext()) {
					GpsSatellite oSat = (GpsSatellite) it.next();
					logText("onGpsStatusChange: Satellites: " + oSat.getSnr());
					c++;
				}
				mGpsUtil.setSatteliteCount(c);

				if (mGpsUtil.getSatteliteCount() >= SATTELITECOUNT) {
					// いい値がとれたのでタイマーをキャンセル
					mNewTimer.cancel();
				}

				if (isGPSFix) { // A fix has been acquired.
					// Do something

					break;
				} else { // The fix has been lost.
					// Do something.
					logText("onGpsStatusChange: isGPSFix = false");
				}

				break;
			case GpsStatus.GPS_EVENT_FIRST_FIX:
				// Do something.
				isGPSFix = true;
				logText("onGpsStatusChange: GPS_EVENT_FIRST_FIX");
				// setSatteliteCount(0);
				break;
			}
		}
	};

	private void logText(String string, long time) {
		Log.i("GpsLib", time + string);
	}

	// 鮮度を考慮して履歴に追加
	public void setLocations(double lat, double lon, double accuracy,
			double time) {
		if ((SystemClock.elapsedRealtime() - mLastLocationMillis) < mUtil
				.getKeyLong(R.string.timerange)) {
			setLocationsToHistory(lat, lon, accuracy, time);
			// calc(System.currentTimeMillis());
			mGpsUtil.setLocationsToPreference(lat, lon, accuracy, time);
		}
	}

	// 履歴に追加。
	public void setLocationsToHistory(double lat, double lon, double accuracy,
			double time) {
		double time0 = mGpsUtil.getTime();
		if (time0 > 0) {
			float[] results = new float[1];
			Location.distanceBetween(lat, lon, mGpsUtil.getLat(), mGpsUtil
					.getLon(), results);
			if (results[0] < DISTANCELIMIT) {
				// 異常値の時は履歴に入れない
				mDb.clearRow(mDb.table.geo_table);
				mDb.table.geo_table.lat = lat;
				mDb.table.geo_table.lon = lon;
				mDb.table.geo_table.acc = accuracy;
				mDb.table.geo_table.time = time;
				mDb.insert(mDb.table.geo_table);
			}
		} else {
			mDb.clearRow(mDb.table.geo_table);
			mDb.table.geo_table.lat = lat;
			mDb.table.geo_table.lon = lon;
			mDb.table.geo_table.acc = accuracy;
			mDb.table.geo_table.time = time;
			mDb.insert(mDb.table.geo_table);
		}
	}

}
