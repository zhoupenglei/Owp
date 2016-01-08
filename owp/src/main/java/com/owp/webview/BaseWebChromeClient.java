package com.owp.webview;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.owp.R;


public class BaseWebChromeClient extends WebChromeClient {
    private Context context;

    public BaseWebChromeClient(Context context) {
        this.context = context;
    }

    @Override
    public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View choiceView = inflater.inflate(R.layout.alert_dialog, null);
        ImageView btn_close = (ImageView) choiceView.findViewById(R.id.btn_close_toast);
        TextView textView = (TextView) choiceView.findViewById(R.id.show_textview);
        textView.setText(message);
        final Dialog dialog = new Dialog(context, R.style.dialog_router);

        dialog.setContentView(choiceView);
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                result.confirm();
                dialog.dismiss();
            }
        });
        dialog.setCancelable(false);
        dialog.show();
        return true;
    }

    @Override
    public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        final AlertDialog myDialog = builder.create();
        myDialog.show();
        myDialog.getWindow().setContentView(R.layout.confirm_dialog);
        TextView dialog_title = (TextView) myDialog.getWindow().findViewById(R.id.dialog_title);
        TextView dialog_content = (TextView) myDialog.getWindow().findViewById(R.id.dialog_content);
        Button left_btn = (Button) myDialog.getWindow().findViewById(R.id.left_btn);
        Button right_btn = (Button) myDialog.getWindow().findViewById(R.id.right_btn);
        dialog_title.setText("确认");
        dialog_content.setText(message);
        left_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                result.confirm();
                myDialog.dismiss();
            }
        });
        right_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                result.cancel();
                myDialog.dismiss();
            }
        });
        return true;
    }
}
