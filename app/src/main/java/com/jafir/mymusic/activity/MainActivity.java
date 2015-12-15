package com.jafir.mymusic.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.jafir.mymusic.Config;
import com.jafir.mymusic.PlayManager;
import com.jafir.mymusic.R;
import com.jafir.mymusic.adapter.MusicAdapter;
import com.jafir.mymusic.bean.Music;
import com.jafir.mymusic.service.PlayService;
import com.jafir.mymusic.service.ScanService;

import org.kymjs.kjframe.KJDB;
import org.kymjs.kjframe.ui.ViewInject;
import org.kymjs.kjframe.utils.KJLoger;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    /**
     * 底部播放暂停等按钮
     **/
    private Button mBtnNext, mBtnPrevious, mBtnPlay;
    /**
     * 底部头像图片
     **/
    private ImageView mImg;
    /**
     * 音乐列表
     **/
    private ArrayList<Music> mMusicList;
    private ListView mListview;
    private MusicAdapter mAdapter;
    /**
     * 进度条
     **/
    private ProgressBar mProgress;

    private SeekBar mSeekbar;
    /**
     * 播放模式
     **/
    private ImageView mMode;
    /**
     * 模式的index
     **/
    private int mModeIndex = Config.MODE_SEQUENCE;
    /**
     * 歌名 歌手
     **/
    private TextView mTvTitle, mTvArtist;
    /**
     * 音乐播放器服务
     */
    private PlayService mPlayersService;
    /**
     * 音乐播放管理器
     **/
    private PlayManager mPlayManager;
    /**
     * hanlder循环处理更新 进度条
     **/
    private ProgressHandle mHander = new ProgressHandle();
    /**
     * 进度条更新 线程
     **/
    private ProgressThread mThead = new ProgressThread();
    /**
     * 一共多少歌
     **/
    private int mMusicCount;


    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Config.RECEIVER_UPDATE_MUSIC_LIST:
                    mMusicCount = intent.getIntExtra(Config.SCAN_MUSIC_COUNT, 0);
                    KJDB db = KJDB.create(context, Config.DB_NAME, Config.isDebug);
                    mMusicList = (ArrayList<Music>) db.findAll(Music.class);
                    ViewInject.toast(MainActivity.this, "您有" + mMusicCount + "首歌");
                    KJLoger.debug("list:" + mMusicList.toString());
                    if (mMusicList != null) {
                        //填充adapter
                        mAdapter.updateDate(mMusicList);
                    } else {
                        ViewInject.toast(MainActivity.this, "您本地没有音乐");
                    }
                    break;

                case Config.RECEIVER_MUSIC_SCAN_FAIL:
                    ViewInject.toast(MainActivity.this, "扫描出现了问题。。。");
                    break;

                case Config.RECEIVER_MUSIC_CHANGE:
                    refreshBottomBar();
                    break;

            }
        }
    };


    class ProgressThread implements Runnable {
        @Override
        public void run() {
            if (mPlayManager.getPlaying() == Config.PLAYING_PLAY) {
                Message msg = Message.obtain();
                msg.arg1 = mPlayManager.getDuration();
                msg.arg2 = mPlayManager.getCurrentPosition(); // 进度
                mHander.sendMessage(msg);
            }
        }
    }

    @SuppressLint("HandlerLeak")
    class ProgressHandle extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
