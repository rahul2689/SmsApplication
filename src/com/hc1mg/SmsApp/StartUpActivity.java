package com.hc1mg.SmsApp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class StartUpActivity extends Activity {

	private Button buttonCompose;
	private Button buttonInbox;
	private Button buttonOutbox;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		buttonInbox = (Button) findViewById(R.id.button_main_activity_inbox);
		buttonCompose = (Button) findViewById(R.id.button_main_activity_compose);
		buttonOutbox = (Button) findViewById(R.id.button_main_activity_outbox);
		buttonInbox.setOnClickListener(mClickListener);
		buttonCompose.setOnClickListener(mClickListener);
		buttonOutbox.setOnClickListener(mClickListener);
		
	}

	private OnClickListener mClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.button_main_activity_inbox:
				goToInbox();
				break;
			case R.id.button_main_activity_compose:
				goToCompose();
				break;
			case R.id.button_main_activity_outbox:
				goToOutbox();
			}
		}
	};

	public void goToInbox() {
		Intent intent = new Intent(StartUpActivity.this, ReceiveSmsActivity.class);
		startActivity(intent);
	}

	protected void goToOutbox() {
		Intent intent = new Intent(StartUpActivity.this, SentSmsActivity.class);
		startActivity(intent);
	}

	public void goToCompose() {
		Intent intent = new Intent(StartUpActivity.this, ComposeSmsActivity.class);
		startActivity(intent);
	}

}
