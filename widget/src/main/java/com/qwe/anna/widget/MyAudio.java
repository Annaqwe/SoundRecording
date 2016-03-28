package com.qwe.anna.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Environment;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qwe.anna.widget.config.AudioConfig;

import java.io.IOException;
import java.util.ArrayList;

import static com.qwe.anna.widget.AudioUtils.getAudioPathsFromList;
import static com.qwe.anna.widget.AudioUtils.reloadAudioList;

public class MyAudio extends MyWidget {

    //点击开始录音的按钮
    private Button btnRecord;
    //标题
    private TextView textAudioTitle;
    //录音对话框
    private Dialog dlgRecord;
    private ImageButton imageTalkLevel;
    private TextView textRecord;
    //更新时间的线程
    private Thread threadTime;

    private ArrayList<String> audioList = null;
    //存放录音文件路径的值，通过“，”分隔开，不包含路径表示存在默认路径下
    private EditText audioArrayHolder;
    private LinearLayout lLayoutAudioChoice;

    //录音对象
    private MyRecorder recorder;
    private View mView;
    private Context mContext;

    private static int MAX_TIME = 0; // 最长录制时间，单位秒，0为无时间限制
    private static int MIX_TIME = 1; // 最短录制时间，单位秒，0为无时间限制，建议设为1
    private static int RECORD_NO = 0; // 不在录音
    private static int RECORD_ING = 1; // 正在录音
    private static int RECORD_ED = 2; // 完成录音
    private static int RECORD_STATE = 0; // 录音的状态
    private static float RECORD_TIME = 0.0f; // 录音的时间
    private static double VOICE_VALUE = 0.0; // 麦克风获取的音量值

    public MyAudio(Context context) {
        this(context, null, 0);
    }

    public MyAudio(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyAudio(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(R.layout.widget_audio, this, true);
        initViews(mView);
        TypedArray tArray = context.obtainStyledAttributes(attrs, R.styleable.MyAudio);
        String strTitle = tArray.getString(R.styleable.MyAudio_my_title);
        setTitle(strTitle);
        setType(AudioConfig.TYPE_AUDIO);
        setReadOnly(false);
    }

    @Override
    public void setTitle(String title) {
        super.setTitle(title);
        textAudioTitle.setText(title);
    }

    @Override
    public String getValue() {
        return audioArrayHolder.getText().toString();
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);
        audioArrayHolder.setText(value);
        audioList = getAudioPathsFromList(audioArrayHolder);
        reloadAudioList(audioList, mContext, lLayoutAudioChoice, null);
    }

    /**
     * 初始化
     */
    private void initViews(View view) {
        btnRecord = (Button) view.findViewById(R.id.pls_talk);
        lLayoutAudioChoice = (LinearLayout) view.findViewById(R.id.linear_widget_form_audio);
        audioArrayHolder = (EditText) view.findViewById(R.id.widget_form_audio_array_holder);
        textAudioTitle = (TextView) view.findViewById(R.id.kind_title);
        mFlagNotNull = (TextView) view.findViewById(R.id.flag_requred);
        mErrorInfo = (TextView) view.findViewById(R.id.errer_info);
        audioList = getAudioPathsFromList(audioArrayHolder);
        reloadAudioList(audioList, mContext, lLayoutAudioChoice, null);
    }

    private void VoiceStart() throws IOException {

        // 如果当前不是正在录音状态，开始录音
        if (RECORD_STATE != RECORD_ING) {
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                Toast.makeText(getContext(), R.string.sd_card_fail_open, Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                recorder = new MyRecorder();
            } catch (Exception e) {
                return;
            }
            RECORD_STATE = RECORD_ING;
            // 显示录音情况
            showVoiceDialog();
            // 开始录音
            recorder.start();
            // 计时线程
            myThread();
        }
    }

    private void VoiceStop() {
        // 如果是正在录音
        if (RECORD_STATE == RECORD_ING) {
            RECORD_STATE = RECORD_ED;
            // 如果录音图标正在显示,关闭
            if (dlgRecord.isShowing()) {
                dlgRecord.dismiss();
            }

            // 停止录音
            recorder.stop();
            VOICE_VALUE = 0.0;

            if (RECORD_TIME < MIX_TIME) {
                showWarnToast();
                RECORD_STATE = RECORD_NO;
            } else {
                //录音完成
                audioList.add(recorder.getPath());
                reloadAudioList(audioList, mContext, lLayoutAudioChoice, null);
            }
            //   btnRecord.setText(R.string.click_start_record);
        }
    }


