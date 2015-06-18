package com.hc1mg.SmsApp;

import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SmsAdapter extends BaseAdapter {

	private List<SmsInfo> mMessageList;
	private Context mContext;
	private ViewHolder viewHolder;

	public SmsAdapter(Context context, List<SmsInfo> smsMessagesList) {
		mContext = context;
		mMessageList = smsMessagesList;
	}

	@Override
	public int getCount() {
		return mMessageList.size();
	}

	@Override
	public SmsInfo getItem(int position) {
		return mMessageList.get(position);
	}

	@Override 
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null || convertView.getTag() == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.inbox_view_adapter, parent, false);
			viewHolder = new ViewHolder(convertView);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		showPhoneNoOrName(position);
		viewHolder.mDateTv.setText(mMessageList.get(position).getDate());
		setMessage(position);
		viewHolder.mMessageTv.setTag(position);
		viewHolder.mMessageTv.setOnClickListener(mClickListener());
		return convertView;
	}

	private void showPhoneNoOrName(int position) {
		if (mMessageList.get(position).getName() != null) {
			viewHolder.mPhoneNumberTv.setText(mMessageList.get(position)
					.getName());
			viewHolder.mRelativeLayoutId.setBackgroundColor(mContext
					.getResources().getColor(R.color.grey));
		} else {
			viewHolder.mPhoneNumberTv.setText(mMessageList.get(position)
					.getPhoneNumber());
			viewHolder.mRelativeLayoutId.setBackgroundColor(mContext
					.getResources().getColor(R.color.white));
		}
	}

	private OnClickListener mClickListener() {
		return new OnClickListener() {

			@Override
			public void onClick(View v) {
				int pos = (Integer) v.getTag();
				if (!mMessageList.get(pos).isIsReadOrNot()) {
					viewHolder.mMessageTv.setTypeface(null, Typeface.NORMAL);
					mMessageList.get(pos).setIsReadOrNot(true);
					notifyDataSetChanged();
				}
			}
		};
	}

	private void setMessage(int position) {
		if (!mMessageList.get(position).isIsReadOrNot()) {
			viewHolder.mMessageTv.setText(mMessageList.get(position)
					.getMessageBody());
			viewHolder.mMessageTv.setTypeface(null, Typeface.BOLD);
			viewHolder.mSmsCheckBox.setVisibility(View.GONE);
		} else {
			viewHolder.mMessageTv.setText(mMessageList.get(position)
					.getMessageBody());
			viewHolder.mMessageTv.setTypeface(null, Typeface.NORMAL);
			viewHolder.mSmsCheckBox.setVisibility(View.VISIBLE);
		}
	}

	class ViewHolder {
		private TextView mPhoneNumberTv;
		private TextView mMessageTv;
		private TextView mDateTv;
		private RelativeLayout mRelativeLayoutId;
		private CheckBox mSmsCheckBox;

		public ViewHolder(View convertView) {
			mRelativeLayoutId = (RelativeLayout) convertView
					.findViewById(R.id.rl_sms_adapter);
			mPhoneNumberTv = (TextView) convertView
					.findViewById(R.id.tv_receive_sms_adapter_contact);
			mMessageTv = (TextView) convertView
					.findViewById(R.id.tv_receive_sms_adapter_msg_boby);
			mDateTv = (TextView) convertView
					.findViewById(R.id.tv_receive_sms_adapter_date);
			mSmsCheckBox = (CheckBox) convertView
					.findViewById(R.id.cb_sms_adapter);
		}
	}
}