//            mProgress.setMax(msg.arg1);
//            mProgress.setProgress(msg.arg2);
            mSeekbar.setMax(msg.arg1);
            mSeekbar.setProgress(msg.arg2);
            mHander.post(mThead);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        /** 开启服务 **/
        Intent serviceIntent = new Intent(this, PlayService.class);
        this.bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
        /** 注册广播 **/
        registerMyReceiver();
        /** 初始化底部菜单 **/
        initBottonBar();
        initAdapter();
        initListView();

        /** 获得播放管理器 **/
        mPlayManager = PlayManager.getInstance(getApplicationContext());
        /** 设置播放模式   **/
        mPlayManager.setMode(mModeIndex);

    }

    private void initAdapter() {
        KJDB db = KJDB.create(this, Config.DB_NAME, Config.isDebug);
        mMusicList = (ArrayList<Music>) db.findAll(Music.class);
        if (mMusicList != null) {
            mAdapter = new MusicAdapter(this, mMusicList);
        }
    }

    private void initListView() {
        mListview = (ListView) findViewById(R.id.main_list);
        mListview.setAdapter(mAdapter);
        mListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mHander.post(mThead);
                mPlayersService.play(mMusicList, position);
                refreshBottomBar();
            }
        });
    }

    /**
     * 动态注册广播
     */
    private void registerMyReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Config.RECEIVER_UPDATE_MUSIC_LIST);
        filter.addAction(Config.RECEIVER_MUSIC_SCAN_FAIL);
        filter.addAction(Config.RECEIVER_MUSIC_CHANGE);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshBottomBar();
    }


    /**
     * 初始化底部栏
     */
    private void initBottonBar() {
        findViewById(R.id.bottom_bar).setOnClickListener(this);
        mImg = (ImageView) findViewById(R.id.bottom_img_image);
        mImg.setImageResource(R.mipmap.img_noplaying);

        mTvTitle = (TextView) findViewById(R.id.bottom_tv_title);
        mTvArtist = (TextView) findViewById(R.id.bottom_tv_artist);
        mTvTitle.setText(Config.TITLE);
        mTvArtist.setText(Config.ARTIST);

        mMode = (ImageView) findViewById(R.id.bottom_btn_mode);
        mProgress = (ProgressBar) findViewById(R.id.progress);
        mSeekbar = (SeekBar) findViewById(R.id.seekbar);
        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mPlayManager.seekTo(seekBar.getProgress());
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mPlayManager.seekTo(seekBar.getProgress());

            }
        });

        mBtnNext = (Button) findViewById(R.id.bottom_btn_next);
        mBtnPrevious = (Button) findViewById(R.id.bottom_btn_previous);
        mBtnPlay = (Button) findViewById(R.id.bottom_btn_play);
        mMode.setOnClickListener(this);
        mBtnNext.setOnClickListener(this);
        mBtnPrevious.setOnClickListener(this);
        mBtnPlay.setOnClickListener(this);
    }


    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            ViewInject.toast(MainActivity.this, "呀，音乐播放失败，退出再进试试");
            mPlayersService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mPlayersService = ((PlayService.LocalPlayer) service)
                    .getService();
        }
    };

    @Override
    public void onClick(View v) {

        if (mMusicList == null) {
            ViewInject.toast(MainActivity.this, "请先扫描一下音乐");
            return;
        }
        switch (v.getId()) {
            case R.id.bottom_btn_next:
                mPlayersService.next();
                refreshBottomBar();
                break;
            case R.id.bottom_btn_previous:
                mPlayersService.previous();
                refreshBottomBar();
                break;
            case R.id.bottom_btn_play:
                if (mPlayManager.getPlaying() == Config.PLAYING_PLAY) {
                    mPlayersService.pause();
                } else if (mPlayManager.getPlaying() == Config.PLAYING_PAUSE) {
                    mPlayersService.replay();
                } else {
                    mPlayersService.play();
                }
                refreshBottomBar();
                break;

            case R.id.bottom_btn_mode:
                if (mModeIndex == 3) {
                    mModeIndex = 0;
                }
                mModeIndex++;
                mPlayManager.setMode(mModeIndex);
                refreshBottomBar();
                break;


        }
    }


    /**
     * 刷新底部栏
     */
    private void refreshBottomBar() {
        switch (mPlayManager.getPlaying()) {
            case Config.PLAYING_PAUSE:
                mImg.setImageResource(R.mipmap.img_noplaying);
                mBtnPlay.setBackgroundResource(R.drawable.selector_btn_play);
                mTvTitle.setText(mPlayManager.getMusic().getTitle());
                mTvArtist.setText(mPlayManager.getMusic().getArtist());
                break;
            case Config.PLAYING_PLAY:
                mHander.post(mThead);
                mImg.setImageResource(R.mipmap.img_playing);
                mBtnPlay.setBackgroundResource(R.drawable.selector_btn_pause);
                mTvTitle.setText(mPlayManager.getMusic().getTitle());
                mTvArtist.setText(mPlayManager.getMusic().getArtist());
                break;
            case Config.PLAYING_STOP:
                mImg.setImageResource(R.mipmap.img_noplaying);
                mBtnPlay.setBackgroundResource(R.drawable.selector_btn_play);
                mTvTitle.setText(Config.TITLE);
                mTvArtist.setText(Config.ARTIST);
                break;
        }

        switch (mPlayManager.getMode()) {
            case Config.MODE_RANDOM:
                mMode.setImageResource(R.drawable.bt_playing_mode_shuffle);
                break;
            case Config.MODE_SEQUENCE:
                mMode.setImageResource(R.drawable.bt_playing_mode_order);
                break;
            case Config.MODE_REPEAT_SINGLE:
                mMode.setImageResource(R.drawable.bt_playing_mode_cycle);
                break;
            case Config.MODE_REPEAT_ALL:
                mMode.setImageResource(R.drawable.bt_playing_mode_singlecycle);
                break;
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_scan) {
            /**扫描音乐*/
            startService(new Intent(this, ScanService.class));
            ViewInject.toast(this, "正在后台扫描音乐");

            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        this.unbindService(connection);
    }


}
