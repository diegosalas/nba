package com.rsi.nba.plugins;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.DisplayCutout;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import com.rsi.nba.model.Data;
import com.rsi.nba.model.Extra;
import com.rsi.nba.util.ConnectionStateMonitor;
import com.rsi.nba.util.Errors;
import com.rsi.nba.util.ResultListener;

import java.io.File;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Objects;
import java.util.UUID;

import javax.crypto.Cipher;

import static android.content.Context.CLIPBOARD_SERVICE;
import static android.content.Context.DOWNLOAD_SERVICE;
import static java.nio.charset.StandardCharsets.UTF_8;



public class Utils {

    private static final String TAG = "nba-utils-functions";
    private static final String PRIVATE_KEY_ID = "nba-privateKey";
    private static final String PREF_UNIQUE_ID = "nba-idDispositivo";

    //Atributos
    private Activity mActivity;
    private Context mContext;
    public static Boolean mWriteStoragePermission;


    //Constructor
    public Utils(Activity mActivity,Context mContext) {
        this.mActivity = mActivity;
        this.mContext = mContext;
    }

    //Metodos
    private Extra setResultExtra(Integer code, Integer errorCode, String response){
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

    // Función que hace vibrar el dispositivo con un patrón dado
    public void startVibration (String patron, ResultListener<Extra> resultListener) {
        Vibrator mVibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        String[] arrayPatron = patron.split("\\s*,\\s*");
        long[] pattern = new long[arrayPatron.length];
        for (int i = 0; i < arrayPatron.length; i++) {
            pattern[i] = Long.parseLong(arrayPatron[i]);
        }

        try {
            Log.d(TAG, "El mVibrator es: " + mVibrator);
            if(mVibrator.hasVibrator()) {
                mVibrator.vibrate(pattern, -1);
                Log.d(TAG, "Ha vibrado bien");
                resultListener.finish(setResultExtra(1,null,null));
            } else {
                Log.d(TAG, "En el error de startVibration");
                resultListener.finish(setResultExtra(0,Errors.UTILS_NO_VIBRATION_HARDWARE,null));
            }

        } catch (Exception e) {
            Log.d(TAG, "En el catch de startVibration: " + e);
            resultListener.finish(setResultExtra(0,Errors.UTILS_VIBRATION_EXCEPTION,null));
        }
    }

    // Función para copiar un texto en el portapapeles
    public void copyClipboard(String text, ResultListener<Extra> resultListener) {
        try {
            ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(CLIPBOARD_SERVICE);
            ClipData data = ClipData.newPlainText("text", text);
            clipboard.setPrimaryClip(data);
            Log.d(TAG, "Ha copiado el texto");
            resultListener.finish(setResultExtra(1,null,null));
        } catch(Exception e) {
            Log.d(TAG, "En el catch de copyClipboard: " + e);
            resultListener.finish(setResultExtra(0,Errors.UTILS_COPYCLIPBOARD_EXCEPTION,null));
        }
    }

    // Función que devuelve el sistema operativo
    public void getOperativeSystem (ResultListener<Extra> resultListener) {
        try {
            Log.d(TAG, "El sistema es: Android");
            resultListener.finish(setResultExtra(1,null,"Android"));
        } catch (Exception e) {
            Log.d(TAG, "En el catch de getOperativeSystem: " + e);
            resultListener.finish(setResultExtra(0,Errors.UTILS_OPERATIVE_SYSTEM_EXCEPTION,null));
        }
    }

    // Función que genera un par de clave pública / clave privada
    public void generatePublicKey(ResultListener<Extra>resultListener) {
        try {
            // Inicializa el shared preferences
            SharedPreferences sharedPrefs = mContext.getSharedPreferences(PRIVATE_KEY_ID, Context.MODE_PRIVATE);

            // Genera el par de claves
            KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
            keyGenerator.initialize(1024);
            KeyPair keyPair = keyGenerator.genKeyPair();
            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();

            // Paso a string la clave publica
            String encodedPublic = Base64.encodeToString(publicKey.getEncoded(), Base64.DEFAULT);
            String finalPublicKey = encodedPublic.replaceAll("\n", "");
            // Paso a string la clave privada
            String encodedPrivate = Base64.encodeToString(privateKey.getEncoded(), Base64.DEFAULT);

            // Guardo la clave privada en el shared preferences
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString(PRIVATE_KEY_ID, encodedPrivate);
            editor.commit();
            // Devuelve la clave publica al evento
            Log.d(TAG, "generatePublicKey: el key es " + finalPublicKey);
            resultListener.finish(setResultExtra(1,null,finalPublicKey));
        } catch(Exception e) {
            Log.d(TAG, "En el catch de generatePublicKey: " + e);
            resultListener.finish(setResultExtra(0,Errors.UTILS_PAIR_GENERATOR_EXCEPTION,null));
        }
    }

    // Función que encripta un texto con la clave privada
    public void encryptToken(String text, ResultListener<Extra>resultListener) {
        try {
            // Recupero la clave privada guardada en el shared preferences
            SharedPreferences sharedPrefs = mContext.getSharedPreferences(PRIVATE_KEY_ID, Context.MODE_PRIVATE);
            String token = sharedPrefs.getString(PRIVATE_KEY_ID, "");

            if(token != null) {
                // Convierte el string en PrivateKey
                byte[] tokenData = Base64.decode(token, Base64.DEFAULT);
                X509EncodedKeySpec spec = new X509EncodedKeySpec(tokenData);
                KeyFactory kf = KeyFactory.getInstance("RSA");
                PrivateKey pk = kf.generatePrivate(spec);

                Cipher cipher = Cipher.getInstance("RSA");
                cipher.init(Cipher.ENCRYPT_MODE, pk);
                byte[] encryptedBytes = cipher.doFinal(text.getBytes());
                String encrypted = bytesToString(encryptedBytes);
                Log.d(TAG, "encryptToken: text encrypted: " + encrypted);
                resultListener.finish(setResultExtra(1,null,encrypted));
            } else {
                Log.d(TAG, "encryptToken: El Token es Null");
                resultListener.finish(setResultExtra(0,Errors.UTILS_CIPHER_NO_PRIVATEKEY,null));
            }
        } catch(Exception e) {
            Log.d(TAG, "En el catch de encryptToken: " + e);
            resultListener.finish(setResultExtra(0,Errors.UTILS_CIPHER_EXCEPTION,null));
        }
    }

    // Función que firma un texto con la clave privada
    public void signText (String text,ResultListener<Extra> resultListener) {
        try {
            // Recupero la clave privada guardada en el shared preferences
            SharedPreferences sharedPrefs = mContext.getSharedPreferences(PRIVATE_KEY_ID, Context.MODE_PRIVATE);
            String token = sharedPrefs.getString(PRIVATE_KEY_ID, "");
            String finalToken = token.replaceAll("\n", "");

            if(finalToken != null) {
                // Convierte el string en PrivateKey
                byte[] tokenData = Base64.decode(finalToken, Base64.DEFAULT);
                PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(tokenData);
                KeyFactory kf = KeyFactory.getInstance("RSA");
                PrivateKey pk = kf.generatePrivate(spec);


                Signature privateSignature = Signature.getInstance("SHA256withRSA");
                privateSignature.initSign(pk);
                privateSignature.update(text.getBytes(UTF_8));
                byte[] signature = privateSignature.sign();
                String stringSignature = Base64.encodeToString(signature, Base64.DEFAULT);
                String[] arraySplit = stringSignature.split("\n");
                String finalSignature = "";
                for(int i = 0; i < arraySplit.length; i++) {
                    finalSignature += arraySplit[i];
                }
                Log.d(TAG, "signText: el texto signed: " + finalSignature);
                resultListener.finish(setResultExtra(1,null,finalSignature));
            } else {
                Log.d(TAG, "signText: El Token es Null");
                resultListener.finish(setResultExtra(0,Errors.UTILS_SIGN_NO_PRIVATEKEY,null));
            }
        } catch(Exception e){
            Log.d(TAG, "En el catch de signText: " + e);
            resultListener.finish(setResultExtra(0,Errors.UTILS_SIGN_EXCEPTION,null));
        }
    }

    // Para obtener el id del dispositivo
    public void getUUID(ResultListener<Extra> resultListener) {
        try{
            SharedPreferences sharedPrefs = mContext.getSharedPreferences(PREF_UNIQUE_ID, Context.MODE_PRIVATE);
            String uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);

            if (uniqueID == null) {
                uniqueID = UUID.randomUUID().toString();
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(PREF_UNIQUE_ID, uniqueID);
                editor.commit();
            }
            Log.d(TAG, "getUUID: uniqueID es " + uniqueID);
            resultListener.finish(setResultExtra(1,null,uniqueID));
        } catch(Exception e) {
            Log.d(TAG, "En el catch de getUUID: " + e);
            resultListener.finish(setResultExtra(0,Errors.UTILS_UUID_EXCEPTION,null));
        }
    }


