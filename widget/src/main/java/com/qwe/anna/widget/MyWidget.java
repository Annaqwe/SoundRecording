package com.qwe.anna.widget;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Map;

/**
 * 控件基类 继承LinearLayout布局
 */
public class MyWidget extends LinearLayout implements Editor {
    private String name;
    private String title;
    private String type;
    private String description;
    private int priority;
    private Map<String, String> style;
    private Map<String, String> toggles;
    private String value = null;
    private View mTogglesHolder;
    private static Boolean mbReadOnly = false;
    private static Boolean mbInvalid = false;
    protected TextView mFlagNotNull = null;
    protected TextView mErrorInfo = null;

    public static Boolean getReadOnly() {
        return mbReadOnly;
    }

    public static Boolean isInvalid() {
        return mbInvalid;
    }

    public static void setInvalidStatus(Boolean bInvalid) {
        MyWidget.mbInvalid = bInvalid;
    }

    public void setReadOnly(Boolean bReadOnly) {
        this.mbReadOnly = bReadOnly;
    }

    public void onResume() {

    }

    //数据监听器
    private EditorListener mEditorListener;

    public MyWidget(Context context) {
        this(context, null, 0);
    }

    public MyWidget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyWidget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    ;

    @Override
    public void setEditorListener(EditorListener editorListener) {
        mEditorListener = editorListener;
    }

    @Override
    public void onRequestResult(int requestCode, int resultCode, Intent data) {
        //打开其他Context返回数据
    }

    public void checkEntryInvalid() {
        if (style != null && style.get("notNull") != null && style.get("notNull").equals("true") && isValueNull()) {
            setInvalidStatus(true);
            if (mErrorInfo != null) {
                mErrorInfo.setVisibility(View.VISIBLE);
                setError("不能为空");
            }
        } else {
            setInvalidStatus(false);
            if (mErrorInfo != null)
                mErrorInfo.setVisibility(View.GONE);
        }
    }

    public void setError(String string) {
        if (mErrorInfo != null) {
            mErrorInfo.setText(string);
            mErrorInfo.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public EditorListener getEditorListener() {
        return mEditorListener;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public View getTogglesHolder() {
        return mTogglesHolder;
    }

    public Map<String, String> getStyle() {
        return style;
    }

    public void setStyle(Map<String, String> style) {
        this.style = style;
        if (style != null) {
            if (style.get("visible") != null && style.get("visible").equals("false"))
                this.setVisibility(View.GONE);
            if (style.get("notNull") != null && style.get("notNull").equals("true")) {
                if (mFlagNotNull != null)
                    mFlagNotNull.setVisibility(View.VISIBLE);
            } else {
                if (mFlagNotNull != null)
                    mFlagNotNull.setVisibility(View.GONE);
            }
        }
    }

    public String getValue() {
        return value;
    }

    public boolean isValueNull() {
        if (getValue() == null || getValue().equals(""))
            return true;
        return false;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Map<String, String> getToggles() {
        return toggles;
    }

    public void setToggles(Map<String, String> toggles, View togglesHolder) {
        this.toggles = toggles;
        this.mTogglesHolder = togglesHolder;
    }
}
