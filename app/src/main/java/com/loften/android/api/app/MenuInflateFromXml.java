package com.loften.android.api.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loften.android.api.R;

import java.lang.reflect.Method;

public class MenuInflateFromXml extends AppCompatActivity {

    private static final int sMenuExampleResources[] = {
            R.menu.title_only, R.menu.title_icon
    };

    private static final String sMenuExampleNames[] = {
      "title_only", "title_icon"
    };

    private Spinner mSpinner;

    private TextView mInstructionsText;

    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, sMenuExampleNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner = new Spinner(this);

        mSpinner.setAdapter(adapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                invalidateOptionsMenu();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        layout.addView(mSpinner,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));

        mInstructionsText = new TextView(this);
        mInstructionsText.setText(R.string.menu_from_xml_instructions_press_menu);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(10,10,10,10);
        layout.addView(mInstructionsText, lp);

        setContentView(layout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        setIconsVisible(menu, true);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(sMenuExampleResources[mSpinner.getSelectedItemPosition()], menu);

        mInstructionsText.setText(R.string.menu_from_xml_instructions_go_back);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Toast.makeText(this, "one click", Toast.LENGTH_SHORT).show();
        return true;
    }

    /**
     *在Android4.0系统中，创建菜单Menu，通过setIcon方法给菜单添加图标是无效的，图标没有显出来，
     * 2.3系统中是可以显示出来的。这个问题的根本原因在于4.0系统中，涉及到菜单的源码类 MenuBuilder做了改变
     */
    private void setIconsVisible(Menu menu, boolean flag) {
        //判断menu是否为空
        if(menu != null) {
            try {
                //如果不为空,就反射拿到menu的setOptionalIconsVisible方法
                Method method = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                //暴力访问该方法
                method.setAccessible(true);
                //调用该方法显示icon
                method.invoke(menu, flag);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
