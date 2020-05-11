package com.rsi.nba.util;

import android.content.Context;

import com.rsi.nba.R;
import com.rsi.nba.model.Data;

public class Errors {

    //Default Error
    public static final int GENERIC_EVENT_MANAGER               = 98;
    public static final int DEFAULT_EVENT_MANAGER               = 99;

    // Utils Errors
    public static final int UTILS_NO_VIBRATION_HARDWARE         = 201;
    public static final int UTILS_VIBRATION_EXCEPTION           = 202;
    public static final int UTILS_ZENMODE_EXCEPTION             = 203;
    public static final int UTILS_LISTEN_NETWORK_EXCEPTION      = 204;
    public static final int UTILS_COPYCLIPBOARD_EXCEPTION       = 211;
    public static final int UTILS_OPERATIVE_SYSTEM_EXCEPTION    = 221;
    public static final int UTILS_PAIR_GENERATOR_EXCEPTION      = 231;
    public static final int UTILS_CIPHER_NO_PRIVATEKEY          = 241;
    public static final int UTILS_CIPHER_EXCEPTION              = 242;
    public static final int UTILS_SIGN_NO_PRIVATEKEY            = 243;
    public static final int UTILS_SIGN_EXCEPTION                = 244;
    public static final int UTILS_UUID_EXCEPTION                = 251;
    public static final int UTILS_DOWNLOAD_PDF                  = 261;
    public static final int UTILS_DOWNLOAD_PDF_NO_PERMISSION    = 262;
    public static final int UTILS_DEVICE_MODEL_EXCEPTION        = 271;
    public static final int UTILS_NOTCH_EXCEPTION               = 281;
    public static final int UTILS_GENERIC_ERROR                 = 291;
    public static final int UTILS_GENERIC_NO_FUNCTION           = 292;
    public static final int UTILS_GENERIC_IS_ROOTED             = 491;

    // OpenApp ERRORS
   public static final int APPOPENER_OPEN_LAUNCHINTENT_NULL     = 101;
   public static final int APPOPENER_OPEN_EXCEPTION             = 102;
   public static final int APPOPENER_CHECK_APP_NOT_INSTALLED    = 111;
   public static final int APPOPENER_OPENMARKET_ERROR           = 121;
   public static final int APPOPENER_GENERIC_ERROR              = 191;
   public static final int APPOPENER_GENERIC_NO_FUNCTION        = 192;

   // Biometrics Errors
   public static final int BIOMETRICS_LOWER_VERSION             = 301;
   public static final int BIOMETRICS_NO_HARDWARE_DETECTED      = 302;
   public static final int BIOMETRICS_NO_FINGERPRINT            = 303;
   public static final int BIOMETRICS_AVAILABILITY_EXCEPTION    = 304;
   public static final int BIOMETRICS_OPENMODAL_CANT_OPEN       = 310;
   public static final int BIOMETRICS_OPENMODAL_ERROR           = 311;
   public static final int BIOMETRICS_OPENMODAL_HELP            = 312;
   public static final int BIOMETRICS_OPENMODAL_FAILED          = 313;
   public static final int BIOMETRICS_OPENMODAL_CANCEL          = 314;
   public static final int BIOMETRICS_CLOSEMODAL_CANT_CLOSE     = 315;
   public static final int BIOMETRICS_UPDATE_FLAG               = 320;
   public static final int BIOMETRICS_INSERT_FLAG               = 321;
   public static final int BIOMETRICS_GET_FLAG                  = 322;
   public static final int BIOMETRICS_GENERIC_ERROR             = 391;
   public static final int BIOMETRICS_GENERIC_NO_FUNCTION       = 392;
   public static final int BIOMETRICS_GENERIC_CIPHER            = 393;
   public static final int BIOMETRICS_GENERIC_GET_KEY           = 394;
   public static final int BIOMETRICS_GENERIC_CREATE_KEY        = 395;

   //PUSH Errors
   public static final int PUSH_UPDATE_FLAG_ERROR              = 501;
   public static final int PUSH_GET_FLAG_ERROR                 = 511;
   public static final int PUSH_GET_TOKEN_ID_ERROR             = 521;
   public static final int PUSH_GENERATE_INSTANCE_ID_ERROR     = 531;

   public static final int PUSH_GENERIC_ERROR                  = 591;
   public static final int PUSH_GENERIC_NO_FUNCTION            = 592;


    //Atributos
    private Context context;
    private Integer code;
    private String description;

    //Constructor
    public Errors(Context context) {
        this.context = context;
    }

    //Metodos

    public Data getDataError(int code){

        Data errorData = new Data();

        errorData.setCode(code);
        errorData.setDescription(getDescription(code));

        return errorData;
    }

    public String getDescription(int code){
        switch(code){
            case 98:
                return context.getString(R.string.error_98);
            case 99:
                return context.getString(R.string.error_99);
            case 201:
                return context.getString(R.string.error_201);
            case 202:
                return context.getString(R.string.error_202);
            case 203:
                return context.getString(R.string.error_203);
            case 204:
                return context.getString(R.string.error_204);
            case 211:
                return context.getString(R.string.error_211);
            case 221:
                return context.getString(R.string.error_221);
            case 231:
                return context.getString(R.string.error_231);
            case 241:
                return context.getString(R.string.error_241);
            case 242:
                return context.getString(R.string.error_242);
            case 243:
                return context.getString(R.string.error_243);
            case 244:
                return context.getString(R.string.error_244);
            case 251:
                return context.getString(R.string.error_251);
            case 261:
                return context.getString(R.string.error_261);
            case 262:
                return context.getString(R.string.error_262);
            case 271:
                return context.getString(R.string.error_271);
            case 281:
                return context.getString(R.string.error_281);
            case 291:
                return context.getString(R.string.error_291);
            case 292:
                return context.getString(R.string.error_292);
            case 491:
                return context.getString(R.string.error_491);
            case 101:
                return context.getString(R.string.error_101);
            case 102:
                return context.getString(R.string.error_102);
            case 111:
                return context.getString(R.string.error_111);
            case 121:
                return context.getString(R.string.error_121);
            case 191:
                return context.getString(R.string.error_191);
            case 192:
                return context.getString(R.string.error_192);
            case 301:
                return context.getString(R.string.error_301);
            case 302:
                return context.getString(R.string.error_302);
            case 303:
                return context.getString(R.string.error_303);
            case 304:
                return context.getString(R.string.error_304);
            case 310:
                return context.getString(R.string.error_310);
            case 311:
                return context.getString(R.string.error_311);
            case 312:
                return context.getString(R.string.error_312);
            case 313:
                return context.getString(R.string.error_313);
            case 314:
                return context.getString(R.string.error_314);
            case 315:
                return context.getString(R.string.error_315);
            case 320:
                return context.getString(R.string.error_320);
            case 321:
                return context.getString(R.string.error_321);
            case 322:
                return context.getString(R.string.error_322);
            case 391:
                return context.getString(R.string.error_391);
            case 392:
                return context.getString(R.string.error_392);
            case 393:
                return context.getString(R.string.error_393);
            case 394:
                return context.getString(R.string.error_394);
            case 395:
                return context.getString(R.string.error_395);
            case 501:
                return context.getString(R.string.error_501);
            case 511:
                return context.getString(R.string.error_511);
            case 521:
                return context.getString(R.string.error_521);
            case 531:
                return context.getString(R.string.error_531);
            case 591:
                return context.getString(R.string.error_591);
            case 592:
                return context.getString(R.string.error_592);
            default:
                return "";
        }
    }

}
