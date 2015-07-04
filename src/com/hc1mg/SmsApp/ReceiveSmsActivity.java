package com.hc1mg.SmsApp;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.PhoneLookup;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.baoyz.swipemenulistview.SwipeMenuListView.OnMenuItemClickListener;

public class ReceiveSmsActivity extends Activity implements OnItemClickListener {

	private static ReceiveSmsActivity inst;
	private List<SmsInfo> smsMessagesListRead = new ArrayList<SmsInfo>();
	private List<SmsInfo> smsMessagesListUnRead = new ArrayList<SmsInfo>();
	private List<SmsInfo> smsMessagesList = new ArrayList<SmsInfo>();
	private SwipeMenuListView smsListView;
	private SmsAdapter smsAdapter;
	private Button buttonCompose;
	private String mDateText;
	private String smsMessageStr;
	private View mImageButton;

	public static ReceiveSmsActivity instance() {
		if (inst == null) {
			inst = new ReceiveSmsActivity();
		}
		return inst;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_receive_sms);
		smsListView = (SwipeMenuListView) findViewById(R.id.lv_receive_sms_list);
		smsAdapter = new SmsAdapter(ReceiveSmsActivity.this);
		smsListView.setAdapter(smsAdapter);
		mImageButton = (ImageButton) findViewById(R.id.image_button_compose);
		smsListView.setOnItemClickListener(this);
		mImageButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				goToCompose();
			}
		});

		SwipeMenuCreator creator = createListSwipeItem();
		smsListView.setMenuCreator(creator);
		smsListView.setOnMenuItemClickListener(swipeItemClickListener());
	}

	@Override
	public void onStart() {
		List<SmsInfo> listRead = refreshSmsInboxRead();
		List<SmsInfo> listUnread = refreshSmsInboxUnread();
		smsMessagesList.addAll(listUnread);
		smsMessagesList.addAll(listRead);
		smsAdapter.updateMsgList(smsMessagesList);
		super.onStart();
		inst = this;
	}

	@Override
	public void onResume() {
		super.onResume();
	}
	private OnMenuItemClickListener swipeItemClickListener() {
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

	private SwipeMenuCreator createListSwipeItem() {
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
							Uri.parse("content://sms/" + threadId), null, null);
					return;
				}
			} while (smsInboxCursor.moveToNext());
		} catch (Exception e) {

		}
	}

	public void goToCompose() {
		Intent intent = new Intent(ReceiveSmsActivity.this,
				ComposeSmsActivity.class);
		startActivity(intent);
	}

	public List<SmsInfo> refreshSmsInboxRead() {
		ContentResolver contentResolver = getContentResolver();
		Cursor smsInboxCursor = contentResolver.query(
				Uri.parse("content://sms/inbox"), null, "read = 1", null, null);
		smsInboxCursor.moveToFirst();
		int indexBody = smsInboxCursor.getColumnIndex("body");
		int indexAddress = smsInboxCursor.getColumnIndex("address");
		smsMessagesListRead.clear();
		do {
			SmsInfo info = extractSmsInfo(smsInboxCursor, indexBody,
					indexAddress, true);
			smsMessagesListRead.add(info);
		} while (smsInboxCursor.moveToNext());
		return smsMessagesListRead;
	}

	public List<SmsInfo> refreshSmsInboxUnread() {
		ContentResolver contentResolver = getContentResolver();
		Cursor smsInboxCursor = contentResolver.query(
				Uri.parse("content://sms/inbox"), null, "read = 0", null, null);
		smsInboxCursor.moveToFirst();
		int indexBody = smsInboxCursor.getColumnIndex("body");
		int indexAddress = smsInboxCursor.getColumnIndex("address");
		smsMessagesListUnRead.clear();
		do {
			SmsInfo info = extractSmsInfo(smsInboxCursor, indexBody,
					indexAddress, false);
			smsMessagesListUnRead.add(info);
		} while (smsInboxCursor.moveToNext());
		return smsMessagesListUnRead;
	}

	private SmsInfo extractSmsInfo(Cursor smsInboxCursor, int indexBody,
			int indexAddress, boolean val) {
		String name = getContactName(getApplicationContext(),
				smsInboxCursor.getString(smsInboxCursor
						.getColumnIndex("address")));
		SmsInfo info = prepareSmsObj(smsInboxCursor, indexBody, indexAddress,
				val, name);
		String date = smsInboxCursor.getString(smsInboxCursor
				.getColumnIndex("date"));
		Long timestamp = Long.parseLong(date);
		Date newDate = new Date(timestamp);
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy");
		mDateText = format.format(newDate);
		info.setDate(mDateText);
		return info;
	}

	private SmsInfo prepareSmsObj(Cursor smsInboxCursor, int indexBody,
			int indexAddress, boolean val, String name) {
		SmsInfo info = new SmsInfo();
		info.setName(name);
		info.setPhoneNumber(smsInboxCursor.getString(indexAddress));
		info.setMessageBody(smsInboxCursor.getString(indexBody));
		info.setIsReadOrNot(val);
		return info;
	}

	public void updateList(final SmsInfo info) {
		smsMessagesList.add(0, info);
		if (smsAdapter != null) {
			smsAdapter.notifyDataSetChanged();
		}
	}

	public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
		if (smsMessagesList.get(pos).getName() != null) {
			String name = smsMessagesList.get(pos).getName();
			String message = smsMessagesList.get(pos).getMessageBody();
			smsMessageStr = name + "\n";
			smsMessageStr += message;
		} else {
			String phoneNumber = smsMessagesList.get(pos).getPhoneNumber();
			String message = smsMessagesList.get(pos).getMessageBody();
			smsMessageStr = phoneNumber + "\n";
			smsMessageStr += message;
		}
		Toast.makeText(this, smsMessageStr, Toast.LENGTH_SHORT).show();
	}

	public void goToCompose(View view) {
		Intent intent = new Intent(ReceiveSmsActivity.this,
				ComposeSmsActivity.class);
		startActivity(intent);
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
