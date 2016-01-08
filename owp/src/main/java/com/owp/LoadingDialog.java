package com.owp;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;


public class LoadingDialog extends Dialog {

	private Context context;

	public LoadingDialog(Context context) {
		super(context, R.style.dialog_router);
		View contentView = LayoutInflater.from(context).inflate(
				R.layout.loading_dialog, null);
		setContentView(contentView);
		this.context = context;

	}


}
