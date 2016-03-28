package com.qwe.anna.widget.util;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import com.qwe.anna.widget.R;
import java.io.File;
public class CommonUtils {

    public static boolean checkSDCard(Context context){
        if(!IsCanUseSdCard())
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                    .setCancelable(true)
                    .setIcon(R.drawable.ic_dialog_alert_light)
                    .setNegativeButton(R.string.config_ok, null)
                    .setTitle(R.string.sd_card_fail_open);
            builder.show();
            return false;
        }
        return true;
    }
    /*
      sdCard是否可读写
     */
    public static boolean IsCanUseSdCard() {
        try {
            return Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
        /**
         * 获取文件名称
         *
         * @param strPath 文件路径
         * @return 文件名称
         */
    public static String getNameFromPath(String strPath) {
        String strName = new String();
        String[] strArr = null;
        if (strPath != null) {
            strArr = strPath.split("/");
            if (strArr != null)
                strName = strArr[strArr.length - 1];
        }
        return strName;
    }
    public static void makeRootDirectory(String filePath) {
        if (IsCanUseSdCard()) {
            File file = null;
            try {
                file = new File(filePath);
                if (!file.exists()) {
                    file.mkdirs();
                }
            } catch (Exception e) {

            }
        }
    }
 //   final private String mStrNoMedia = ".nomedia";
    public static void makeMediaRootDirectory(String filePath) {
        if (IsCanUseSdCard()) {
            File file = null;
            try {
                file = new File(filePath);
                if (!file.exists()) {
                    file.mkdirs();
                    new File(filePath,"/.nomedia").createNewFile();
                }
            } catch (Exception e) {
                Log.i("error", e.toString());
            }
        }
    }
}
