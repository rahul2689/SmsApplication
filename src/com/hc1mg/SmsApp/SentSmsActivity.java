package com.hc1mg.SmsApp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.baoyz.swipemenulistview.SwipeMenuListView.OnMenuItemClickListener;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.PhoneLookup;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;

public class SentSmsActivity extends Activity {

	private SwipeMenuListView mSmsListView;
	private List<SmsInfo> smsMessagesList = new ArrayList<SmsInfo>();
	private SmsAdapter smsAdapter;
	private List<String> threadIds = new ArrayList<String>();
	private String mDateText;
	private ImageButton mImageButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sent_sms);
		mSmsListView = (SwipeMenuListView) findViewById(R.id.lv_sent_sms_list);
		mImageButton = (ImageButton) findViewById(R.id.image_button_compose);
		refreshSmsOutbox();
		smsAdapter = new SmsAdapter(getApplicationContext(), smsMessagesList);
		mSmsListView.setAdapter(smsAdapter);
		mImageButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				goToCompose();
			}
		});
		SwipeMenuCreator creator = createSwipeMenuItem();
		mSmsListView.setMenuCreator(creator);
		mSmsListView.setOnMenuItemClickListener(listItemSwipeClickListener());
	}

	private OnMenuItemClickListener listItemSwipeClickListener() {
		return new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(int position, SwipeMenu menu,
					int index) {
				SmsInfo info = smsMessagesList.get(position);
				switch (index) {
				case 0:
					delete(info, position);
					smsMessagesList.remove(position);
					smsAdapter.notifyDataSetChanged();
					break;
				default:
					break;
				}
				return false;
			}
		};
	}

	private SwipeMenuCreator createSwipeMenuItem() {
		SwipeMenuCreator creator = new SwipeMenuCreator() {

			@Override
			public void create(SwipeMenu menu) {
				SwipeMenuItem deleteItem = new SwipeMenuItem(
						getApplicationContext());
				deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
						0x3F, 0x25)));
				deleteItem.setWidth(dp2px(90));
				deleteItem.setIcon(R.drawable.ic_delete);
				menu.addMenuItem(deleteItem);
			}
		};
		return creator;
	}

	private void delete(SmsInfo item, int position) {
		ContentResolver contentResolver = getContentResolver();
		Cursor smsInboxCursor = contentResolver.query(
				Uri.parse("content://sms/"), null, null, null, null);
		smsInboxCursor.moveToPosition(position);
		try {
			do {
				String address = smsInboxCursor.getString(smsInboxCursor
						.getColumnIndex("address"));
				long threadId = smsInboxCursor.getLong(1);
				if (address.equals(item.getPhoneNumber())) {
					getContentResolver().delete(
							Uri.parse("content://sms/" + threadId), null,
							null);
					return;
				}
			}while (smsInboxCursor.moveToNext());
		} catch (Exception e) {
		}
	}

	public void goToCompose() {
		Intent intent = new Intent(SentSmsActivity.this,
				ComposeSmsActivity.class);
		startActivity(intent);
	}

	public List<String> refreshSmsOutbox() {
		ContentResolver contentResolver = getContentResolver();
		Cursor smsInboxCursor = contentResolver.query(
				Uri.parse("content://sms/sent"), null, null, null, null);
		smsInboxCursor.moveToFirst();
		int indexBody = smsInboxCursor.getColumnIndex("body");
		int indexAddress = smsInboxCursor.getColumnIndex("address");
		int threadId = smsInboxCursor.getColumnIndex("thread_id");
		smsMessagesList.clear();
		do {
			extractSentSms(smsInboxCursor, indexBody, indexAddress, threadId);
		} while (smsInboxCursor.moveToNext());
		return threadIds;
	}

	private void extractSentSms(Cursor smsInboxCursor, int indexBody,
			int indexAddress, int threadId) {
		String name = getContactName(getApplicationContext(),
				smsInboxCursor.getString(smsInboxCursor
						.getColumnIndex("address")));
		SmsInfo info = new SmsInfo();
		info.setName(name);
		info.setPhoneNumber(smsInboxCursor.getString(indexAddress));
		info.setMessageBody(smsInboxCursor.getString(indexBody));
		info.setIsReadOrNot(true);
		String date = smsInboxCursor.getString(smsInboxCursor
				.getColumnIndex("date"));
		Long timestamp = Long.parseLong(date);
		Date newDate = new Date(timestamp);
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy");
		mDateText = format.format(newDate);
		info.setDate(mDateText);
		smsMessagesList.add(info);
	}

	public String getContactName(Context context, String phoneNumber) {
		ContentResolver cr = context.getContentResolver();
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(phoneNumber));
		Cursor cursor = cr.query(uri,
				new String[] { PhoneLookup.DISPLAY_NAME }, null, null, null);
		if (cursor == null) {
			return null;
		}
		String contactName = null;
		if (cursor.moveToFirst()) {
			contactName = cursor.getString(cursor
					.getColumnIndex(PhoneLookup.DISPLAY_NAME));
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		return contactName;
	}

	private int dp2px(int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				getResources().getDisplayMetrics());
	}
}
