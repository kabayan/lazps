package jp.adlibjapan.android.lib.gpslibactivity;

import java.text.SimpleDateFormat;

import jp.adlibjapan.android.lib.gpslibactivity.service.GpsUtil;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class GpsLibMainActivity extends Activity {

	private double mLat;
	private double mLon;
	private Utils mUtils;
	private String mGeoHex;
	private GpsUtil mGpsUtil;

	/*
	 * --- 位置activity
	 *
	 * サービスの起動・停止 利用位置情報プロバイダ設定 チェック頻度設定 精度設定（network, gps) 現在位置表示（実測値、予測値）
	 * 履歴クリア SPモード設定状態表示 ---
	 */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mUtils = new Utils(this);
		mGpsUtil = new GpsUtil(this);

		if (savedInstanceState == null) {
			// intentで呼ばれたときの処理
			Intent ai = getIntent();
			if (Intent.ACTION_VIEW.equals(ai.getAction())) {
				Intent intent = new Intent();
				intent.putExtra("service_boolean", mGpsUtil
						.getGpsLibServiceIsActive());
				intent.putExtra("running_boolean", mGpsUtil
						.getGpsLibIsRunning());
				intent.putExtra("interval_long", mGpsUtil.getInterval());
				intent.putExtra("lat_double", mGpsUtil.getLat());
				intent.putExtra("lon_double", mGpsUtil.getLon());
				intent.putExtra("acc_double", mGpsUtil.getAcc());
				intent.putExtra("geohex_string", mGpsUtil.getGeohex());
				intent.putExtra("address_string", mGpsUtil.getGeoCording());
				intent.putExtra("time_double", mGpsUtil.getTime());
				setResult(Activity.RESULT_OK, intent);
				finish();
			}
		}
	}

	/*
	 * Intent i = new Intent( getApplicationContext(),
	 * jp.adlibjapan.android.lib.gpslibactivity.GpsLibSettingActivity.class);
	 * i.setAction(Intent.ACTION_VIEW); i.putExtra("time",millss);
	 * startActivityForResult(i, R.string.app_name);
	 *
	 * @Override protected void onActivityResult(int requestCode, int
	 * resultCode, Intent intent) { if (requestCode == R.string.app_name &&
	 * resultCode == RESULT_OK) { // インテントからのパラメータ取得 String text = ""; Bundle
	 * extras = intent.getExtras(); if (extras != null) { mLat =
	 * extras.getDouble("lat_double"); mLon = extras.getDouble("lon_double");
	 *
	 * String status = "サービス起動状態" + ":" + (extras.getBoolean("service_boolean")
	 * ? "true" : "false") + "\n" + "計測中" + ":" +
	 * (extras.getBoolean("running_boolean") ? "true" : "false") + "\n" + "計測頻度"
	 * + ":" + extras.getLong("interval_long") / 1000 + "\n" + "緯度" + ":" +
	 * extras.getDouble("lat_double") + "\n" + "経度" + ":" +
	 * extras.getDouble("lon_double") + "\n" + "精度" + ":" +
	 * extras.getDouble("acc_double") + "\n" + "geohex" + ":" +
	 * extras.getString("geohex_string") + "\n" + "住所" + ":" +
	 * extras.getString("address_string") + "\n" + "位置取得時間" + ":" + new
	 * SimpleDateFormat("yyyyMMdd HHmmss").format(extras
	 * .getDouble("time_double"));
	 *
	 * TextView tv = (TextView) findViewById(R.id.tvLocation);
	 * tv.setText(status); } }
	 */
	private void setEvent() {
		Button bt = (Button) findViewById(R.id.bSetting

		);
		bt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(GpsLibMainActivity.this,
						GpsLibSettingActivity.class);
				startActivity(i);
			}
		});

		bt = (Button) findViewById(R.id.bShowMap);
		bt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent mi;
				String url = "geo:" + mLat + "," + mLon;
				Log.i("GpsLib", url);
				mi = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				startActivity(mi);
			}
		});
		bt = (Button) findViewById(R.id.bShowGeoHex);
		bt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent mi;
				String url = "http://geohex.net/v3.html?code=" + mGeoHex;
				// Log.i("GpsLib", url);
				mi = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				startActivity(mi);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();

		mGpsUtil.calc(System.currentTimeMillis());

		setEvent();
		setStatus();
	}

	// アクティビティ呼び出し結果の取得
	protected void setStatus() {

		mLat = mGpsUtil.getLat();
		mLon = mGpsUtil.getLon();
		mGeoHex = mGpsUtil.getGeohex();

		String status = getString(R.string.service)
				+ ":"
				+ (mGpsUtil.getGpsLibServiceIsActive() ? getString(R.string.servicetrue)
						: getString(R.string.servicefalse))
				+ "\n"
				+ getString(R.string.running)
				+ ":"
				+ (mGpsUtil.getGpsLibIsRunning() ? getString(R.string.runningtrue)
						: getString(R.string.runningfalse))
				+ "\n"
				+ getString(R.string.interval)
				+ ":"
				+ mUtils.getKeyLong(R.string.interval) / 1000
				+ getString(R.string.sec)
				+ "\n"
				+ getString(R.string.lat)
				+ ":"
				+ mLat
				+ "\n"
				+ getString(R.string.lon)
				+ ":"
				+ mLon
				+ "\n"
				+ getString(R.string.acc)
				+ ":"
				+ mUtils.getKeyDouble(R.string.acc)
				+ "\n"
				+ getString(R.string.geohex)
				+ ":"
				+ mGpsUtil.getGeohex()
				+ "\n"
				+ getString(R.string.geocoding)
				+ ":"
				+ mGpsUtil.getGeoCording()
				+ "\n"
				+ getString(R.string.time)
				+ ":"
				+ new SimpleDateFormat("yyyyMMdd HHmmss").format(mGpsUtil
						.getTime())
				+ "\n"
				+ getString(R.string.nextinterval)
				+ ":"
				+ new SimpleDateFormat("yyyyMMdd HHmmss").format(mGpsUtil
						.getNextInterval());

		TextView tv = (TextView) findViewById(R.id.tvLocation);
		tv.setText(status);
	}
}
