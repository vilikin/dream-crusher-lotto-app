package in.vilik.dreamcrusher;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by vili on 06/03/2017.
 */

public class Debug {
    public static int DEBUG_LVL = 1;
    public static boolean SHOW_TOASTS = false;
    private static Context context;

    public static void print(String tag, String method, String msg, int lvl) {
        if (lvl <= DEBUG_LVL && BuildConfig.DEBUG) {
            Log.d(tag, method + ": " + msg);

            if (context != null) {
                String text = tag + ": " + method + ": " + msg;
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        }
    }

    public static void setContext(Context context) {
        Debug.context = context;
    }

    public static void loadDebug(Context host) {
        String tmpLevel = host.getResources().getString(R.string.debugLevel);
        String tmpShowToasts = host.getResources().getString(R.string.showToasts);
        context = host;

        try {
            DEBUG_LVL = Integer.parseInt(tmpLevel);
            print("Debug", "loadDebug(host)", "Successfully loaded debug level " + DEBUG_LVL + ".", 1);
        } catch (NumberFormatException e) {
            print("Debug", "loadDebug(host)",
                    "Number format exception, check debug.xml. " +
                            "Using debug level " + DEBUG_LVL + ".", 1);
        }

        try {
            SHOW_TOASTS = Integer.parseInt(tmpLevel) > 0;
            print("Debug", "loadDebug(host)", "Successfully loaded show toasts: " + SHOW_TOASTS + ".", 1);
        } catch (NumberFormatException e) {
            print("Debug", "loadDebug(host)",
                    "Number format exception, check debug.xml. " +
                            "Showing toasts: " + SHOW_TOASTS + ".", 1);
        }
    }
}
