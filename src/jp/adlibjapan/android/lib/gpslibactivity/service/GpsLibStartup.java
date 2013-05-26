package jp.adlibjapan.android.lib.gpslibactivity.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

public class GpsLibStartup extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {

		Log.i("GpsLib","onRecive");

		// WAKE_LOCK
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag");
        wl.acquire(200);

		Intent serviceIntent = new Intent(context, GpsLibAlarm.class);
		context.startService(serviceIntent);
	}
}
