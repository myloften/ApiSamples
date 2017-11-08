package com.loften.android.api.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.loften.android.api.R;

import java.util.ArrayList;
import java.util.List;

public class AlertDialogSamples extends AppCompatActivity implements View.OnClickListener {


    private Button simpleDialog;
    private Button simpleListDialog;
    private Button singleChoiceDialog;
    private Button multiChoiceDialog;
    private Button customAdapterDialog;
    private Button customViewDialog;
    private Button customThemeDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_dialog_samples);
        initView();
    }

    private void initView() {

        simpleDialog = (Button) findViewById(R.id.simple_dialog);
        simpleDialog.setOnClickListener(this);
        simpleListDialog = (Button) findViewById(R.id.simple_list_dialog);
        simpleListDialog.setOnClickListener(this);
        singleChoiceDialog = (Button) findViewById(R.id.single_choice_dialog);
        singleChoiceDialog.setOnClickListener(this);
        multiChoiceDialog = (Button) findViewById(R.id.multi_choice_dialog);
        multiChoiceDialog.setOnClickListener(this);
        customAdapterDialog = (Button) findViewById(R.id.custom_adapter_dialog);
        customAdapterDialog.setOnClickListener(this);
        customViewDialog = (Button) findViewById(R.id.custom_view_dialog);
        customViewDialog.setOnClickListener(this);
        customThemeDialog = (Button) findViewById(R.id.custom_theme_dialog);
        customThemeDialog.setOnClickListener(this);
    }

    private List<String> getList(){
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            list.add("item:"+i);
        }
        return list;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.simple_dialog:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.simple_dialog)
                        .setMessage(R.string.dialog_message)
                        .setNegativeButton(R.string.negative_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .setPositiveButton(R.string.postive_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        }).show();
                break;
            case R.id.simple_list_dialog:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.simple_list_dialog)
                        .setItems(R.array.select_dialog_items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String[] items = getResources().getStringArray(R.array.select_dialog_items);
                                new AlertDialog.Builder(AlertDialogSamples.this)
                                        .setMessage(items[i])
                                        .show();
                            }
                        }).show();
                break;
            case R.id.single_choice_dialog:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.single_choice_dialog)
                        .setSingleChoiceItems(R.array.select_dialog_items2, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .setPositiveButton(R.string.postive_button, null)
                        .setNegativeButton(R.string.negative_button, null)
                        .show();
                break;
            case R.id.multi_choice_dialog:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.multi_choice_dialog)
                        .setMultiChoiceItems(R.array.select_dialog_items3,
                                new boolean[]{false, true, false, true, false, false, false},
                                new DialogInterface.OnMultiChoiceClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {

                                    }
                                })
                        .setPositiveButton(R.string.postive_button, null)
                        .setNegativeButton(R.string.negative_button, null)
                        .show();
                break;
            case R.id.custom_adapter_dialog:
                CustomAdapter adapter = new CustomAdapter(this, getList());
                new AlertDialog.Builder(this)
                        .setTitle(R.string.custom_adapter_dialog)
                        .setAdapter(adapter, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(getApplicationContext(), ""+i, Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setCancelable(true)
                        .show();
                break;
            case R.id.custom_view_dialog:
                View view = getLayoutInflater().inflate(R.layout.activity_alert_dialog_samples, null);
                new AlertDialog.Builder(this)
                        .setTitle(R.string.custom_view_dialog)
                        .setView(view)
                        .setCancelable(true)
                        .show();
                break;
            case R.id.custom_theme_dialog:
                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.dialog)
                        .setTitle(R.string.custom_theme_dialog)
                        .setMessage(R.string.dialog_message)
                        .setNegativeButton(R.string.negative_button, null)
                        .setPositiveButton(R.string.postive_button, null);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                break;
        }
    }

    class CustomAdapter extends BaseAdapter{

        private List<String> items;
        private LayoutInflater inflater;

        public CustomAdapter(Context context, List<String> items){
            this.items = items;
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int i) {
            return items.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder holder;
            if(view == null){
                holder = new ViewHolder();
                view = inflater.inflate(R.layout.quick_contacts, null);
                holder.text = (TextView)view.findViewById(R.id.name);
                view.setTag(holder);
            }else {
                holder = (ViewHolder) view.getTag();
            }
            holder.text.setText(items.get(i));
            return view;
        }

        class ViewHolder{
            TextView text;
        }

    }
}
