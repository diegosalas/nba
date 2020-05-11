package com.rsi.nba;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.rsi.nba.controllers.WebViewController;

import org.json.JSONObject;

public class WebAppInterface {

    private static final String TAG = "nba-webAppInterface";

    //Atributos
    private Context mContext;
    private WebViewController webViewController;

    // Constructor de la clase, se le pasa el context
    WebAppInterface(Context c,WebViewController webViewController) {
        this.mContext = c;
        this.webViewController = webViewController;
    }

    // Maneja las llamadas que vienen del WebView
    @JavascriptInterface
    public void webReceiver(String data) {
        Log.d(TAG, "Entra en el webReceiver: " + data);
        webViewController.createEvent(data);
    }

    // Maneja las llamadas que vienen del WebView
    @JavascriptInterface
    public void webReceiverJson(JSONObject data) {
        Log.d(TAG, "Entra en el webReceiverJson: " + data);
        //new MainActivity().eventManager(data);
    }
}
