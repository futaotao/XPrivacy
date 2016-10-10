package biz.bokhorst.xprivacy;

import java.io.File;
import java.io.FileOutputStream;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, Intent bootIntent) {
		GenerateRandomVal(context);
		// Start boot update
		Intent changeIntent = new Intent();
		changeIntent.setClass(context, UpdateService.class);
		changeIntent.putExtra(UpdateService.cAction, UpdateService.cActionBoot);
		context.startService(changeIntent);

		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		// Check if Xposed enabled
		if (Util.isXposedEnabled() && PrivacyService.checkClient())
			try {
				if (PrivacyService.getClient().databaseCorrupt()) {
					// Build notification

					Notification notification = new Notification.Builder(context).setSmallIcon(R.drawable.ic_launcher)
							.setContentTitle(context.getString(R.string.app_name))
							.setContentText(context.getString(R.string.msg_corrupt)).setContentIntent(null)
							.setAutoCancel(true).setWhen(System.currentTimeMillis()).build();

					// NotificationCompat.Builder notificationBuilder = new
					// NotificationCompat.Builder(context);
					// notificationBuilder.setSmallIcon(R.drawable.ic_launcher);
					// notificationBuilder.setContentTitle(context.getString(R.string.app_name));
					// notificationBuilder.setContentText(context.getString(R.string.msg_corrupt));
					// //
					// notificationBuilder.setWhen(System.currentTimeMillis());
					// notificationBuilder.setAutoCancel(true);
					// Notification notification = notificationBuilder.build();

					// Display notification
					notificationManager.notify(Util.NOTIFY_CORRUPT, notification);
				} else
					context.sendBroadcast(new Intent("biz.bokhorst.xprivacy.action.ACTIVE"));
			} catch (Throwable ex) {
				Util.bug(null, ex);
			}
		else {
			// Create Xposed installer intent
			// @formatter:off
			Intent xInstallerIntent = new Intent("de.robv.android.xposed.installer.OPEN_SECTION")
				.setPackage("de.robv.android.xposed.installer")
				.putExtra("section", "modules")
				.putExtra("module", context.getPackageName())
				.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			// @formatter:on

			PendingIntent pi = (xInstallerIntent == null ? null : PendingIntent.getActivity(context, 0,
					xInstallerIntent, PendingIntent.FLAG_UPDATE_CURRENT));

			// Build notification
			// NotificationCompat.Builder notificationBuilder = new
			// NotificationCompat.Builder(context);
			// notificationBuilder.setSmallIcon(R.drawable.ic_launcher);
			// notificationBuilder.setContentTitle(context.getString(R.string.app_name));
			// notificationBuilder.setContentText(context.getString(R.string.app_notenabled));
			// // notificationBuilder.setWhen(System.currentTimeMillis());
			// notificationBuilder.setAutoCancel(true);
			// if (pi != null)
			// notificationBuilder.setContentIntent(pi);
			// Notification notification = notificationBuilder.build();

			Notification notification = new Notification.Builder(context).setSmallIcon(R.drawable.ic_launcher)
					.setContentTitle(context.getString(R.string.app_name))
					.setContentText(context.getString(R.string.app_notenabled)).setContentIntent(pi)
					.setAutoCancel(true).setWhen(System.currentTimeMillis()).build();

			// Display notification
			notificationManager.notify(Util.NOTIFY_NOTXPOSED, notification);
		}
	}

	/**
	 * @author Futao
	 * @desc 重启时生成一次 ， 保证此次开机 所有app 都是相同的随机值
	 */
	private void GenerateRandomVal(Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

		String serial = PrivacyManager.GetRandomSerial();
		preferences.edit().putString(PrivacyManager.cSettingSerial, serial).commit();

		String mac = PrivacyManager.GetRandomMac();
		preferences.edit().putString(PrivacyManager.cSettingMac, mac).commit();

		// 通过/system/etc/init.androVM.sh (IMEI) 来修改
		String imei = PrivacyManager.GetRandomIMEI();
		preferences.edit().putString(PrivacyManager.cSettingImei, imei).commit();

		String androidId = PrivacyManager.GetRandomAndroidId();
		preferences.edit().putString(PrivacyManager.cSettingId, androidId).commit();

		String mcc = PrivacyManager.GetRandomMcc();
		preferences.edit().putString(PrivacyManager.cSettingMcc, mcc).commit();

		String mnc = PrivacyManager.GetRandomMnc();
		preferences.edit().putString(PrivacyManager.cSettingMnc, mnc).commit();

		String subscriber = PrivacyManager.GetRandomSubscriberId();
		preferences.edit().putString(PrivacyManager.cSettingSubscriber, subscriber).commit();

		String content = serial + "_" + mac + "_" + imei + "_" + androidId + "_" + mcc + mnc + subscriber;
		try {
			saveToSDCard(content);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @author Futao
	 * @desc 保存内容到sd卡
	 */
	private void saveToSDCard(String content) throws Exception {
		String sdPath = "/sdcard/random";
		File file = new File(sdPath);
		//判断没有时创建一个
		if (!file.exists()) {
			file.createNewFile();
		}
		FileOutputStream outStream = new FileOutputStream(file);
		outStream.write(content.getBytes());
		outStream.close();
	}

}
