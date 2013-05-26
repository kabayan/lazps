package jp.adlibjapan.android.lib.gpslibactivity.service;

import jp.adlibjapan.android.lib.gpslibactivity.Utils;
import jpadlibjapan.android.lib.simpledb.GpsLibDb;
import android.app.Service;
import android.app.backup.RestoreObserver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;

public class GpsLibService extends Service {

	private GpsLib mGpsLib;
	private LocationManager mLocationManager;
	private GpsLibDb mDb;
	private Utils mUtil;

	@Override
	public void onCreate() {
		mDb = new GpsLibDb(this);
		mGpsLib = new GpsLib(this, mDb);
		mUtil = new Utils(this);
		super.onCreate();
	}

	Handler mHandler = new Handler();

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Utils.logText("start GpsLibService");
		// GpsTask task = new GpsTask();
		// task.execute(null);

		new Thread(new Runnable() {
			public void run() {
				mHandler.post(task);
			}
		}, "aaa").start();

		// Thread thr = new Thread(null, task, "GpsLib_Service");
		// thr.start();
		return START_NOT_STICKY;
	}

	public class GpsTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			mLocationManager = getLocationManager();
			mGpsLib.restartGps(mLocationManager);
			return null;
		}

	}

	private Runnable task = new Runnable() {
		@Override
		public void run() {
			mLocationManager = getLocationManager();
			mGpsLib.restartGps(mLocationManager);
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return null;
//		return interfaceImpl_;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		super.onUnbind(intent);
		return true;
	}

	@Override
	public void onDestroy() {
		mGpsLib.cancelGps();
		mDb.close();
		super.onDestroy();
	}

//	private GpsLibServiceInterface.Stub interfaceImpl_ = new GpsLibServiceInterface.Stub() {
//
//		@Override
//		public void restartGpsService() throws RemoteException {
//			mLocationManager = getLocationManager();
//			mGpsLib.restartGps(mLocationManager);
//			// showNotification();
//		}
//
//		@Override
//		public void startGpsService() throws RemoteException {
//			if (!mGpsLib.getGpsLibIsRunning()) {
//				startGps();
//			}
//		}
//
//		@Override
//		public void stopGpsService() throws RemoteException {
//			mLocationManager = null;
//			mGpsLib.cancelGps();
//			// cancelNotification();
//		}
//
//		@Override
//		public void setGPS(boolean use) throws RemoteException {
//			mGpsLib.setGPS(use);
//		}
//
//		@Override
//		public void setWifi(boolean use) throws RemoteException {
//			mGpsLib.setWifi(use);
//		}
//
//		@Override
//		public boolean getGPS() throws RemoteException {
//			return mGpsLib.getGPS();
//		}
//
//		@Override
//		public boolean getWifi() throws RemoteException {
//			return mGpsLib.getWifi();
//		}
//
//		@Override
//		public double getAcc() throws RemoteException {
//			return mGpsLib.getAcc();
//		}
//
//		@Override
//		public String getAddress() throws RemoteException {
//			return mGpsLib.getGeoCording();
//		}
//
//		@Override
//		public String getGpsStatus() throws RemoteException {
//			return mGpsLib.getGPStatus();
//		}
//
//		@Override
//		public double getLat() throws RemoteException {
//			return mGpsLib.getLat();
//		}
//
//		@Override
//		public double getLon() throws RemoteException {
//			return mGpsLib.getLon();
//		}
//
//		@Override
//		public double getTime() throws RemoteException {
//			return mGpsLib.getTime();
//		}
//
//		@Override
//		public double getSatteliteCount() throws RemoteException {
//			return mGpsLib.getSatteliteCount();
//		}
//
//		/*
//		 * timeは　long currentTimeMillis = System.currentTimeMillis();
//		 */
//		@Override
//		public void calc(double time) throws RemoteException {
//			mGpsLib.calc(time);
//		}
//
//		@Override
//		public void setGPSRange(long value) throws RemoteException {
//			mGpsLib.setGpsRange(value);
//			// restartGpsService();
//		}
//
//		@Override
//		public void setNetworkRange(long value) throws RemoteException {
//			mGpsLib.setNetworkRange(value);
//			// restartGpsService();
//		}
//
//		@Override
//		public void setInterval(long value) throws RemoteException {
//			mGpsLib.setInterval(value);
//			// restartGpsService();
//		}
//
//		@Override
//		public long getGPSRange() throws RemoteException {
//			return mGpsLib.getGPSrange();
//		}
//
//		@Override
//		public long getNetworkRange() throws RemoteException {
//			return mGpsLib.getNetworkRange();
//		}
//
//		@Override
//		public long getInterval() throws RemoteException {
//			return mGpsLib.getInterval();
//		}
//
//		@Override
//		public boolean getInit() throws RemoteException {
//			return mGpsLib.getInit();
//		}
//
//		@Override
//		public String getGeohex() throws RemoteException {
//			return mGpsLib.getGeohex();
//		}
//
//	};

	private LocationManager getLocationManager() {
		return (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
	}

	protected void startGps() {
		mLocationManager = getLocationManager();
		mGpsLib.startGps(mLocationManager);
		// showNotification();
	}

}