    /**
     * 显示正在录音的图标
     */
    private void showVoiceDialog() {
        dlgRecord = new Dialog(mContext, R.style.DialogStyle);
        dlgRecord.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlgRecord.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        dlgRecord.setContentView(R.layout.widget_audio_talk_layout);
        imageTalkLevel = (ImageButton) dlgRecord.findViewById(R.id.talk_log);
        textRecord = (TextView) dlgRecord.findViewById(R.id.talk_tv);
        dlgRecord.show();
        dlgRecord.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dlgRecord) {
                VoiceStop();
            }
        });
    }

    /**
     * 录音时间太短时Toast显示
     */
    void showWarnToast() {
        Toast toast = new Toast(mContext);
        LinearLayout linearLayout = new LinearLayout(mContext);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(20, 20, 20, 20);

        // 定义一个ImageView
        ImageView imageView = new ImageView(mContext);
        imageView.setImageResource(R.drawable.form_widget_audio_talk_no); // 图标

        TextView mTv = new TextView(mContext);
        mTv.setText(R.string.time_not_enough_for_record);
        mTv.setTextSize(14);
        mTv.setTextColor(Color.WHITE);// 字体颜色
        // mTv.setPadding(0, 10, 0, 0);

        // 将ImageView和ToastView合并到Layout中
        linearLayout.addView(imageView);
        linearLayout.addView(mTv);
        linearLayout.setGravity(Gravity.CENTER);// 内容居中
        linearLayout.setBackgroundResource(R.drawable.form_widget_audio_trans_round_bg);// 设置自定义toast的背景

        toast.setView(linearLayout);
        toast.setGravity(Gravity.CENTER, 0, 0);// 起点位置为中间 100为向下移100dp
        toast.show();
    }

    // 录音Dialog图片随声音大小切换
    private void setDialogImage() {
        if (VOICE_VALUE < 200.0) {
            imageTalkLevel.setImageResource(R.drawable.form_widget_audio_record_animate_01);
        } else if (VOICE_VALUE > 200.0 && VOICE_VALUE < 400) {
            imageTalkLevel.setImageResource(R.drawable.form_widget_audio_record_animate_02);
        } else if (VOICE_VALUE > 400.0 && VOICE_VALUE < 800) {
            imageTalkLevel.setImageResource(R.drawable.form_widget_audio_record_animate_03);
        } else if (VOICE_VALUE > 800.0 && VOICE_VALUE < 1600) {
            imageTalkLevel.setImageResource(R.drawable.form_widget_audio_record_animate_04);
        } else if (VOICE_VALUE > 1600.0 && VOICE_VALUE < 3200) {
            imageTalkLevel.setImageResource(R.drawable.form_widget_audio_record_animate_05);
        } else if (VOICE_VALUE > 3200.0 && VOICE_VALUE < 5000) {
            imageTalkLevel.setImageResource(R.drawable.form_widget_audio_record_animate_06);
        } else if (VOICE_VALUE > 5000.0 && VOICE_VALUE < 7000) {
            imageTalkLevel.setImageResource(R.drawable.form_widget_audio_record_animate_07);
        } else if (VOICE_VALUE > 7000.0 && VOICE_VALUE < 10000.0) {
            imageTalkLevel.setImageResource(R.drawable.form_widget_audio_record_animate_08);
        } else if (VOICE_VALUE > 10000.0 && VOICE_VALUE < 14000.0) {
            imageTalkLevel.setImageResource(R.drawable.form_widget_audio_record_animate_09);
        } else if (VOICE_VALUE > 14000.0 && VOICE_VALUE < 17000.0) {
            imageTalkLevel.setImageResource(R.drawable.form_widget_audio_record_animate_10);
        } else if (VOICE_VALUE > 17000.0 && VOICE_VALUE < 20000.0) {
            imageTalkLevel.setImageResource(R.drawable.form_widget_audio_record_animate_11);
        } else if (VOICE_VALUE > 20000.0 && VOICE_VALUE < 24000.0) {
            imageTalkLevel.setImageResource(R.drawable.form_widget_audio_record_animate_12);
        } else if (VOICE_VALUE > 24000.0 && VOICE_VALUE < 28000.0) {
            imageTalkLevel.setImageResource(R.drawable.form_widget_audio_record_animate_13);
        }
    }

    /**
     * 录音计时线程
     */
    private void myThread() {
        threadTime = new Thread(ImageThread);
        threadTime.start();
    }

    /**
     * 录音线程
     */
    private Runnable ImageThread = new Runnable() {

        @Override
        public void run() {
            RECORD_TIME = 0.0f;
            // 如果是正在录音状态
            while (RECORD_STATE == RECORD_ING) {
                if (RECORD_TIME >= MAX_TIME && MAX_TIME != 0) {
                    handler.sendEmptyMessage(0x10);
                } else {
                    try {
                        Thread.sleep(200);

                        RECORD_TIME += 0.2;
                        if (RECORD_STATE == RECORD_ING) {
                            VOICE_VALUE = recorder.getAmplitude();
                            handler.sendEmptyMessage(0x11);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }

        }

        Handler handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case 0x10:
                        // 录音超过15秒自动停止,录音状态设为语音完成
                        if (RECORD_STATE == RECORD_ING) {
                            RECORD_STATE = RECORD_ED;
                            // 如果录音图标正在显示的话,关闭显示图标
                            if (dlgRecord.isShowing()) {
                                dlgRecord.dismiss();
                            }
                            // 停止录音
                            recorder.stop();
                            VOICE_VALUE = 0.0;
                            // 如果录音时长小于1秒，显示录音失败的图标
                            if (RECORD_TIME < 1.0) {
                                showWarnToast();
                                RECORD_STATE = RECORD_NO;
                            }
                        }
                        break;

                    case 0x11:
                        textRecord.setText(ParseTimeUtil.format((long) RECORD_TIME));
                        setDialogImage();
                        break;
                }
            }

            ;
        };
    };

    @Override
    public void setReadOnly(Boolean bReadOnly) {
        super.setReadOnly(bReadOnly);
        if (bReadOnly) {
            btnRecord.setVisibility(View.GONE);
        } else {
            btnRecord.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        VoiceStart();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
