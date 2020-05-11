package com.rsi.nba.plugins;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.hardware.biometrics.BiometricPrompt;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.util.Log;

import com.rsi.nba.controllers.WebViewController;
import com.rsi.nba.model.Data;
import com.rsi.nba.model.Extra;
import com.rsi.nba.util.Errors;
import com.rsi.nba.util.FingerprintDialog;
import com.rsi.nba.util.ResultListener;

import android.security.keystore.KeyProperties;

import androidx.core.hardware.fingerprint.FingerprintManagerCompat;
import androidx.core.os.CancellationSignal;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.util.concurrent.Executor;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class Biometrics {

    private static final String TAG = "nba-Biometrics";
    private static final String BIOMETRICS_FLAG_KEY = "biometrics-flag-key";

    final private String ANDROID_KEY_STORE = "AndroidKeyStore";
    final private String KEY_STORE_ALIAS = "nba-keystore";

    private static String PREFERENCE_FILE_KEY;
    private static final int MODE = Context.MODE_PRIVATE;

    //Atributos
    private Context mContext;
    private Activity mActivity;

    private KeyGenerator mKeyGenerator;
    private KeyStore mKeyStore;
    private Cipher mCipher;
    private android.os.CancellationSignal cancellationSignal = new android.os.CancellationSignal();
    private BiometricPrompt mBiometricPrompt;
    private SecretKey key;
    private FingerprintDialog dialog;
    private SharedPreferences sharedPreferences;


    //Constructor
    public Biometrics(Activity mActivity,Context mContext) {
        this.mActivity = mActivity;
        this.mContext = mContext;

        PREFERENCE_FILE_KEY = mContext.getApplicationContext().getPackageName();
        sharedPreferences = mContext.getSharedPreferences(PREFERENCE_FILE_KEY, MODE);

        try {
            mKeyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,ANDROID_KEY_STORE);
            mKeyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | KeyStoreException e) {
            WebViewController.sendCatchError(new Errors(mContext).getDataError(Errors.BIOMETRICS_GENERIC_ERROR));
        }

    }

    //Métodos
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

    // Comprueba si el dispositivo tiene las características para permitir acceso por reconocimiento biométrico
    public void checkBiometricAvailability(ResultListener<Extra> resultListener){
        Log.d(TAG, "Entra en checkBiometricAvailability");

        String resultAvailability = checkAvailability();

        if (resultAvailability.equals("true")){
            resultListener.finish(setResultExtra(1,null,"true"));
        }else {
            resultListener.finish(setResultExtra(0,Integer.valueOf(resultAvailability),null));
        }

    }

    // Abre las ventanas modales segun sea por fingerprint o por biometrics
    public void initBiometricIdentification(String title, String desc, final ResultListener<Extra> resultListener) {
        Log.d(TAG, "Entra en initBiometricIdentification");
        String resultAvailability = checkAvailability();

        //Comprobar que al iniciar esta disponible el acceso biometrico
        if (resultAvailability.equals("true")){
            try {
                initCipher(new ResultListener<Integer>() {
                    @Override
                    public void finish(Integer result) {
                        if (result != 1){
                            resultListener.finish(setResultExtra(0,result,null));
                        }
                    }
                });

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < 28) {
                    Log.d(TAG, "Es fingerprintManager");
                    try{
                        dialog = new FingerprintDialog(mContext, mActivity);
                        dialog.createDialog(title, desc, new ResultListener<Integer>() {
                            @Override
                            public void finish(Integer result) {
                                if (result == 0){
                                    resultListener.finish(setResultExtra(1,null,"cancel"));
                                }
                            }
                        });
                        fingerprintMode(new ResultListener<Integer>() {
                            @Override
                            public void finish(Integer result) {
                                switch (result){
                                    case 1:
                                        resultListener.finish(setResultExtra(1,null,"error"));
                                        break;
                                    case 2:
                                        resultListener.finish(setResultExtra(1,null,"success"));
                                        break;
                                    case 3:
                                        resultListener.finish(setResultExtra(1,null,"failed"));
                                        break;
                                    case 4:
                                        resultListener.finish(setResultExtra(1,null,"help"));
                                        break;
                                    default:
                                        resultListener.finish(setResultExtra(0,result,null));
                                        break;
                                }
                            }
                        });
                    }catch (Exception e){
                        resultListener.finish(setResultExtra(0,Errors.BIOMETRICS_GENERIC_ERROR,null));
                    }
                } else if (Build.VERSION.SDK_INT >= 28) {
                    Log.d(TAG, "Es biometricPrompt");
                    try {
                        cancellationSignal = new android.os.CancellationSignal();
                        biometricMode(title, desc, new ResultListener<String>() {
                            @Override
                            public void finish(String result) {
                                resultListener.finish(setResultExtra(1,null,result));
                            }
                        });
                    }catch (Exception e){
                        resultListener.finish(setResultExtra(0,Errors.BIOMETRICS_GENERIC_ERROR,null));
                    }
                } else {
                    Log.d(TAG, "Es menor a android marshmellow");
                    resultListener.finish(setResultExtra(0,Errors.BIOMETRICS_OPENMODAL_CANT_OPEN,null));
                }

            } catch (Exception e) {
                resultListener.finish(setResultExtra(0,Errors.BIOMETRICS_GENERIC_ERROR,null));
            }

        }else {
            resultListener.finish(setResultExtra(0,Integer.valueOf(resultAvailability),null));
        }

    }

    public void stopBiometricIdentification(ResultListener<Extra> resultListener) {
        Log.d(TAG, "Entra en closeModal");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < 28) {
            Log.d(TAG, "Es fingerprintManager");
            try{
                resultListener.finish(setResultExtra(1,null,null));
                dialog.closeDialog();
            } catch (Exception e) {
                resultListener.finish(setResultExtra(0,Errors.BIOMETRICS_CLOSEMODAL_CANT_CLOSE,null));
            }
        } else if (Build.VERSION.SDK_INT >= 28) {
            Log.d(TAG, "Es biometricPrompt");
            try{
                resultListener.finish(setResultExtra(1,null,null));
                cancellationSignal.cancel();
            } catch (Exception e) {
                resultListener.finish(setResultExtra(0,Errors.BIOMETRICS_CLOSEMODAL_CANT_CLOSE,null));
            }
        } else {
            Log.d(TAG, "Es menor a android marshmellow");
            resultListener.finish(setResultExtra(0,Errors.BIOMETRICS_CLOSEMODAL_CANT_CLOSE,null));
        }
    }

    // LLama a sharePreferences para actualizar la variable de fingerprint
    public void updateBiometricFlag(String text, ResultListener<Extra> resultListener) {
        try {
            writeToSharedPreferences(text);
            resultListener.finish(setResultExtra(1,null,null));
        } catch(Exception e) {
            resultListener.finish(setResultExtra(0,Errors.BIOMETRICS_UPDATE_FLAG,null));
        }
    }

    // LLama a sharePreferences para obtener el dato de fingerprint
    public void getBiometricFlag(ResultListener<Extra> resultListener) {
        try {
            String response = sharedPreferences.getString(BIOMETRICS_FLAG_KEY, "false");
            resultListener.finish(setResultExtra(1,null,response));
        } catch(Exception e) {
            resultListener.finish(setResultExtra(0,Errors.BIOMETRICS_GET_FLAG,null));
        }
    }



    //Métodos auxiliares -------------------
    public String checkAvailability () {
        Log.d(TAG, "Entra en checkAvailability");
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                FingerprintManager finger = (FingerprintManager) mContext.getSystemService(Context.FINGERPRINT_SERVICE);
                Log.d(TAG, "El fingerprintManager es: " + finger);
                if (!finger.isHardwareDetected()) {
                    // Device doesn't support fingerprint authentication
                    Log.d(TAG, "No tiene hardware detectado");
                    return String.valueOf(Errors.BIOMETRICS_NO_HARDWARE_DETECTED);
                } else if (!finger.hasEnrolledFingerprints()) {
                    // User hasn't enrolled any fingerprints to authenticate with
                    Log.d(TAG, "User hasn't enrolled any fingerprints to authenticate with");
                    return String.valueOf(Errors.BIOMETRICS_NO_FINGERPRINT);
                } else {
                    Log.d(TAG, "Everything is ready for fingerprint authentication");
                    // Everything is ready for fingerprint authentication
                    return "true";
                }
            }else{
                //Check if the android version in device is greater than Marshmallow, since fingerprint authentication is only supported from Android 6.0.
                Log.d(TAG, "La versión del dispositivo es inferior a la versión que permite el reconocimiento biométrico ");
                return String.valueOf(Errors.BIOMETRICS_LOWER_VERSION);
            }
        } catch (Exception e) {
            Log.d(TAG, "En el catch: " + e);
            return String.valueOf(Errors.BIOMETRICS_AVAILABILITY_EXCEPTION);
        }
    }

    private void initCipher(final ResultListener<Integer> resultCipher) {
        try {
            Log.d(TAG,"Entra en initCipher");
            final Cipher cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);

            getSecretKey(new ResultListener<Integer>() {
                @Override
                public void finish(Integer result) {
                    if (result != 1){
                        resultCipher.finish(result);
                    }
                }
            });

            KeyStore.Entry entry = mKeyStore.getEntry(KEY_STORE_ALIAS,null);

            if (key == null || !(entry instanceof KeyStore.PrivateKeyEntry)) {
                createKey(new ResultListener<Integer>() {
                    @Override
                    public void finish(Integer result) {
                        if (result != 1){
                            resultCipher.finish(result);
                        }else {
                            Log.d(TAG, "Entra en el crear");
                            getSecretKey(new ResultListener<Integer>() {
                                @Override
                                public void finish(Integer result) {
                                    if (result != 1){
                                        resultCipher.finish(result);
                                    }
                                }
                            });
                        }
                    }
                });
            }

            Log.d(TAG, "El key es:" + key);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            mCipher = cipher;
            resultCipher.finish(1);
        } catch (Exception e) {
            resultCipher.finish(Errors.BIOMETRICS_GENERIC_CIPHER);
        }
    }

    private void getSecretKey(ResultListener<Integer> resultSecretKey) {
        Log.d(TAG, "Entra en getSecretKey");
        key = null;
        try {
            mKeyStore.load(null);
            key = (SecretKey) mKeyStore.getKey(KEY_STORE_ALIAS, null);
            resultSecretKey.finish(1);
        } catch (KeyStoreException e) {
            resultSecretKey.finish(Errors.BIOMETRICS_GENERIC_GET_KEY);
        } catch (Exception e) {
            resultSecretKey.finish(Errors.BIOMETRICS_GENERIC_GET_KEY);
        }
    }

    private void createKey(ResultListener<Integer> resultCreateKey) {
        Log.d(TAG, "Entra en createKey");
        boolean isKeyCreated = false;
        try {
            mKeyStore.load(null);
            // Set the alias of the entry in Android KeyStore where the key will appear
            // and the constrains (purposes) in the constructor of the Builder
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mKeyGenerator.init(new KeyGenParameterSpec.Builder(KEY_STORE_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                        .setUserAuthenticationRequired(true)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                        .build());
            }
            mKeyGenerator.generateKey();
            isKeyCreated = true;
            resultCreateKey.finish(1);
        } catch (NoSuchAlgorithmException e) {
            resultCreateKey.finish(Errors.BIOMETRICS_GENERIC_CREATE_KEY);
        } catch (InvalidAlgorithmParameterException e) {
            resultCreateKey.finish(Errors.BIOMETRICS_GENERIC_CREATE_KEY);
        } catch (CertificateException e) {
            resultCreateKey.finish(Errors.BIOMETRICS_GENERIC_CREATE_KEY);
        } catch (IOException e) {
            resultCreateKey.finish(Errors.BIOMETRICS_GENERIC_CREATE_KEY);
        }
        if (!isKeyCreated) {
            resultCreateKey.finish(Errors.BIOMETRICS_GENERIC_CREATE_KEY);
        }
    }

    /*
     * Los estados para la huella son:
     * 1 - Error en la autenticación
     * 2 - OK en la autenticación
     * 3 - Fallo en la autenticación
     * 4 - En la ayuda de la autenticación
     */
    // Abre la ventana modal con fingerprint
    private void fingerprintMode(final ResultListener<Integer> resultFingerPrintMode) {
        Log.d(TAG, "Entra en fingerprintMode");
        try {
            FingerprintManagerCompat.CryptoObject cryptoObject = new FingerprintManagerCompat.CryptoObject(mCipher);
            FingerprintManagerCompat fingerprintManagerCompat = FingerprintManagerCompat.from(mContext);

            fingerprintManagerCompat.authenticate(cryptoObject, 0, new CancellationSignal(), new FingerprintManagerCompat.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errMsgId, CharSequence errString) {
                    super.onAuthenticationError(errMsgId, errString);
                    Log.d(TAG, "Entra en onAuthenticationError");
                    dialog.updateStatus(1);
                    resultFingerPrintMode.finish(1);
                    dialog.closeDialog();
                }

                @Override
                public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                    super.onAuthenticationHelp(helpMsgId, helpString);
                    Log.d(TAG, "Entra en onAuthenticationHelp");
                    resultFingerPrintMode.finish(4);
                    dialog.updateStatus(4);
                }

                @Override
                public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    Log.d(TAG, "Entra en onAuthenticationSucceeded");
                    dialog.updateStatus(2);
                    resultFingerPrintMode.finish(2);
                    dialog.closeDialog();
                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    Log.d(TAG, "Entra en onAuthenticationFailed");
                    resultFingerPrintMode.finish(3);
                    dialog.updateStatus(3);
                }
            }, null);
        } catch (Exception e) {
            resultFingerPrintMode.finish(Errors.BIOMETRICS_OPENMODAL_CANT_OPEN);
        }
    }

    // Abre la ventana modal con biometrics
    @TargetApi(Build.VERSION_CODES.P)
    private void biometricMode(String title, String desc, final ResultListener<String> resultBiometricMode) {
        Log.d(TAG, "Entra en biometricMode");
        BiometricPrompt.CryptoObject cryptoObject = new BiometricPrompt.CryptoObject(mCipher);
        Executor executor = mActivity.getMainExecutor();
        mBiometricPrompt = new BiometricPrompt.Builder(mContext)
                .setTitle(title)
                .setDescription(desc)
                .setNegativeButton("Cancelar", mContext.getMainExecutor(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d(TAG, "Entra en el cancelar");
                        resultBiometricMode.finish("cancel");
                    }
                })
                .build();
        mBiometricPrompt.authenticate(cryptoObject, cancellationSignal, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                //super.onAuthenticationSucceeded(result);
                Log.d(TAG, "Entra en onAuthenticationSucceeded");
                resultBiometricMode.finish("success");
                cancellationSignal.cancel();
            }

            @Override
            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                //super.onAuthenticationHelp(helpCode, helpString);
                Log.d(TAG, "Entra en onAuthenticationHelp");
                resultBiometricMode.finish("help");
            }

            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                //super.onAuthenticationError(errorCode, errString);
                Log.d(TAG, "Entra en onAuthenticationError");
                resultBiometricMode.finish("error");
                cancellationSignal.cancel();
            }

            @Override
            public void onAuthenticationFailed() {
                Log.d(TAG, "Entra en onAuthenticationFailed");
                resultBiometricMode.finish("failed");
            }
        });
    }

    private void writeToSharedPreferences(String flag) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(BIOMETRICS_FLAG_KEY, flag);
        editor.apply();
    }



}
