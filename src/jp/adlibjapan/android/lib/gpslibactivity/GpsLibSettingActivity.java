package jp.adlibjapan.android.lib.gpslibactivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import jp.adlibjapan.android.lib.gpslibactivity.service.GpsLib;
import jp.adlibjapan.android.lib.gpslibactivity.service.GpsLibAlarm;
import jp.adlibjapan.android.lib.gpslibactivity.service.GpsLibService;
//import jp.adlibjapan.android.lib.gpslibactivity.service.GpsLibServiceInterface;
import jp.adlibjapan.android.lib.gpslibactivity.service.GpsUtil;
import jp.adlibjapan.android.lib.matrix.MinR2;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class GpsLibSettingActivity extends Activity
// implements ServiceConnection
{
	protected static final int WIFI = GpsUtil.WIFI;
	protected static final int GPS = GpsUtil.GPS;
	// private GpsLibServiceInterface mGpsUtil;

	long elapsed;
	private TimerTask mTask;
	private Timer mTimer;
	private static TextView mTv;
	private static Activity mActivity;
	final static long INTERVAL = 5000;
	final static long TIMEOUT = 5000000;

	Map<Long, Integer> INTERVALMAP = new HashMap<Long, Integer>() {
		{
			put(1L * 60 * 1000, 0);
			put(3L * 60 * 1000, 1);
			put(5L * 60 * 1000, 2);
			put(10L * 60 * 1000, 3);
		}
	};
	Map<Long, Integer> NETWORKRANGEMAP = new HashMap<Long, Integer>() {
		{
			put(100L, 0);
			put(500L, 1);
			put(1000L, 2);
			put(5000L, 3);
		}
	};
	Map<Long, Integer> GPSRANGEMAP = new HashMap<Long, Integer>() {
		{
			put(20L, 0);
			put(50L, 1);
			put(200L, 2);
		}
	};
	private GpsUtil mGpsUtil;
	private LocationManager mLocationManager;

	/*
	 * 位置activity
	 *
	 * サービスの起動・停止 利用位置情報プロバイダ設定 チェック頻度設定 精度設定（network, gps) 現在位置表示（実測値、予測値）
	 * 履歴クリア SPモード設定状態表示
	 */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);

		mActivity = this;

		mGpsUtil = new GpsUtil(this);
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		GpsLib.mGpsLibSettingActivity = this;

		// startGps();
		mTv = (TextView) findViewById(R.id.tvLocation);
		setEvents();

		Intent serviceIntent = new Intent(this, GpsLibAlarm.class);
		startService(serviceIntent);

		// startService(new Intent(GpsLibSettingActivity.this,
		// GpsLibService.class));
		// bindService(new Intent(this, GpsLibService.class), this,
		// Context.BIND_AUTO_CREATE);

	}

	@Override
	protected void onStop() {
		super.onStop();
		cancelTimers();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// unbindService(GpsLibSettingActivity.this);
		// stopGps();
	}

	@Override
	protected void onResume() {
		// // 接続されていない場合は接続する
		// if (isConnected_ == false) {
		// bindService(new Intent(this, GpsLibService.class), this,
		// Context.BIND_AUTO_CREATE);
		// isConnected_ = true;
		// }

		setTimers();
		setRanges();
		setGpsSwitch(WIFI, mGpsUtil.getWifi());
		setGpsSwitch(GPS, mGpsUtil.getGPS());

		// Button bt = (Button) findViewById(R.id.bShowMap);
		// if (mGpsUtil.checkGpsStatus(WIFI) || mGpsUtil.checkGpsStatus(GPS)) {
		// bt.setVisibility(Button.VISIBLE);
		// } else {
		// bt.setVisibility(Button.INVISIBLE);
		// }
		super.onResume();
	}

	// @Override
	// public void onServiceConnected(ComponentName name, IBinder service) {
	// mGpsUtil = GpsLibServiceInterface.Stub.asInterface(service);
	//
	// try {
	// setGpsSwitch(WIFI, mGpsUtil.getWifi());
	// setGpsSwitch(GPS, mGpsUtil.getGPS());
	// setRanges();
	// } catch (RemoteException e) {
	// // TODO 自動生成された catch ブロック
	// e.printStackTrace();
	// }
	// }

	private void setRanges() {

		Spinner sp = (Spinner) findViewById(R.id.spRefresh);
		// try {
		if (mGpsUtil.getInit()) {
			sp.setSelection(INTERVALMAP.get(mGpsUtil.getInterval()));
			sp = (Spinner) findViewById(R.id.spNetworkRange);
			sp.setSelection(NETWORKRANGEMAP.get(mGpsUtil.getNetworkRange()));
			sp = (Spinner) findViewById(R.id.spGpsRange);
			sp.setSelection(GPSRANGEMAP.get(mGpsUtil.getGPSrange()));
		}
		// } catch (RemoteException e) {
		// // TODO 自動生成された catch ブロック
		// e.printStackTrace();
		// }
	}

	// @Override
	// public void onServiceDisconnected(ComponentName arg0) {
	// mGpsUtil = null;
	// }

	/*
	 * 状態表示
	 */
	private void setTimers() {

	}

	private void setTimers1() {
		if (mTask != null) {
			mTask.cancel();
			mTask = null;
		}
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}

		mTask = new TimerTask() {

			@Override
			public void run() {
				elapsed += INTERVAL;
				if (elapsed >= TIMEOUT) {
					this.cancel();
					// displayText("finished");
					return;
				}
				// if(some other conditions)
				// this.cancel();
				// logText("seconds elapsed: " + elapsed / 1000);
				showStatus();
			}
		};
		mTimer = new Timer();
		mTimer.scheduleAtFixedRate(mTask, 0, INTERVAL);
	}

	private void cancelTimers() {
		if (mTask != null) {
			mTask.cancel();
			mTask = null;
		}
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
	}

	protected void showStatus() {
		// try {
		if (mGpsUtil != null) {
			logText("\nStatus:"
					+ mGpsUtil.getGPStatus()
					+ ","
					+ "lat:"
					+ String.valueOf(mGpsUtil.getLat())
					+ ","
					+ "lon:"
					+ String.valueOf(mGpsUtil.getLon())
					+ ","
					+ "acc:"
					+ String.valueOf(mGpsUtil.getAcc())
					+ ","
					+ "time:"
					+ new SimpleDateFormat("yyyyMMdd HHmmss").format(mGpsUtil
							.getTime()));
		}
		// } catch (RemoteException e) {
		// // TODO 自動生成された catch ブロック
		// e.printStackTrace();
		// }
	}

	private void setEvents() {
		ToggleButton tg = (ToggleButton) findViewById(R.id.tbgpslib);
		tg.setTextOff(getString(R.string.gpslib));
		tg.setTextOn(getString(R.string.gpslib));
		tg.setChecked(mGpsUtil.getGpsLibServiceIsActive());
		tg.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				ToggleButton tg = (ToggleButton) findViewById(R.id.tbgpslib);
				tg.setChecked(arg1);
				mGpsUtil.setGpsLibServiceIsActive(arg1);
				Intent serviceIntent = new Intent(mActivity, GpsLibAlarm.class);
				startService(serviceIntent);
			}
		});

		tg = (ToggleButton) findViewById(R.id.tbWifi);
		tg.setTextOff("Use Wifi");
		tg.setTextOn("Use Wifi");
		tg.setChecked(false);
		tg.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if (arg1) {
					if (chkGpsService(GpsUtil.WIFI)) {
						setGpsSwitch(WIFI, true);
					}
				} else {
					setGpsSwitch(WIFI, false);
				}
			}
		});

		tg = (ToggleButton) findViewById(R.id.tbgps);
		tg.setTextOff("Use GPS");
		tg.setTextOn("Use GPS");
		tg.setChecked(false);
		tg.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if (arg1) {
					if (chkGpsService(GpsUtil.GPS)) {
						setGpsSwitch(GPS, true);
					}
				} else {
					setGpsSwitch(GPS, false);
				}
				// try {
				// mGpsUtil.restartGpsService();
				// } catch (RemoteException e) {
				// // TODO 自動生成された catch ブロック
				// e.printStackTrace();
				// }
			}
		});

		CheckBox cb = (CheckBox) findViewById(R.id.cbGeocoding);
		cb.setChecked(mGpsUtil.getGeoCordingEnable());
		cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				mGpsUtil.setGeoCordingEnable(isChecked);
			}
		});

		Button bt = (Button) findViewById(R.id.bShowMap);
		// bt.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View arg0) {
		// Intent mi;
		// try {
		// String url = "geo:"
		// + String.valueOf(mGpsUtil.getLat()) + ","
		// + String.valueOf(mGpsUtil.getLon());
		// Log.i("GpsLib", url);
		// mi = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		// startActivity(mi);
		// } catch (RemoteException e) {
		// // TODO 自動生成された catch ブロック
		// e.printStackTrace();
		// }
		// }
		// });

		// bt = (Button) findViewById(R.id.bShowMapCalc);
		// bt.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View arg0) {
		// try {
		// Log.i("GpsLib", "calc lat = " + mGpsUtil.getLat());
		// } catch (RemoteException e) {
		// // TODO 自動生成された catch ブロック
		// e.printStackTrace();
		// }
		// }
		// });

		bt = (Button) findViewById(R.id.bClearlog);
		bt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Utils.writeFileClear();
			}
		});

		bt = (Button) findViewById(R.id.bShowlog);
		bt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				logText("clear");
			}
		});

		Spinner sp = (Spinner) findViewById(R.id.spRefresh);
		sp.setSelection(2);
		sp.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int pos,
					long id) {
				// try {
				switch (pos) {
				case 0:
					mGpsUtil.setInterval(1 * 60 * 1000);
					break;
				case 1:
					mGpsUtil.setInterval(3 * 60 * 1000);
					break;
				case 2:
					mGpsUtil.setInterval(5 * 60 * 1000);
					break;
				case 3:
					mGpsUtil.setInterval(10 * 60 * 1000);
					break;

				default:
					break;
				}
				// } catch (RemoteException e) {
				// // TODO 自動生成された catch ブロック
				// e.printStackTrace();
				// }
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO 自動生成されたメソッド・スタブ

			}
		});

		sp = (Spinner) findViewById(R.id.spNetworkRange);
		sp.setSelection(1);
		sp.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int pos,
					long id) {
				// try {
				switch (pos) {
				case 0:
					mGpsUtil.setNetworkRange(100);
					break;
				case 1:
					mGpsUtil.setNetworkRange(500);
					break;
				case 2:
					mGpsUtil.setNetworkRange(1000);
					break;
				case 3:
					mGpsUtil.setNetworkRange(5000);
					break;

				default:
					break;
				}
				// } catch (RemoteException e) {
				// // TODO 自動生成された catch ブロック
				// e.printStackTrace();
				// }
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO 自動生成されたメソッド・スタブ

			}
		});
		sp = (Spinner) findViewById(R.id.spGpsRange);
		sp.setSelection(1);
		sp.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int pos,
					long id) {
				// try {
				switch (pos) {
				case 0:
					mGpsUtil.setGpsRange(20);
					break;
				case 1:
					mGpsUtil.setGpsRange(50);
					break;
				case 2:
					mGpsUtil.setGpsRange(200);
					break;

				default:
					break;
				}
				// } catch (RemoteException e) {
				// // TODO 自動生成された catch ブロック
				// e.printStackTrace();
				// }
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO 自動生成されたメソッド・スタブ

			}
		});

	}

	public void setGpsSwitch(int provider, boolean flag) {
		ToggleButton tg = null;
		switch (provider) {
		case WIFI:
			tg = (ToggleButton) findViewById(R.id.tbWifi);
			mGpsUtil.setWifi(flag);
			break;
		case GPS:
			tg = (ToggleButton) findViewById(R.id.tbgps);
			mGpsUtil.setGPS(flag);
			break;
		}
		tg.setChecked(flag);
	}

	// /
	// GPSが有効かCheck
	// 有効になっていなければ、設定画面の表示確認ダイアログ
	public boolean chkGpsService(final int provider) {

		boolean flag = checkGpsStatus(provider);
		// GPSセンサーが利用可能か？
		if (!flag) {

			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					mActivity);
			switch (provider) {
			case GPS:
				alertDialogBuilder
						.setMessage("GPSの位置情報利用が有効になっていません。\n有効化しますか？");
				break;
			case WIFI:
				alertDialogBuilder
						.setMessage("Wifiの位置情報利用が有効になっていません。\n有効化しますか？");
				break;

			}
			// GPS設定画面起動用ボタンとイベントの定義
			alertDialogBuilder.setCancelable(false).setPositiveButton("設定起動",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							Intent callGPSSettingIntent = new Intent(
									android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
							mActivity.startActivity(callGPSSettingIntent);
						}
					});
			// キャンセルボタン処理
			alertDialogBuilder.setNegativeButton("キャンセル",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							setGpsSwitch(provider, false);
							dialog.cancel();
						}
					});
			AlertDialog alert = alertDialogBuilder.create();
			// 設定画面へ移動するかの問い合わせダイアログを表示
			alert.show();
		}
		return flag;
	}

	public boolean checkGpsStatus(int provider) {
		switch (provider) {
		case WIFI:
			return mLocationManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		case GPS:
			return mLocationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);
		}
		return false;
	}

	private void logText(String string, long time) {
		logText(string + " "
				+ new SimpleDateFormat("yyyyMMdd HHmmss").format(time));
	}

	private void logText(String s, double val) {
		logText(s + String.valueOf(val));
	}

	private void logText1(String string) {
		final String s = new SimpleDateFormat("yyyyMMdd HHmmss")
				.format(new Date())
				+ " " + string + "\n" + mTv.getText().toString();
		Log.i("GpsLib", s);
	}

	public static void logText(String string) {
		final String s;
		if (string.compareTo("clear") == 0) {
			s = "clear log";
		} else {
			s = new SimpleDateFormat("yyyyMMdd HHmmss").format(new Date())
					+ " " + string + "\n" + mTv.getText().toString();
		}
		mActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mTv.setText(s);
			}
		});
	}

}
