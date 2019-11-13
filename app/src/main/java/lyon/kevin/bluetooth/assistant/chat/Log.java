package lyon.kevin.bluetooth.assistant.chat;



public class Log {
    public final static String TAG = "Lyon";
    static boolean isDebug = true;
    public static void d(String tag, String msg) {
        if(isDebug) {
            android.util.Log.d(TAG + "/" + tag, msg);
//            AppController.getInstance().setLogInfo("d", TAG + "/" + tag, msg);
        }
    }
    public static void i(String tag, String msg) {
        if(isDebug) {
            android.util.Log.i(TAG + "/" + tag, msg);
//            AppController.getInstance().setLogInfo("i", TAG + "/" + tag, msg);
        }
    }
    public static void e(String tag, String msg) {
        if(isDebug) {
            android.util.Log.e(TAG + "/" + tag, msg);
        }
    }

    public static void e(String tag, String msg, Exception e) {
        if(isDebug) {
            android.util.Log.e(TAG + "/" + tag, msg+" e:"+e);
        }
    }

    public static void w(String tag, String msg) {
        if(isDebug) {
            android.util.Log.e(TAG + "/" + tag, msg);
        }
    }

    public static void v(String tag, String msg) {
        if(isDebug) {
            android.util.Log.v(TAG + "/" + tag, msg);
        }
    }
}
