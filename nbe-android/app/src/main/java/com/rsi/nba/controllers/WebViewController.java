package com.rsi.nba.controllers;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import com.google.gson.Gson;
import com.rsi.nba.MainActivity;
import com.rsi.nba.model.Data;
import com.rsi.nba.model.Event;
import com.rsi.nba.model.Extra;
import com.rsi.nba.plugins.AppOpener;
import com.rsi.nba.plugins.Biometrics;
import com.rsi.nba.plugins.Push;
import com.rsi.nba.util.Errors;
import com.rsi.nba.util.ResultListener;
import com.rsi.nba.plugins.Utils;


public class WebViewController implements ResultListener<Extra> {

    private static final String TAG = "nba-WebViewController";

    //Atributos
    private Activity mActivity;
    private Context mContext;
    private static WebView mWebView;

    private static Utils utils;
    private static Event mEvent;

    private AppOpener appOpener;

    private Biometrics biometrics;

    private Push push;


    //Constructor
    public WebViewController(Activity mActivity,Context mContext, WebView mWebView) {
        this.mActivity = mActivity;
        this.mContext = mContext;
        this.mWebView = mWebView;

        utils = new Utils(mActivity,mContext);
        appOpener = new AppOpener(mContext);
        biometrics = new Biometrics(mActivity,mContext);
        push = new Push(mContext);
    }

    //Métodos
    public void createEvent(String data){
        try {
            Event event = new Gson().fromJson(data,Event.class);
            String eventName = event.getEvent().split("-")[1];
            event.setEvent(eventName);
            eventManager(event);
        }catch (Exception e){
            Log.d(TAG, "createEvent: fail -- " + e.getMessage());
        }
    }


