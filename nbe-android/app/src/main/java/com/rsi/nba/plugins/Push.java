package com.rsi.nba.plugins;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.rsi.nba.controllers.WebViewController;
import com.rsi.nba.model.Data;
import com.rsi.nba.model.Event;
import com.rsi.nba.model.Extra;
import com.rsi.nba.util.Errors;
import com.rsi.nba.util.ResultListener;

import org.json.JSONObject;

public class Push {

    private static final String TAG = "nba-Push";

    private static final String PUSH_FLAG_KEY = "push-flag-key";
    private static final String PUSH_FIREBASE_ID = "push-firebase-id";

    public static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";

    //Atributos
    private static Context mContext;

    private static String PREFERENCE_FILE_KEY;
    private static final int MODE = Context.MODE_PRIVATE;
    private final SharedPreferences sharedPreferences;
    private boolean enableNotifications = false;


    //Constructor
    public Push(Context mContext) {
        this.mContext = mContext;

        sharedPreferences = mContext.getSharedPreferences(PREFERENCE_FILE_KEY, MODE);
    }

    //Métodos
    private static Extra setResultExtra(Integer code, Integer errorCode, String response){
        Extra extra = new Extra();
        if (code == 0){
            extra.setCodigoRetorno(code);
            extra.setData(new Errors(mContext).getDataError(errorCode));
            return extra;
        }else if (response != null){
            Data data = new Data();
            data.setResponse(response);
            extra.setCodigoRetorno(code);
            extra.setData(data);
            return extra;
        }else {
            extra.setCodigoRetorno(code);
            return extra;
        }
    }

    //Evento que sirve para obtener el flag de notificaciones.
    public void getPushFlag(ResultListener<Extra> resultListener) {
        try {
            String response = sharedPreferences.getString(PUSH_FLAG_KEY, "false");
            resultListener.finish(setResultExtra(1,null,response));
        } catch(Exception e) {
            resultListener.finish(setResultExtra(0,Errors.PUSH_GET_FLAG_ERROR,null));
        }
    }

    //Evento para actualizar el flag de notificaciones.
    public void updatePushFlag(String text,ResultListener<Extra> resultListener) {
        try {
            writeToSharedPreferences(text);
            resultListener.finish(setResultExtra(1,null,null));
        } catch(Exception e) {
            resultListener.finish(setResultExtra(0,Errors.PUSH_UPDATE_FLAG_ERROR,null));
        }
    }

    //Evento que sirve para modificar el flag de notificaciones a false.
    public void removePushFlag(ResultListener<Extra>resultListener){
        try{
            writeToSharedPreferences("false");
            resultListener.finish(setResultExtra(1,null,null));
        }catch (Exception e){
            resultListener.finish(setResultExtra(0,Errors.PUSH_UPDATE_FLAG_ERROR,null));
        }
    }

    //Evento que sirve para obtener el id del dispositivo para las notificaciones push.
    public void getIdPush(ResultListener<Extra>resultListener){
        try {
            String token = sharedPreferences.getString(PUSH_FIREBASE_ID, "");

            resultListener.finish(setResultExtra(1,null,token));

        } catch(Exception e) {
            resultListener.finish(setResultExtra(0,Errors.PUSH_GET_TOKEN_ID_ERROR,null));
        }

    }

    //Evento que sirve para generar el id del dispositivo para las notificaciones push y lo guarda para futuras ocasiones.
    public void generateIdPush(final ResultListener<Extra> resultListener) {
        Log.d(TAG, "En el generateIdPush:");

        String token = sharedPreferences.getString(PUSH_FIREBASE_ID, "");
        try{
            if (token.equals("")) {

                FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }

                        String token = task.getResult().getToken();
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(PUSH_FIREBASE_ID, token);
                        editor.apply();
                        resultListener.finish(setResultExtra(1, null, token));
                    }
                });


            } else {
                resultListener.finish(setResultExtra(0, Errors.PUSH_GENERATE_INSTANCE_ID_ERROR, null));
            }
        }catch (Exception e){
            Log.d(TAG, "generateIdPush: en el Catch --> " + e);
            resultListener.finish(setResultExtra(0,Errors.PUSH_GENERIC_ERROR,null));
        }
    }

    //Evento que sirve para inicializar el listener para que lleguen las notificaciones push.
    public void initPushListener(ResultListener<Extra> resultListener){

        SharedPreferences sharedPrefs = mContext.getSharedPreferences(ENABLED_NOTIFICATION_LISTENERS, Context.MODE_PRIVATE);
        enableNotifications = sharedPrefs.getBoolean(ENABLED_NOTIFICATION_LISTENERS,false);

        if (!enableNotifications){
            enableNotifications = true;
            FirebaseMessaging.getInstance().setAutoInitEnabled(true);
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putBoolean(ENABLED_NOTIFICATION_LISTENERS, enableNotifications);
            editor.commit();

            resultListener.finish(setResultExtra(1,null,null));
        }else {
            enableNotifications = false;
            FirebaseMessaging.getInstance().setAutoInitEnabled(false);
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putBoolean(ENABLED_NOTIFICATION_LISTENERS, enableNotifications);
            editor.commit();
        }

    }

    //Métodos auxiliares -------------------------
    private void writeToSharedPreferences(String flag) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PUSH_FLAG_KEY, flag);
        editor.apply();
    }

    public static void sendMessage(JSONObject jsonObject) {
        Log.d(TAG, "sendMessage: -- " + jsonObject);
        Event eventMsg = new Event();
        eventMsg.setEvent("native-onPushReceived");
        eventMsg.setExtra(setResultExtra(1,null, jsonObject.toString()));

        String json = new Gson().toJson(eventMsg);

        Log.d(TAG, "sendMessage: json event: " + json);
        WebViewController.dataToSend(json);

    }

}
