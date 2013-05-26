package jp.adlibjapan.android.lib.gpslibactivity.service;

import jp.adlibjapan.android.lib.gpslibactivity.Utils;
import jpadlibjapan.android.lib.simpledb.GpsLibDb;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

public class GpsLibAlarm extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	private final IBinder binder = new Binder() {
		@Override
		protected boolean onTransact(int code, Parcel data, Parcel reply,
				int flags) throws RemoteException {
			return super.onTransact(code, data, reply, flags);
		}
	};

	private GpsUtil mGpsUtil;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// 起動確認
		mGpsUtil = new GpsUtil(getApplicationContext());
		Context context = getApplicationContext();
		Intent serviceIntent = new Intent(context, GpsLibService.class);

		// サービス自体を利用中か？
		if (mGpsUtil.getGpsLibServiceIsActive()) {
			// Thread thr = new Thread(null, task, "GpsLibAlarm_Service");
			// thr.start();
			if (mGpsUtil.getGpsLibIsRunning()) {
				// GpsLibServiceを停止
				context.stopService(serviceIntent);
			}
			context.startService(serviceIntent);
			setNextTimerAndStop();
			Utils.logText("start Alarm");
		} else {
			// サービス停止
			if (mGpsUtil.getGpsLibIsRunning()) {
				// GpsLibServiceを停止
				context.stopService(serviceIntent);
				Utils.logText("stop Service");
			}
			stopSelf();
			Utils.logText("stop Alarm");
		}
		return START_NOT_STICKY;
	}

	private Runnable task = new Runnable() {

		@Override
		public void run() {
			synchronized (binder) {
				try {
					Context context = getApplicationContext();
					Intent serviceIntent = new Intent(context,
							GpsLibService.class);
					if (mGpsUtil.getGpsLibIsRunning()) {
						// GpsLibServiceを停止
						context.stopService(serviceIntent);
					}
					context.startService(serviceIntent);
				} catch (Exception e) {
				}
			}
			setNextTimerAndStop();
		}
	};

	protected void setNextTimerAndStop() {
		// 次回起動登録
		long now = System.currentTimeMillis();
		PendingIntent alarmSender = PendingIntent.getService(GpsLibAlarm.this,
				0, new Intent(GpsLibAlarm.this, GpsLibAlarm.class), 0);
		AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		am.cancel(alarmSender);
		am.set(AlarmManager.RTC_WAKEUP, now + mGpsUtil.getInterval(),
				alarmSender);
		Utils.logText("Next task start at "
				+ Utils.printdatetime(now + mGpsUtil.getInterval()));
		mGpsUtil.setNextInterval(now + mGpsUtil.getInterval());

		// サービス終了
		stopSelf();
	}
}