    // Función que manega las llamadas que vienen del WebView y ejecuta sus funciones correspondientes
    public void eventManager(final Event event) {
        Log.d(TAG, "Entra en el eventManager: "
        + "El evento es: " + event.getEvent()
        + " - El extra es: " + event.getExtra());

        mEvent = event;

        try {
            // Switch que filtra los eventos y enlaza cada uno con su funcionalidad
            switch(event.getEvent()) {

                case "startVibration":
                    String pattern = event.getExtra().getPattern().isEmpty() ? "" : event.getExtra().getPattern();
                    utils.startVibration(pattern, this);
                    break;

                case "copyClipboard":
                    String textToCopy = event.getExtra().getTextToCopy().isEmpty() ? "" : event.getExtra().getTextToCopy();
                    utils.copyClipboard(textToCopy, this);
                    break;

                case "operativeSystem":
                    utils.getOperativeSystem(this);
                    break;

                case "generatePublicKey":
                        utils.generatePublicKey(this);
                    break;

                case "encryptToken":
                    String textToTokenize = event.getExtra().getToken().isEmpty() ? "" : event.getExtra().getToken();
                    utils.encryptToken(textToTokenize, this);
                    break;

                case "signToken":
                    String textToSign = event.getExtra().getToken().isEmpty() ? "" : event.getExtra().getToken();
                    utils.signText(textToSign, this);
                    break;

                case "getDeviceUuid":
                    utils.getUUID(this);
                    break;

                case "downloadPdf":
                    MainActivity.checkPermission();
                    break;

                case "getDeviceModel":
                    utils.getDeviceModel(this);
                    break;

                case "hasNotch":
                    utils.hasNotch(this);
                    break;

                case "networkStateListener":
                    utils.listenNetwork(this);
                    break;

                case "isDeviceRooted":
                    utils.isDeviceRooted(this);
                    break;

                case "closeapp":
                    utils.closeapp();
                    break;

                case "statusBarColor":
                    String color = event.getExtra().getColor().isEmpty() ? "" : event.getExtra().getColor();
                    utils.changeStatusBarColor(color,this);
                    break;

                case "openApp":
                    String nombreAndroid = event.getExtra().getNombreAndroid().isEmpty() ? "" : event.getExtra().getNombreAndroid();
                    appOpener.openApp(nombreAndroid, this);
                    break;

                case "openProfileApp":
                    String nombreAndroidProfile = event.getExtra().getNombreAndroid().isEmpty() ? "" : event.getExtra().getNombreAndroid();
                    String userProfile = event.getExtra().getUserProfile().isEmpty() ? "" : event.getExtra().getUserProfile();
                    appOpener.openProfileApp(nombreAndroidProfile, userProfile, this);
                    break;

                case "checkBiometricAvailability":
                    biometrics.checkBiometricAvailability(this);
                    break;

                case "initBiometricIdentification":
                    String title = event.getExtra().getTitle().isEmpty() ? "" : event.getExtra().getTitle();
                    String desc = event.getExtra().getDesc().isEmpty() ? "" : event.getExtra().getDesc();
                    biometrics.initBiometricIdentification(title,desc,this);
                    break;

                case "stopBiometricIdentification":
                    biometrics.stopBiometricIdentification(this);
                    break;

                case "updateBiometricFlag":
                    String biometricValue = event.getExtra().getBiometricValue().isEmpty() ? "" : event.getExtra().getBiometricValue();
                    biometrics.updateBiometricFlag(biometricValue,this);
                    break;

                case "getBiometricFlag":
                    biometrics.getBiometricFlag(this);
                    break;

                case "getPushFlag":
                    push.getPushFlag(this);
                    break;

                case "updatePushFlag":
                    String flag = event.getExtra().getFlag().isEmpty() ? "" : event.getExtra().getFlag();
                    push.updatePushFlag(flag,this);
                    break;

                case "removePushFlag":
                    push.removePushFlag(this);
                    break;

                case "getIdPush":
                    push.getIdPush(this);
                    break;

                case "generateIdPush":
                    push.generateIdPush(this);
                    break;

                case "initPushListener":
                    push.initPushListener(this);
                    break;

                case "closeApp":
                    android.os.Process.killProcess(android.os.Process.myPid());
                    break;

                default:
                    Extra extraDef = new Extra();
                    extraDef.setCodigoRetorno(0);
                    extraDef.setData(new Errors(mContext).getDataError(Errors.DEFAULT_EVENT_MANAGER));
                    sendResult(event,extraDef);
                    break;
            }

        } catch(Exception e) {
            Log.d(TAG, "En el catch del eventManager: " + e);
            Extra extraCatch = new Extra();
            extraCatch.setCodigoRetorno(0);
            extraCatch.setData(new Errors(mContext).getDataError(Errors.GENERIC_EVENT_MANAGER));
            sendResult(event,extraCatch);
        }
    }

    //Función para devolver el resultado de los switch
    private static void sendResult(Event event, Extra extra){
        String eventName = "nbe-" + event.getEvent() + "Return";

        event.setEvent(eventName);
        event.setExtra(extra);
        String json = new Gson().toJson(event);
        sendCallback(json);
    }

    // Función que ejecuta una función en el WebView
    private static void sendCallback(final String data) {
        try {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    String jsToCode = "webviewListener(" + data + ")";
                    Log.d(TAG, "Return: " + data);
                    mWebView.evaluateJavascript(jsToCode, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {
                            Log.d(TAG, "receiveValue: " + value);
                        }
                    });
                }
            });
        } catch(Exception e) {
            Log.d(TAG, "En el catch del mandar evento: " + e);
        }
    }

    //Función para enviar respuesta de mensaje
    public static void dataToSend(String data){
        sendCallback(data);
    }

    //Función para enviar respuesta del catch Biometric
    public static void sendCatchError(Data data){
        Extra extra = new Extra();
        extra.setCodigoRetorno(0);
        extra.setData(data);
        sendResult(mEvent,extra);
    }

    public static void download(){
        String urlPDF = mEvent.getExtra().getUrl().isEmpty() ? "" : mEvent.getExtra().getUrl();
        utils.downloadPdf(urlPDF, new ResultListener<Extra>() {
            @Override
            public void finish(Extra result) {
                sendResult(mEvent,result);
            }
        });
    }

    @Override
    public void finish(Extra result) {
        sendResult(mEvent,result);
    }
}
