package com.rsi.nba.plugins;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.rsi.nba.model.Extra;
import com.rsi.nba.util.Errors;
import com.rsi.nba.util.ResultListener;

public class AppOpener {

    private static final String TAG = "nba-AppOpener";

    //Atributos
    private Context mContext;

    //Constructor
    public AppOpener(Context mContext) {
        this.mContext = mContext;
    }

    //Métodos
    private Extra setResultExtra(Integer code, Integer errorCode){
        Extra extra = new Extra();
        if (code == 0){
            extra.setCodigoRetorno(code);
            extra.setData(new Errors(mContext).getDataError(errorCode));
            return extra;
        }else {
            extra.setCodigoRetorno(code);
            return extra;
        }
    }

    public void openApp (String appName, ResultListener<Extra> resultListener) {
        Log.d(TAG, "Entra en openApp");
        try {
            Intent launchIntent = mContext.getPackageManager().getLaunchIntentForPackage(appName);
            if (launchIntent != null) {
                mContext.startActivity(launchIntent);
                Log.d(TAG, "openApp: True");
                resultListener.finish(setResultExtra(1,null));
            } else {
                Log.d(TAG, "openApp: False launchIntent is null search in google play");
                Intent playStore = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appName));
                playStore.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                playStore.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                playStore.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mContext.startActivity(playStore);
                resultListener.finish(setResultExtra(0,Errors.APPOPENER_CHECK_APP_NOT_INSTALLED));
            }
        } catch (Exception e) {
            Log.d(TAG, "En el catch: " + e);
            resultListener.finish(setResultExtra(0,Errors.APPOPENER_OPEN_EXCEPTION));
        }
    }

    public void openProfileApp(String appName,String userProfile,ResultListener<Extra> resultListener){
        Log.d(TAG, "Entra en openProfileApp");
        try{
            Uri uri;

            switch(appName){
                case "com.facebook.katana" :
                    uri = Uri.parse("fb://profile/" + userProfile);
                    break;
                case "com.twitter.android" :
                    uri = Uri.parse("twitter://user?screen_name=" + userProfile);
                    break;
                default:
                    resultListener.finish(setResultExtra(0,Errors.APPOPENER_OPEN_LAUNCHINTENT_NULL));
                    return;
            }

            Intent launchIntent = new Intent(Intent.ACTION_VIEW, uri);
            mContext.startActivity(launchIntent);

            resultListener.finish(setResultExtra(1,null));
        }catch (Exception e){
            Log.d(TAG, "En el catch: " + e);
            resultListener.finish(setResultExtra(0,Errors.APPOPENER_OPEN_EXCEPTION));
        }
    }



}
