package com.hc1mg.SmsApp;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ComposeSmsActivity extends Activity {

	private static final int CONTACT_PICKER_RESULT = 1;
	private Button mSendButton;
	private EditText mPhoneNumberEt;
	private EditText mSmsMessageEt;
	private Button buttonAddContact;
	public int REQUESTCODE = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_compose_sms);
		mSendButton = (Button) findViewById(R.id.btnSendSMS);
		mPhoneNumberEt = (EditText) findViewById(R.id.et_send_sms_phone_no_);
		mSmsMessageEt = (EditText) findViewById(R.id.editTextSMS);
		buttonAddContact = (Button) findViewById(R.id.button_send_sms_add_contact);
		mSendButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				String toPhoneNumber = mPhoneNumberEt.getText().toString();
				String smsMessage = mSmsMessageEt.getText().toString();
				sendSMS(toPhoneNumber, smsMessage);
			}
		});

		buttonAddContact.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
				startActivityForResult(intent, REQUESTCODE);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case CONTACT_PICKER_RESULT:
				final EditText inputPhoneNumber = (EditText) findViewById(R.id.et_send_sms_phone_no_);
				Cursor cursor = null;
				String phoneNumber = "";
				List<String> allNumbers = new ArrayList<String>();
				try {
					ContentResolver cr = getContentResolver();
					Cursor cur = cr.query(
							ContactsContract.Contacts.CONTENT_URI, null, null,
							null, null);
					if (cur.getCount() > 0) {
						while (cur.moveToNext()) {
							String id = cur
									.getString(cur
											.getColumnIndex(ContactsContract.Contacts._ID));
							if (Integer
									.parseInt(cur.getString(cur
											.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
								Cursor pCur = cr
										.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
												null,
												ContactsContract.CommonDataKinds.Phone.CONTACT_ID
														+ " = ?",
												new String[] { id }, null);
								while (pCur.moveToNext()) {
									phoneNumber = pCur
											.getString(pCur
													.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
									allNumbers.add(phoneNumber);
								}
								pCur.close();
							}
						}

					}
				} catch (Exception e) {
				} finally {
					if (cursor != null) {
						cursor.close();
					}

					final CharSequence[] items = allNumbers
							.toArray(new String[allNumbers.size()]);
					AlertDialog.Builder builder = new AlertDialog.Builder(
							ComposeSmsActivity.this);
					builder.setTitle("Choose a number");
					builder.setItems(items,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int item) {
									String selectedNumber = items[item]
											.toString();
									selectedNumber = selectedNumber.replace(
											"-", "");
									inputPhoneNumber.setText(selectedNumber);
								}
							});
					AlertDialog alert = builder.create();
					if (allNumbers.size() > 1) {
						alert.show();
					} else {
						String selectedNumber = phoneNumber.toString();
						selectedNumber = selectedNumber.replace("-", "");
						inputPhoneNumber.setText(selectedNumber);
					}

					if (phoneNumber.length() == 0) {
					}
				}
				break;
			}
		}
	}

	protected void sendSMS(String toPhoneNumber, String smsMessage) {

		try {
			String SENT = "SMS_SENT";
			PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
					new Intent(SENT), 0);
			registerReceiver(new BroadcastReceiver() {
				public void onReceive(Context context, Intent intent) {
					switch (getResultCode()) {
					case Activity.RESULT_OK:
						Toast.makeText(getBaseContext(), "SMS sent",
								Toast.LENGTH_SHORT).show();
						break;
					case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
						Toast.makeText(getBaseContext(), "Generic failure",
								Toast.LENGTH_SHORT).show();
						break;
					case SmsManager.RESULT_ERROR_NO_SERVICE:
						Toast.makeText(getBaseContext(), "No service",
								Toast.LENGTH_SHORT).show();
						break;
					case SmsManager.RESULT_ERROR_NULL_PDU:
						Toast.makeText(getBaseContext(), "Null PDU",
								Toast.LENGTH_SHORT).show();
						break;
					case SmsManager.RESULT_ERROR_RADIO_OFF:
						Toast.makeText(getBaseContext(), "Radio off",
								Toast.LENGTH_SHORT).show();
						break;
					}
				}
			}, new IntentFilter(SENT));
			SmsManager smsManager = SmsManager.getDefault();
			smsManager.sendTextMessage(toPhoneNumber, null, smsMessage, sentPI,
					null);
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), "Sending SMS failed.",
					Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
	}

	public void goToInbox(View view) {
		Intent intent = new Intent(ComposeSmsActivity.this,
				ReceiveSmsActivity.class);
		startActivity(intent);
	}
}