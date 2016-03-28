package com.qwe.anna.widget;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.qwe.anna.widget.config.AudioConfig;
import com.qwe.anna.widget.util.CommonUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
public class AudioUtils {

    private static AudioViewHolder mViewHolder = null;
    private static AudioViewHolder mPreViewHolder = null;
    static Context mContext = null;
    /**
     * 更新进度条的线程
     */
    private static Thread barThread = null;
    /**
     * MediaPlayer
     */
    public static MediaPlayer media = null;
    public static boolean playState = false; // 播放状态

    public static ArrayList<String> getAudioPathsFromList(EditText audiosArrayHolder) {
        if (audiosArrayHolder == null)
            return null;
        String curImageListStr = audiosArrayHolder.getText().toString();
        if (curImageListStr.length() < 3) {
            return new ArrayList<String>();
        } else {
            ArrayList<String> arrayList = new ArrayList<String>(Arrays.asList(curImageListStr.split(",")));
            for (int i = 0; i < arrayList.size(); i++) {
                String string = arrayList.get(i);
                int len = string.split("/").length;
                //如果不包含路径，则默认为存在默认位置里
                if (len < 2)
                    arrayList.set(i, AudioConfig.getAudioDownloadPath() + "/" + string);
            }
            return arrayList;
        }
    }

