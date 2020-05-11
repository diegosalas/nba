package com.rsi.nba.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.rsi.nba.R;

public class FingerprintDialog extends DialogFragment {
    private static final String TAG = "nba-FingerprintDialog";

    //Atributos
    private Context mContext;
    private Activity mActivity;
    private static AlertDialog mDialog;

    //Constructor

    public FingerprintDialog(Context context, Activity activity) {
        Log.d(TAG, "En el initialize del dialog");
        mContext = context;
        mActivity = activity;
    }

    public void createDialog(String title, String desc, final ResultListener<Integer> resultCreateDialog) {
        Log.d(TAG, "Entra en createDialog del dialog");
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        LayoutInflater inflater = mActivity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.fingerprint_dialog, null));
        mDialog = builder.create();
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.show();

        TextView titleTxt = mDialog.findViewById(R.id.txtFingerprintTitle);
        TextView descriptionTxt =  mDialog.findViewById(R.id.txtFingerprintDescription);
        titleTxt.setText(title);
        descriptionTxt.setText(desc);

        Button cancelBtn = mDialog.findViewById(R.id.buttonCancel);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeDialog();
                resultCreateDialog.finish(0);
            }
        });
    }

    public void closeDialog() {
        mDialog.dismiss();
        mDialog = null;
    }

    public void updateStatus(int status) {
        Log.d(TAG, "Entra en el updateStatus");
        final TextView txtStatus = mDialog.findViewById(R.id.txtFingerprintDescription);
        final ImageView imageStatus = mDialog.findViewById(R.id.imageFingerprint);

        switch(status) {
            case 1:
            case 4:
                txtStatus.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        txtStatus.setText(R.string.fingerprint_status_error);
                        txtStatus.setTextColor(ContextCompat.getColor(mContext, R.color.red));
                        imageStatus.setImageResource(R.drawable.fingerprint_ko);
                    }
                }, 1600);
                break;
            case 2:
                txtStatus.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        txtStatus.setTextColor(ContextCompat.getColor(mContext, R.color.black));
                        txtStatus.setText(R.string.fingerprint_status_success);
                        imageStatus.setImageResource(R.drawable.fingerprint_ok);
                    }
                }, 1600);
                break;
            case 3:
                txtStatus.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        txtStatus.setTextColor(ContextCompat.getColor(mContext, R.color.red));
                        txtStatus.setText(R.string.fingerprint_status_failed);
                        imageStatus.setImageResource(R.drawable.fingerprint_ko);
                    }
                }, 1600);
                break;
        }
    }
}