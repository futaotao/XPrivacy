package biz.bokhorst.xprivacy;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

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
					NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
					notificationBuilder.setSmallIcon(R.drawable.ic_launcher);
					notificationBuilder.setContentTitle(context.getString(R.string.app_name));
					notificationBuilder.setContentText(context.getString(R.string.msg_corrupt));
					notificationBuilder.setWhen(System.currentTimeMillis());
					notificationBuilder.setAutoCancel(true);
					Notification notification = notificationBuilder.build();

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
			NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
			notificationBuilder.setSmallIcon(R.drawable.ic_launcher);
			notificationBuilder.setContentTitle(context.getString(R.string.app_name));
			notificationBuilder.setContentText(context.getString(R.string.app_notenabled));
			notificationBuilder.setWhen(System.currentTimeMillis());
			notificationBuilder.setAutoCancel(true);
			if (pi != null)
				notificationBuilder.setContentIntent(pi);
			Notification notification = notificationBuilder.build();

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
		preferences.edit().putString(PrivacyManager.cSettingSerial, PrivacyManager.GetRandomSerial()).commit();
		preferences.edit().putString(PrivacyManager.cSettingMac, PrivacyManager.GetRandomMac()).commit();
		preferences.edit().putString(PrivacyManager.cSettingImei, PrivacyManager.GetRandomIMEI()).commit();
		preferences.edit().putString(PrivacyManager.cSettingId, PrivacyManager.GetRandomAndroidId()).commit();
		preferences.edit().putString(PrivacyManager.cSettingMcc, PrivacyManager.GetRandomMcc()).commit();
		preferences.edit().putString(PrivacyManager.cSettingMnc, PrivacyManager.GetRandomMnc()).commit();
		preferences.edit().putString(PrivacyManager.cSettingSubscriber, PrivacyManager.GetRandomSubscriberId())
				.commit();
	}
}