    public static void reloadAudioList(ArrayList<String> curAudioList, Context context, LinearLayout linearLayoutAudioChoice, String strStyle) {
        final View viewWidgetFormAudio = linearLayoutAudioChoice.findViewById(R.id.widget_form_audio);
        final GridView audioGridList = (GridView) linearLayoutAudioChoice.findViewById(R.id.widget_form_audio_child_grid);
        final TextView audioDescription = (TextView) linearLayoutAudioChoice.findViewById(R.id.widget_form_audio_description);
        final EditText audioArrayHolder = (EditText) linearLayoutAudioChoice.findViewById(R.id.widget_form_audio_array_holder);

        if (curAudioList.size() < 1) {
            viewWidgetFormAudio.setVisibility(View.GONE);
            audioDescription.setText("无录音");
            return;
        }
        mContext = context;
        viewWidgetFormAudio.setVisibility(View.VISIBLE);
        final UIFormAudioItemAdapter uiFormAudioItemAdapter = new UIFormAudioItemAdapter(context, curAudioList, linearLayoutAudioChoice);
        audioGridList.setAdapter(uiFormAudioItemAdapter);

        audioGridList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (mViewHolder == null)
                    mViewHolder = new AudioViewHolder();
                if (mViewHolder != null) {
                    if (mPreViewHolder == null)
                        mPreViewHolder = new AudioViewHolder();
                    mPreViewHolder.mButtonRemove = mViewHolder.mButtonRemove;
                    mPreViewHolder.mPlay = mViewHolder.mPlay;
                    mPreViewHolder.mTextView = mViewHolder.mTextView;
                }
                mViewHolder.mFrameLayout = (FrameLayout) view.findViewById(R.id.framelayout);
                mViewHolder.mTextView = (TextView) view.findViewById(R.id.time_text);
                mViewHolder.mButtonRemove = (ImageButton) view.findViewById(R.id.child_remove);
                mViewHolder.mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
                mViewHolder.mPath = (TextView) view.findViewById(R.id.audio_path_text);
                mViewHolder.mPlay = (ImageButton) view.findViewById(R.id.bt_play_audio);
                try {
                    onPlayClick(view);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        audioDescription.setText(String.valueOf(curAudioList.size()) + " 段录音");
        //重新适应横向滚动
        DisplayMetrics outMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        float density = outMetrics.density; // 像素密度
        ViewGroup.LayoutParams params = audioGridList.getLayoutParams();
        int itemWidth = (int) (140 * density);
        int spacingWidth = (int) (10 * density);
        int listSize = curAudioList.size();
        params.width = itemWidth * listSize + (listSize) * spacingWidth;
        audioGridList.setStretchMode(GridView.NO_STRETCH);
        setAudioPathsFromList(curAudioList, audioArrayHolder);
    }


    /**
     * 获取路径列表
     *
     * @param audioArrayHolder
     * @return
     */
    public static void setAudioPathsFromList(ArrayList<String> curAudioList, EditText audioArrayHolder) {
        String strList = curAudioList.toString();
        if (strList.length() < 5) {
            audioArrayHolder.setText("");
            return;
        } else {
            String curImageListStr = strList.substring(1, strList.length() - 1).replace(" ", "");
            audioArrayHolder.setText(curImageListStr);
        }
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void onPlayClick(View v) throws IOException {
        //没有播放
        if (!playState) {
            playCurAudio();
        } else {
            // 如果正在播放
            if (media.isPlaying()) {
                //如果两次点击不为同一文件则停止旧文件 播放新文件
                if (mPreViewHolder != null && !mPreViewHolder.mTextView.getText().equals(mViewHolder.mTextView.getText())) {
                    media.stop();
                    mPreViewHolder.mButtonRemove.setVisibility(View.VISIBLE);
                    if (Build.VERSION.SDK_INT >= 16)
                        mPreViewHolder.mPlay.setBackground(((Activity) mContext).getResources().getDrawable(R.drawable.form_widget_audio_play));
                    else
                        mPreViewHolder.mPlay.setBackgroundDrawable(((Activity) mContext).getResources().getDrawable(R.drawable.form_widget_audio_play));
                    playCurAudio();
                } else {
                    media.stop();
                    playState = false;
                    mViewHolder.mButtonRemove.setVisibility(View.VISIBLE);
                }
            } else {
                playState = false;
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    //播放当前选中的文件
    private static void playCurAudio() {
        // 如果不是正在播放
        if (!CommonUtils.IsCanUseSdCard()) {
            Toast.makeText(mContext, R.string.sd_card_fail_open, Toast.LENGTH_SHORT).show();
            return;
        }
        if (media == null)
            media = new MediaPlayer();
        if (media.isPlaying())
            media.stop();
        media.reset();
        File file = new File(mViewHolder.mPath.getText().toString());
        if (!file.exists()) {
            Toast.makeText(mContext, R.string.file_not_exit, Toast.LENGTH_SHORT).show();
            return;
        }
        // 设置播放资源
        try {
            media.setDataSource(file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            try {
                media.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 开始播放
            media.start();
            media.setLooping(false);
            mViewHolder.mButtonRemove.setVisibility(View.INVISIBLE);
            if (Build.VERSION.SDK_INT >= 16)
                mViewHolder.mPlay.setBackground(((Activity) mContext).getResources().getDrawable(R.drawable.form_widget_audio_pause));
            else
                mViewHolder.mPlay.setBackgroundDrawable(((Activity) mContext).getResources().getDrawable(R.drawable.form_widget_audio_pause));

            Log.d("audio-max", String.format("%d", (int) media.getDuration()));
            mViewHolder.mProgressBar.setMax(((int) media.getDuration()));
            barUpdate();
            // 改变播放的标志位
            playState = true;

            // 设置播放结束时监听
            media.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Log.d("audio", "stop");
                    playState = false;
                    if (media != null && media.isPlaying()) {
                        media.stop();
                    }
                    //                     mViewHolder.mProgressBar.setProgress(0);
                    mViewHolder.mButtonRemove.setVisibility(View.VISIBLE);
                    if (Build.VERSION.SDK_INT >= 16)
                        mViewHolder.mPlay.setBackground(((Activity) mContext).getResources().getDrawable(R.drawable.form_widget_audio_play));
                    else
                        mViewHolder.mPlay.setBackgroundDrawable(((Activity) mContext).getResources().getDrawable(R.drawable.form_widget_audio_play));
                }
            });
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    /**
     * 进度条线程
     */
    private static void barUpdate() {
        barThread = new Thread(BarUpdateThread);
        barThread.start();
    }

    /**
     * 进度条更新线程
     */
    private static Runnable BarUpdateThread = new Runnable() {

        @Override
        public void run() {
            for (mViewHolder.mProgressBar.getProgress(); (mViewHolder.mProgressBar.getProgress() < mViewHolder.mProgressBar.getMax()) && playState; ) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.sendEmptyMessage(0x12);
            }
        }

        Handler handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == 0x12) {
                    Log.d("audio", String.format("%d", (int) media.getCurrentPosition()));
                    mViewHolder.mProgressBar.setProgress(((int) media.getCurrentPosition()));
                }
            }

            ;
        };
    };

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

    private static void initAudio() {
        mViewHolder = null;
        mPreViewHolder = null;
        mContext = null;
        /**更新进度条的线程*/
        barThread = null;
        /** MediaPlayer */
        media = null;
        playState = false;
    }

    public static void onDeleteAudio() {

    }

    /**
     * 创建一个确认对话框
     *
     * @param context        the context
     * @param messageId      the messageId
     * @param view           the view
     * @param okListener     the listener when OK is clicked
     * @param cancelListener the listener when cancel is clicked
     */
    public static Dialog createConfirmationDialog(
            Context context, int messageId, View view, DialogInterface.OnClickListener okListener,
            DialogInterface.OnClickListener cancelListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setCancelable(true)
                .setIcon(R.drawable.ic_dialog_alert_light)
                .setNegativeButton(R.string.config_ok, okListener)
                .setPositiveButton(R.string.config_cancel, cancelListener)
                .setTitle(R.string.config_delete);
        if (messageId != -1) {
            builder.setMessage(messageId);
        }
        if (view != null) {
            builder.setView(view);
        }
        return builder.show();
    }

}
