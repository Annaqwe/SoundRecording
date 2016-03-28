package com.qwe.anna.widget;

import android.content.Intent;

/**
 * @author leenanxi
 */
public interface Editor {
    public interface EditorListener {

        /**
         * 数据变化监听器
         * @param beforeData 变化前
         * @param afterData  变化后
         */
        public void onDataChanged(String beforeData, String afterData);


        //选择图片
        public static final int REQUEST_PICK_PHOTO = 1;
        //选择文件
        public static final int REQUEST_PICK_FILE = 12;
        public static final int FIELD_CHANGED = 3;
        public static final int FIELD_TURNED_EMPTY = 4;
        public static final int FIELD_TURNED_NON_EMPTY = 5;

        // 编辑器不同标石相同的数据 data, e.g. 例如从全名变为 结构化名称
        public static final int EDITOR_FORM_CHANGED = 6;
    }
    /**
     * 添加数据变化监听器
     *
     * @param editorListener 数据变化监听器
     */
    public void setEditorListener(EditorListener editorListener);

    /**
     * 获取控件的返回
     * @param resultCode
     * @param data
     */
    public void onRequestResult(int requestCode, int resultCode, Intent data);

    /**
     * 获取数据监听
     */
    public EditorListener getEditorListener();
}

