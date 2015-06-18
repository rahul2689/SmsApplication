package com.hc1mg.SmsApp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsMessage;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SmsBroadcastReceiver extends BroadcastReceiver {

	public static final String SMS_BUNDLE = "pdus";
	private String mPhoneNumber = "";
	private String mMsgBody = "";
	private long mTimeMillis;
	private Context mContext;
	private String mDateText;
	static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";
	private String mSmsMessageStr = "";
	private int notificationId = 111;

	public void onReceive(Context context, Intent intent) {
		mContext = context;
		NotificationManager mNotificationManager = (NotificationManager) mContext
				.getSystemService(Context.NOTIFICATION_SERVICE);
		if (intent.getAction().equals(ACTION)) {
			Bundle intentExtras = intent.getExtras();
			if (intentExtras != null) {
				Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
				for (int i = 0; i < sms.length; ++i) {
					SmsMessage smsMessage = SmsMessage
							.createFromPdu((byte[]) sms[i]);

					mMsgBody = smsMessage.getMessageBody().toString();
					mPhoneNumber = smsMessage.getOriginatingAddress();
					mTimeMillis = smsMessage.getTimestampMillis();

					Date date = new Date(mTimeMillis);
					SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy");
					mDateText = format.format(date);

					mSmsMessageStr += mPhoneNumber + " at " + "\t" + mDateText
							+ "\n";
					mSmsMessageStr += mMsgBody + "\n";
				}
				ReceiveSmsActivity inst = ReceiveSmsActivity.instance();
				SmsInfo info = new SmsInfo();
				info.setPhoneNumber(mPhoneNumber);
				info.setMessageBody(mMsgBody);
				info.setDate(mDateText);
				info.setIsReadOrNot(false);
				inst.updateList(info);

			}
			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
					mContext);
			mBuilder.setContentTitle("New Message");
			mBuilder.setContentText(mSmsMessageStr);
			mBuilder.setTicker(mPhoneNumber + ": " + mMsgBody);
			mBuilder.setSmallIcon(R.drawable.ic_launcher);

			Intent resultIntent = new Intent(mContext, ReceiveSmsActivity.class);
			resultIntent.putExtra("notificationId", notificationId);
			TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
			stackBuilder.addParentStack(ReceiveSmsActivity.class);
			stackBuilder.addNextIntent(resultIntent);
			PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
					0, PendingIntent.FLAG_ONE_SHOT);
			mBuilder.setContentIntent(resultPendingIntent);
			mNotificationManager.notify(notificationId, mBuilder.build());
		}
	}
}