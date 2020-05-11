package com.rsi.nba;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import com.google.gson.Gson;
import com.rsi.nba.controllers.WebViewController;
import com.rsi.nba.model.Event;
import com.rsi.nba.model.Extra;
import com.rsi.nba.plugins.Utils;
import com.rsi.nba.util.KeyboardUtils;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "nba-mainActivity";

    //KEYS
    public static final String KEY_ADAPTER_NAME = "nbAdapter";
    private static final String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final int WRITE_EXTERNAL_STORAGE_CODE = 1234;

    public static boolean appForeground = false;

    //Atributos
    private static WebView mWebView;
    private static Context mContext;
    private static Activity mActivity;
    private int origSize = 0;


    //        DEVELOP
    private String url = "https://cdntest.ruralvia.com/CA-FRONT/NBE/app/";
    //        TEST
    //private String url = "https://cdntest.ruralvia.com/CA-FRONT/NBE/app/develop/";
    //        PROD
    //private String url = "https://cdn.rm-static.com/CA-FRONT/NBE/app/";

    @Override
    protected void onResume() {
        super.onResume();
        appForeground = true;
        Log.d(TAG, "onResume: appForeground: " + appForeground);
    }

    @Override
    protected void onPause() {
        super.onPause();
        appForeground = false;
        Log.d(TAG, "onPause: appForeground: " + appForeground);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = getApplicationContext();
        mActivity = this;

        appForeground = true;

        // Para poder hacer abrir con inspect: Eliminar para producción
        WebView.setWebContentsDebuggingEnabled(true);

        //Evitar que el webview se ajuste al abrir y cerrar el teclado
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        mWebView = findViewById(R.id.webViewNbe);
        final RelativeLayout relativeLayout = findViewById(R.id.frame);

        mWebView.setWebViewClient(new WebViewClient());

        WebSettings ajustesWebView = mWebView.getSettings();
        ajustesWebView.setJavaScriptEnabled(true);
        ajustesWebView.setDomStorageEnabled(true);

        mWebView.loadUrl(url);

        //Creamos un controlador para manejar las llamadas al webview
        WebViewController webViewController = new WebViewController(this,getApplicationContext(),mWebView);

        // Publica un objeto "nbAdapter" para poder ser invocado desde la web
        mWebView.addJavascriptInterface(new WebAppInterface(getApplicationContext(), webViewController), KEY_ADAPTER_NAME);


        KeyboardUtils.addKeyboardToggleListener(this, new KeyboardUtils.SoftKeyboardToggleListener() {
            @Override
            public void onToggleSoftKeyboard(boolean isVisible, final int diff) {
                Handler handler = new Handler();
                Log.d(TAG, "onToggleSoftKeyboard: " + isVisible + " " + diff);
                if (!isVisible){
                    origSize = diff;
                    mWebView.setTranslationY(diff-origSize);
                    relativeLayout.setPadding(0,0,0,0);
                }else {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            relativeLayout.setPadding(0,0,0,diff-origSize-20);
                        }
                    },150);

                }
            }
        });
    }


    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        try{
            Log.d(TAG, "onBackPressed");
            Event eventMsg = new Event();
            eventMsg.setEvent("native-onBackButtonPressed");
            Extra extra = new Extra();
            extra.setCodigoRetorno(1);
            eventMsg.setExtra(extra);

            String json = new Gson().toJson(eventMsg);
            WebViewController.dataToSend(json);

        }catch (Exception e){
            Log.d(TAG, "onBackPressed: en el catch " + e.getMessage());
        }

    }

    // Comprueba si el usuario puede descargar un archivo
    public static void checkPermission() {
        Log.d(TAG, "checkPermission");

        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (ContextCompat.checkSelfPermission(mContext, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "checkPermission: ask for permission");
            ActivityCompat.requestPermissions(mActivity,permissions,WRITE_EXTERNAL_STORAGE_CODE);
        }else {
            Log.d(TAG, "checkPermission: permission already granted");
            Utils.mWriteStoragePermission = true;
            WebViewController.download();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case WRITE_EXTERNAL_STORAGE_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.d(TAG, "onRequestPermissionsResult: OK");
                    Utils.mWriteStoragePermission = true;
                    WebViewController.download();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.d(TAG, "onRequestPermissionsResult: Fail");
                    Utils.mWriteStoragePermission = false;
                    WebViewController.download();
                }
                return;
            }
        }
    }


}