    // Llama a la clase asincrona "DownloadPdfAsync" para descargar el pdf
    public void downloadPdf(String pdfUrl, ResultListener<Extra> resultListener) {
        try {
            if (mWriteStoragePermission) {
                String[] arrayName = pdfUrl.split("/");
                String fileName = arrayName[arrayName.length - 1];
                File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + File.separator + "Download/" + fileName);

                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(pdfUrl))
                        .setTitle(fileName)                                                                     // Title of the Download Notification
                        .setDescription("Descargando...")                                                       // Description of the Download Notification
                        .setDestinationUri(Uri.fromFile(file))                                                  // Uri of the destination file
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED) // Visibility of the download Notification
                        .setAllowedOverMetered(true)                                                            // Set if download is allowed on Mobile network
                        .setAllowedOverRoaming(true);                                                           // Set if download is allowed on roaming network

                DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(DOWNLOAD_SERVICE);
                long downloadID = downloadManager.enqueue(request);// enqueue puts the download request in the queue.
                Log.d(TAG, "downloadPdf: " + downloadID);
                Toast.makeText(mContext, "Descarga finalizada", Toast.LENGTH_SHORT).show();
                resultListener.finish(setResultExtra(1,null,"Ok"));

            }else {
                Log.d(TAG, "downloadPdf: SIN PERMISOS");
                resultListener.finish(setResultExtra(0,Errors.UTILS_DOWNLOAD_PDF,null));
            }
        } catch (Exception e) {
            Log.d(TAG, "downloadPdf: ERROR en el Catch");
            resultListener.finish(setResultExtra(0,Errors.UTILS_DOWNLOAD_PDF,null));
        }
    }

    // Devuelve el modelo del dispositivo
    public void getDeviceModel(ResultListener<Extra> resultListener) {
        try {
            String manufacturer = Build.MANUFACTURER;
            String model = Build.MODEL;
            String deviceModel;
            if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
                deviceModel = parseStringModel(model);
            }
            else{
                deviceModel = parseStringModel(manufacturer) + " " + model;
            }
            Log.d(TAG, "El modelo es: " + deviceModel);
            resultListener.finish(setResultExtra(1,null,deviceModel));
        } catch (Exception e) {
            Log.d(TAG, "En el catch de getDeviceModel: " + e);
            resultListener.finish(setResultExtra(0,Errors.UTILS_DEVICE_MODEL_EXCEPTION,null));
        }
    }

    // Función que informa si el dispositivo tiene notch
    public void hasNotch(ResultListener<Extra> resultListener){
        try{
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

                DisplayCutout displayCutout = mActivity.getWindow().getDecorView().getRootWindowInsets().getDisplayCutout();
                if (displayCutout!= null){
                    Log.d(TAG, "hasNotch: true");
                    resultListener.finish(setResultExtra(1,null,"true"));
                }else {
                    Log.d(TAG, "hasNotch: false");
                    resultListener.finish(setResultExtra(1,null,"false"));
                }
            }
            else{
                Log.d(TAG, "hasNotch: false cause version");
                resultListener.finish(setResultExtra(1,null,"false"));
            }
        }catch (Exception e){
            Log.d(TAG, "hasNotch: en el Catch " + e);
            resultListener.finish(setResultExtra(0,Errors.UTILS_NOTCH_EXCEPTION,null));
        }
    }

    //Funcion para crear un listener que notifique cambios es el estado de la conexión.
    public void listenNetwork(ResultListener<Extra> resultListener){
        try{
            ConnectionStateMonitor mConnectionMonitor = new ConnectionStateMonitor(mContext);
            mConnectionMonitor.enable();

            switch (mConnectionMonitor.getNetworkType()) {

                case ConnectionStateMonitor.WIFI:
                    Log.d(TAG, "listenNetwork: Wifi");
                    resultListener.finish(setResultExtra(1,null,"Wifi"));
                    break;

                case ConnectionStateMonitor.CELLULAR:
                    Log.d(TAG, "listenNetwork: Celular");
                    resultListener.finish(setResultExtra(1,null,getNetworkClass(mContext)));
                    break;

                case ConnectionStateMonitor.ETHERNET:
                    Log.d(TAG, "listenNetwork: Ethernet");
                    resultListener.finish(setResultExtra(1,null,"Wifi"));
                    break;

                default:
                    Log.d(TAG, "listenNetwork: None");
                    resultListener.finish(setResultExtra(1,null,"None"));
            }

        }catch (Exception e){
            resultListener.finish(setResultExtra(0,Errors.UTILS_LISTEN_NETWORK_EXCEPTION,null));
        }
    }

    //Funcion para informar si el dispositivo es rooted
    public void isDeviceRooted(ResultListener<Extra> resultListener) {
        boolean check = checkBuildTags() == 1 || checkSuperUserApk() == 1 || checkFilePath() == 1;
        if (check){
            resultListener.finish(setResultExtra(1,null,"true"));
        }else if (checkBuildTags() == 0 || checkSuperUserApk() == 0 || checkFilePath() == 0){
            resultListener.finish(setResultExtra(1,null,"false"));
        }else {
            resultListener.finish(setResultExtra(1, Errors.UTILS_GENERIC_IS_ROOTED, null));
        }
    }

    //Evento para cerrar la aplicación.
    public void closeapp(){
        mActivity.finish();
        System.exit(0);
    }

    //Evento para cambiar el color del status bar
    public void changeStatusBarColor(String hexColor,ResultListener<Extra> resultListener){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = mActivity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor(hexColor));

            if (isColorDark(Color.parseColor(hexColor))){
                mActivity.getWindow().getDecorView().setSystemUiVisibility(0);
            }else {
                mActivity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }

            resultListener.finish(setResultExtra(1,null,"OK"));
        }else {
            resultListener.finish(setResultExtra(0,Errors.UTILS_GENERIC_ERROR,null));
        }
    }

    // Métodos Auxiliares --------------------------------

    //Función para distinguir si un color es oscuro o no:
    public boolean isColorDark(int color){
        double darkness = 1-(0.299*Color.red(color) + 0.587*Color.green(color) + 0.114*Color.blue(color))/255;
        if(darkness<0.5){
            return false; // It's a light color
        }else{
            return true; // It's a dark color
        }
    }

    // Función que parsea el string del modelo del dispositivo para que sea legible
    private String parseStringModel(String str){
        if (str == null || str.length() == 0) {
            return "";
        }
        char first = str.charAt(0);
        if (Character.isUpperCase(first)) {
            return str;
        } else {
            return Character.toUpperCase(first) + str.substring(1);
        }
    }

    // Función que convierte bytes a String
    private String bytesToString(byte[] b) {
        byte[] b2 = new byte[b.length + 1];
        b2[0] = 1;
        System.arraycopy(b, 0, b2, 1, b.length);
        return new BigInteger(b2).toString(36);
    }


    private String getNetworkClass(Context context) {
        TelephonyManager mTelephonyManager = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        int networkType = Objects.requireNonNull(mTelephonyManager).getNetworkType();
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN: {
                Log.d(TAG, "getNetworkClass: 2G");
                return "2G";
            }
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP: {
                Log.d(TAG, "getNetworkClass: 3G");
                return "3G";
            }
            case TelephonyManager.NETWORK_TYPE_LTE: {
                Log.d(TAG, "getNetworkClass: 4G");
                return "4G";
            }
            default:
                return "Unavailable";
        }
    }

    private Integer checkBuildTags() {
        Log.d(TAG,"Entra en el checkBuildTags");
        try {
            String buildTags = android.os.Build.TAGS;
            return buildTags != null && buildTags.contains("test-keys") ? 1 : 0;
        } catch(Exception e) {
            return Errors.UTILS_GENERIC_IS_ROOTED;
        }
    }

    private Integer checkSuperUserApk() {
        Log.d(TAG,"Entra en el checkSuperUserApk");
        try {
            return new File("/system/app/Superuser.apk").exists() ? 1 : 0;
        } catch(Exception e) {
            return Errors.UTILS_GENERIC_IS_ROOTED;
        }
    }

    private Integer checkFilePath() {
        Log.d(TAG,"Entra en el checkFilePath");
        try {
            String[] paths = { "/sbin/su", "/system/bin/su", "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
                    "/system/bin/failsafe/su", "/data/local/su" };
            for (String path : paths) {
                if (new File(path).exists()) return 1;
            }
            return 0;
        } catch (Exception e) {
            return Errors.UTILS_GENERIC_IS_ROOTED;
        }
    }


}
