package com.loften.android.api.app;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Presentation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.loften.android.api.R;

import static com.loften.android.api.R.id.modes;

public class PresentationActivity extends AppCompatActivity
        implements CompoundButton.OnCheckedChangeListener, View.OnClickListener, AdapterView.OnItemSelectedListener {
    private final String TAG = "PresentationActivity";

    private static final String PRESENTATION_KEY = "presentation";

    private CheckBox showAllDisplays;
    private ListView displayList;

    private static final int[] PHOTOS = new int[]{
            R.drawable.aisi,R.drawable.lufei,
            R.drawable.luobin,R.drawable.namei,
            R.drawable.shanzhi,R.drawable.suolong
    };

    private DisplayManager mDisplayManager;
    private DisplayListAdapter mDisplayListAdapter;
    private int mNextImageNumber;

    /**
     * 利用SparseArray存储所有屏幕的prsentation上显示的内容（尤其是当屏幕不止一个时数组的作用更大）
     * SparseArray：稀疏数组，在存储数据不多的情况下可以大大地节省空间，此时可以代替hasmap使用
     */
    private SparseArray<DemoPresentationContents> mSavedPresentationContents;
    /**
     * 根据displayId记录当前显示的presentation
     */
    private final SparseArray<DemoPresentation> mActivePresentations =
            new SparseArray<DemoPresentation>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mSavedPresentationContents =
                    savedInstanceState.getSparseParcelableArray(PRESENTATION_KEY);
        } else {
            mSavedPresentationContents = new SparseArray<DemoPresentationContents>();
        }

        mDisplayManager = (DisplayManager)getSystemService(Context.DISPLAY_SERVICE);

        setContentView(R.layout.activity_presentation);
        initView();
    }

    private void initView() {
        showAllDisplays = (CheckBox) findViewById(R.id.show_all_displays);
        showAllDisplays.setOnCheckedChangeListener(this);
        displayList = (ListView) findViewById(R.id.display_list);
        mDisplayListAdapter = new DisplayListAdapter(this);
        displayList.setAdapter(mDisplayListAdapter);
    }

    /**
     * 当activity恢复时，首先从之前在pause状态中保存的presentation中显示的内容恢复过来
     */
    @Override
    protected void onResume() {
        super.onResume();

        // 更新listView
        mDisplayListAdapter.updateContents();

        // 恢复保存的presentationContent
        final int numDisplays = mDisplayListAdapter.getCount();
        for (int i = 0; i < numDisplays; i++) {
            final Display display = mDisplayListAdapter.getItem(i);
            final DemoPresentationContents contents =
                    mSavedPresentationContents.get(display.getDisplayId());
            if (contents != null) {
                showPresentation(display, contents);
            }
        }
        //恢复完成后清空mSavedPresentationContents中的内容
        mSavedPresentationContents.clear();

        // 注册屏幕的监听事件
        mDisplayManager.registerDisplayListener(mDisplayListener, null);
    }

    /**
     * 当activity不可见时保存presentation中显示的内容 将所有屏幕的presentation解除
     */
    @Override
    protected void onPause() {
        super.onPause();

        // 注销掉对屏幕的监听
        mDisplayManager.unregisterDisplayListener(mDisplayListener);

        // 遍历当前所有可见的presentation
        Log.d(TAG, "Activity is being paused.  Dismissing all active presentation.");
        for (int i = 0; i < mActivePresentations.size(); i++) {
            // 如果当前的presentation是可见的则将共内容保存
            DemoPresentation presentation = mActivePresentations.valueAt(i);
            int displayId = mActivePresentations.keyAt(i);
            // 将所有display的显示内容保存
            mSavedPresentationContents.put(displayId, presentation.mContents);
            presentation.dismiss();
        }
        // 清空所mActivePresentations数组（所有presentation都不可见了）
        mActivePresentations.clear();
    }

    /**
     * 保存presentation的状态信息，用于当activity被非正常杀死时再次调用oncreate方法重建activity时 恢复状态
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Be sure to call the super class.
        super.onSaveInstanceState(outState);
        outState.putSparseParcelableArray(PRESENTATION_KEY, mSavedPresentationContents);
    }

    /**
     * 点击时创建一个对话框，用于显示dispaly的信息
     */
    @Override
    public void onClick(View view) {
        Context context = view.getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final Display display = (Display)view.getTag();
        Resources r = context.getResources();
        AlertDialog alert = builder
                .setTitle(r.getString(
                        R.string.presentation_alert_info_text, display.getDisplayId()))
                .setMessage(display.toString())
                .setNeutralButton(R.string.presentation_alert_dismiss_text,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                .create();
        alert.show();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        final Display display = (Display)parent.getTag();
        final Display.Mode[] modes = display.getSupportedModes();
        setPresentationDisplayMode(display, position >= 1 && position <= modes.length ?
                modes[position - 1].getModeId() : 0);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        final Display display = (Display)parent.getTag();
        setPresentationDisplayMode(display, 0);
    }

    /**
     * 同时为两个复选框提供监听服务
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == showAllDisplays) {
            // 更新列表框
            mDisplayListAdapter.updateContents();
        } else {
            // 从复选框中取出Display
            final Display display = (Display)buttonView.getTag();
            if (isChecked) {
                // 显示下一张图片
                DemoPresentationContents contents = new DemoPresentationContents(getNextPhoto());
                showPresentation(display, contents);
            } else {
                hidePresentation(display);
            }
            mDisplayListAdapter.updateContents();
        }
    }

    private void showPresentation(Display display, DemoPresentationContents contents) {
        final int displayId = display.getDisplayId();
        // 如果当前resentation已经是显示状态则直接返回
        if (mActivePresentations.get(displayId) != null) {
            return;
        }

        Log.d(TAG, "Showing presentation photo #" + contents.photo
                + " on display #" + displayId + ".");
        // 否则新建一个presentation，并存储到mActivePresentations当中
        DemoPresentation presentation = new DemoPresentation(this, display, contents);
        presentation.show();
        // 设置prsentation的解除监听
        presentation.setOnDismissListener(mOnDismissListener);
        mActivePresentations.put(displayId, presentation);
    }

    private final DisplayManager.DisplayListener mDisplayListener =
            new DisplayManager.DisplayListener() {
                @Override
                public void onDisplayAdded(int displayId) {
                    mDisplayListAdapter.updateContents();
                }

                @Override
                public void onDisplayChanged(int displayId) {
                    mDisplayListAdapter.updateContents();
                }

                @Override
                public void onDisplayRemoved(int displayId) {
                    mDisplayListAdapter.updateContents();
                }
            };

    private final DialogInterface.OnDismissListener mOnDismissListener =
            new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    DemoPresentation presentation = (DemoPresentation)dialog;
                    int displayId = presentation.getDisplay().getDisplayId();
                    // 从presentation数组当中将当前presentation删除
                    mActivePresentations.delete(displayId);
                    // 更新listView
                    mDisplayListAdapter.notifyDataSetChanged();
                }
            };


    private void hidePresentation(Display display) {
        final int displayId = display.getDisplayId();
        DemoPresentation presentation = mActivePresentations.get(displayId);
        if (presentation == null) {
            return;
        }

        Log.d(TAG, "Dismissing presentation on display #" + displayId + ".");

        presentation.dismiss();
        mActivePresentations.delete(displayId);
    }

    private void setPresentationDisplayMode(Display display, int displayModeId) {
        final int displayId = display.getDisplayId();
        DemoPresentation presentation = mActivePresentations.get(displayId);
        if (presentation == null) {
            return;
        }

        presentation.setPreferredDisplayMode(displayModeId);
    }

    private int getNextPhoto() {
        final int photo = mNextImageNumber;
        mNextImageNumber = (mNextImageNumber + 1) % PHOTOS.length;
        return photo;
    }

    /**
     * 定义listView的适配器，用于显示所有的屏幕信息
     */
    private final class DisplayListAdapter extends ArrayAdapter<Display>{
        final Context mContext;

        public DisplayListAdapter(Context context) {
            super(context, R.layout.presentation_list_item);
            mContext = context;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            final View v;
            if(convertView == null){
                v = ((Activity)mContext).getLayoutInflater().inflate(
                        R.layout.presentation_list_item, null);
            }else {
                v = convertView;
            }

            final Display display = getItem(position);
            final int displayId = display.getDisplayId();

            DemoPresentation presentation = mActivePresentations.get(displayId);
            DemoPresentationContents contents = presentation != null ?
                    presentation.mContents : null;
            if(contents == null){
                contents = mSavedPresentationContents.get(displayId);
            }

            CheckBox cb = (CheckBox)v.findViewById(R.id.checkbox_presentation);
            cb.setTag(display);
            cb.setOnCheckedChangeListener(PresentationActivity.this);
            cb.setChecked(contents != null);

            TextView tv = (TextView)v.findViewById(R.id.display_id);
            tv.setText(v.getContext().getResources().getString(
                    R.string.presentation_display_id_text, displayId, display.getName()));

            Button b = (Button)v.findViewById(R.id.info);
            b.setTag(display);
            b.setOnClickListener(PresentationActivity.this);

            Spinner s = (Spinner)v.findViewById(modes);
            Display.Mode[] modes = display.getSupportedModes();
            if (contents == null || modes.length == 1) {
                s.setVisibility(View.GONE);
                s.setAdapter(null);
            } else {
                ArrayAdapter<String> modeAdapter = new ArrayAdapter<String>(mContext,
                        android.R.layout.simple_list_item_1);
                s.setVisibility(View.VISIBLE);
                s.setAdapter(modeAdapter);
                s.setTag(display);
                s.setOnItemSelectedListener(PresentationActivity.this);

                modeAdapter.add("<default mode>");

                for (Display.Mode mode : modes) {

                        modeAdapter.add(String.format("Mode %d: %dx%d/%.1ffps",
                                mode.getModeId(),
                                mode.getPhysicalWidth(), mode.getPhysicalHeight(),
                                mode.getRefreshRate()));
                        if (contents.displayModeId == mode.getModeId()) {
                            s.setSelection(modeAdapter.getCount() - 1);
                        }

                }
            }

            return v;
        }

        public void updateContents() {
            clear();

            String displayCategory = getDisplayCategory();
            Display[] displays = mDisplayManager.getDisplays(displayCategory);
            addAll(displays);

            Log.d(TAG, "There are currently " + displays.length + " displays connected.");
            for (Display display : displays) {
                Log.d(TAG, "  " + display);
            }
        }

        private String getDisplayCategory() {
            return showAllDisplays.isChecked() ? null :
                    DisplayManager.DISPLAY_CATEGORY_PRESENTATION;
        }
    }

    /**
     * 创建Presentation的子类，prsentation是一种特殊的对话框，它用来在另外的屏幕上显示内容
     * 需要注意的是在创建它之前就必须要和它的目标屏幕进行绑定，指定目标屏幕的方式有两种一是利用MediaRouter
     * 二是利用DisplayManager，本例演示的是后一种方法
     */
    private final class DemoPresentation extends Presentation{
        final DemoPresentationContents mContents;

        public DemoPresentation(Context outerContext, Display display, DemoPresentationContents contents) {
            super(outerContext, display);
            mContents = contents;
        }

        public void setPreferredDisplayMode(int modeId){
            mContents.displayModeId = modeId;

            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.preferredDisplayModeId = modeId;
            getWindow().setAttributes(params);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            //根据presentation的上下文获取到资源文件
            Resources r = getContext().getResources();
            setContentView(R.layout.presentation_content);

            //获取到与之关联的屏幕
            final Display display = getDisplay();
            final int displayId = display.getDisplayId();
            final int photo = mContents.photo;

            //设置文本显示的内容：描述显示的图片的id，屏幕的id，屏幕的名字等信息
            TextView text = (TextView)findViewById(R.id.text);
            text.setText(r.getString(R.string.presentation_photo_text,
                    photo, displayId, display.getName()));
            ImageView image = (ImageView)findViewById(R.id.image);
            image.setImageDrawable(ContextCompat.getDrawable(PresentationActivity.this, PHOTOS[photo]));

            //GradientDrawable支持使用渐变色来描绘图形，用其为activity设置渐变的背景色
            GradientDrawable drawable = new GradientDrawable();
            //设置图形模式为矩形
            drawable.setShape(GradientDrawable.RECTANGLE);
            //设置渐变的模式为图形渐变
            drawable.setGradientType(GradientDrawable.RADIAL_GRADIENT);
            //获取到屏幕的大小
            Point p = new Point();
            getDisplay().getSize(p);
            //设置圆形渐变的半径为屏幕长和宽的最大值的一半
            drawable.setGradientRadius(Math.max(p.x, p.y) / 2);
            //设置渐变的颜色
            drawable.setColors(mContents.colors);
            //为presentation的主界面添加渐变背景色
            findViewById(android.R.id.content).setBackground(drawable);
        }
    }

    /**
     * 创建类，用于定义在presentation上显示的内容
     * 该类实现了parcelable接口用于实现对象的序列化，
     * 序列化之后就可以方便地在多个activity之间传递复杂的数据
     * 实现parcelable接口必须实现三个方法
     * 1.public int describeContents() 默认返回0即可
     * 2.public void writeToParcel将类中的数据写入到包中（打包的过程）
     * 3.public static final Creator<T> CREATOR=new Creator<T>(){}
     * public static final一个都不能少，方法名CREATOR不能更改
     */
    private final static class DemoPresentationContents implements Parcelable {

        final int photo;
        final int[] colors;
        int displayModeId;

        public DemoPresentationContents(int photo) {
            this.photo = photo;
            colors = new int[]{
                    ((int) (Math.random() * Integer.MAX_VALUE)) | 0xFF000000,
                    ((int) (Math.random() * Integer.MAX_VALUE)) | 0xFF000000
            };
        }

        private DemoPresentationContents(Parcel in){
            photo = in.readInt();
            colors = new int[] { in.readInt(), in.readInt() };
            displayModeId = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(photo);
            dest.writeInt(colors[0]);
            dest.writeInt(colors[1]);
            dest.writeInt(displayModeId);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<DemoPresentationContents> CREATOR = new Creator<DemoPresentationContents>() {
            @Override
            public DemoPresentationContents createFromParcel(Parcel in) {
                return new DemoPresentationContents(in);
            }

            @Override
            public DemoPresentationContents[] newArray(int size) {
                return new DemoPresentationContents[size];
            }
        };
    }
}
