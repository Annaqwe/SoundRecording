package com.qwe.anna.widget;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.qwe.anna.widget.util.CommonUtils;

import java.io.File;
import java.util.ArrayList;

import static com.qwe.anna.widget.AudioUtils.createConfirmationDialog;
import static com.qwe.anna.widget.AudioUtils.getNameFromPath;
import static com.qwe.anna.widget.AudioUtils.reloadAudioList;

public class UIFormAudioItemAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<String> mList;
    protected LayoutInflater mInflater;
    private LinearLayout mLinearLayoutImageChoice;
    private int mnStyle = 1;

    /**
     * MediaPlayer
     */
    public MediaPlayer media;

    /**
     * 更新进度条的线程
     */
    private Thread barThread;

    public AudioViewHolder mViewHolder;

    public UIFormAudioItemAdapter(Context context, ArrayList<String> list, LinearLayout linearLayoutImageChoice) {
        this.mContext = context;
        this.mList = list;
        mInflater = LayoutInflater.from(context);
        mLinearLayoutImageChoice = linearLayoutImageChoice;
    }

    public void setStyle(int nStyle) {
        mnStyle = nStyle;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final String path = mList.get(position);
        convertView = mInflater.inflate(R.layout.widget_audio_template_item, null);
        if (mViewHolder == null)
            mViewHolder = new AudioViewHolder();
        mViewHolder.mFrameLayout = (FrameLayout) convertView.findViewById(R.id.framelayout);
        mViewHolder.mTextView = (TextView) convertView.findViewById(R.id.time_text);
        mViewHolder.mButtonRemove = (ImageButton) convertView.findViewById(R.id.child_remove);
        mViewHolder.mProgressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);
        mViewHolder.mPath = (TextView) convertView.findViewById(R.id.audio_path_text);
        mViewHolder.mTextView.setText(getNameFromPath(path));

        mViewHolder.mPath.setText(path);
        if (mnStyle == 0) {
            mViewHolder.mButtonRemove.setVisibility(View.GONE);
        } else {
            //删除按钮
            mViewHolder.mButtonRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!CommonUtils.IsCanUseSdCard()) {
                        Toast.makeText(mContext, R.string.sd_card_fail_open, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    createConfirmationDialog(mContext, -1, null, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //删除文件
                            mViewHolder.mFrameLayout.setVisibility(View.GONE);
                            mList.remove(path);
                            new File(path).delete();
                            reloadAudioList(mList, mContext, mLinearLayoutImageChoice, null);
                        }


                    }, null);

                }
            });
        }
        convertView.setTag(mViewHolder);
        return convertView;
    }
}
