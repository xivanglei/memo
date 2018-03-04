package com.example.mymemo;

import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mymemo.util.ActivityCollector;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private DrawerLayout mDrawerLayout;

    RecyclerView memo;

    private List<ContentItem> memoList = new ArrayList<ContentItem>();

    private List<ContentItem> layUpList = new ArrayList<ContentItem>();

    private MemoAdapter adapter, layUpAdapter;

    private ActionServiceReceiver receiver;

    public static final String TAG = "MainActivity";

    private Button drawerButton, addButton;

    private EditText search;

    private List<ContentItem> tempMemoList, tempLayUpList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this, RemindService.class);
        startService(intent);
        MyDatabase.MEMOSQL = new MyDatabase(this, "MyMemo", null, 5);
        MyDatabase.DB = MyDatabase.MEMOSQL.getWritableDatabase();
        initData();
        initView();
        receiver = new ActionServiceReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.mymemo.remind");
        registerReceiver(receiver, intentFilter);
        receiver.setMessage(new ActionServiceReceiver.Message() {
            @Override
            public void send() {
                initData();
                adapter.notifyDataSetChanged();
                layUpAdapter.notifyDataSetChanged();
            }
        });
    }
    public void initView() {
        drawerButton = (Button) findViewById(R.id.drawer_button);
        addButton = (Button) findViewById(R.id.add_button);
        drawerButton.setOnClickListener(this);
        addButton.setOnClickListener(this);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        memo = (RecyclerView) findViewById(R.id.memo_list);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager
                (2, StaggeredGridLayoutManager.VERTICAL);
         memo.setLayoutManager(layoutManager);
        adapter = new MemoAdapter(memoList, this, R.layout.memo_item);
        adapter.setOnSaveListener(new MemoAdapter.OnSaveListener() {
            @Override
            public void onClick() {
                initData();
                layUpAdapter.notifyDataSetChanged();
            }
        });
        memo.setAdapter(adapter);
        RecyclerView layUp = (RecyclerView) findViewById(R.id.lay_up_list);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this);
        layUp.setLayoutManager(layoutManager2);
        layUpAdapter = new MemoAdapter(layUpList, this, R.layout.lay_up_item);
        layUp.setAdapter(layUpAdapter);
        search = (EditText) findViewById(R.id.search);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String key = search.getText().toString();
                if(StringUtils.isBlank(key)) {
                    memoList.clear();
                    layUpList.clear();
                    for(ContentItem item : tempMemoList) {
                        memoList.add(item);
                    }
                    for(ContentItem item : tempLayUpList) {
                        layUpList.add(item);
                    }
                } else {
                    memoList.clear();
                    layUpList.clear();
                    for(ContentItem item : tempMemoList) {
                        if(item.getContent().contains(key)) {
                            memoList.add(item);
                        }
                    }
                    for(ContentItem item : tempLayUpList) {
                        if(item.getContent().contains(key)) {
                            layUpList.add(item);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
                layUpAdapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void initData() {
        Cursor cursor = MyDatabase.DB.rawQuery("select * from Memo", null);
        memoList.clear();
        layUpList.clear();
        while(cursor.moveToNext()) {
            int isLayUp = cursor.getInt(cursor.getColumnIndex("is_lay_up"));
            String date = cursor.getString(cursor.getColumnIndex("date"));
            String content = cursor.getString(cursor.getColumnIndex("content"));
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            long remindDate = cursor.getLong(cursor.getColumnIndex("remind_date"));
            int isRemind = cursor.getInt(cursor.getColumnIndex("isRemind"));
            if(isLayUp != 1) {
                memoList.add(new ContentItem(date, content, id, remindDate, isRemind));
            } else {
                layUpList.add(new ContentItem(date, content, id, remindDate, isRemind, isLayUp));
            }
        }
        cursor.close();
        tempMemoList = new ArrayList<ContentItem>();
        for(ContentItem item: memoList) {
            tempMemoList.add(item);
        }
        tempLayUpList = new ArrayList<ContentItem>();
        for(ContentItem item: layUpList) {
            tempLayUpList.add(item);
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.drawer_button:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.add_button:
                Intent intent = new Intent(this, EditActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
        adapter.notifyDataSetChanged();
        layUpAdapter.notifyDataSetChanged();
    }

    /*
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
    }
    */
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        unregisterReceiver(receiver);
    }
}
