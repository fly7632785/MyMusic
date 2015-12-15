package com.jafir.mymusic.service;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.TelephonyManager;

import com.jafir.mymusic.Config;
import com.jafir.mymusic.PlayManager;
import com.jafir.mymusic.bean.ListData;
import com.jafir.mymusic.bean.Music;

import org.kymjs.kjframe.ui.ViewInject;
import org.kymjs.kjframe.utils.KJLoger;

import java.util.List;

/**
 * Created by jafir on 15/12/14.
 */
public class PlayService extends Service {


    private  PlayManager mPlayer ;
    private final LocalPlayer localPlayer = new LocalPlayer();
    private SystemReceiver mReceiver;

    /**
     * 用于和本地的绑定activity式的服务调用
     */
    public class LocalPlayer extends Binder {
        public PlayService getService() {
            return PlayService.this;
        }
    }




    @Override
    public IBinder onBind(Intent intent) {
        IntentFilter intentFilter = new IntentFilter();
        mReceiver = new SystemReceiver();
        intentFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
        intentFilter.addAction("android.intent.action.PHONE_STATE");
        registerReceiver(mReceiver, intentFilter);
        return localPlayer;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mPlayer = PlayManager.getInstance(getApplicationContext());
        startForeground(1, new Notification());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPlayer.destroy();
        unregisterReceiver(mReceiver);
        stopForeground(true);
    }

    /**
     * 播放
     *
     * @param list
     * @param position
     * @return
     */
    public Music play(List<Music> list, int position) {
        Music music = null;
        if (list == null || list.isEmpty()) {
            play();
        } else {
            music = mPlayer.play(this, list, position);
        }
        return music;
    }

    /**
     * 默认播放
     *
     * @return
     */
    public Music play() {
        Music music = null;
        if (ListData.getLocalList(this) == null
                || ListData.getLocalList(this).isEmpty()) {
            ViewInject.toast("亲，还没有歌呢，去扫描一下吧");
        } else {
            music = mPlayer.play(this, ListData.getLocalList(this), 0);
        }
        return music;
    }

    /**
     * 暂停
     */
    public void pause() {
        mPlayer.pause();
    }

    /**
     * 正在暂停，调用后开始继续播放
     */
    public void replay() {
        mPlayer.replay();
    }

    /**
     * 下一首
     *
     * @return
     */
    public Music next() {
        Music music = null;
        if (mPlayer.getPlaying() == Config.PLAYING_STOP
                || mPlayer.getList().isEmpty()) {
            play();
        } else {
            music = mPlayer.next(this);
            KJLoger.debug("正在播放：" + music.getTitle());
        }
        return music;
    }

    /**
     * 上一首
     *
     * @return
     */
    public Music previous() {
        Music music = null;
        if (mPlayer.getPlaying() == Config.PLAYING_STOP
                || mPlayer.getList().isEmpty()) {
            play();
        } else {
            music = mPlayer.previous(this);
            KJLoger.debug("正在播放：" + music.getTitle());
        }
        return music;
    }

    /**
     * 打电话时暂停播放
     *
     * @author kymjs
     *
     */
    public class SystemReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 如果是打电话
            if (Intent.ACTION_NEW_OUTGOING_CALL.equals(intent.getAction())) {
                pause();
            } else {
                // 如果是来电
                TelephonyManager tm = (TelephonyManager) context
                        .getSystemService(Service.TELEPHONY_SERVICE);
                switch (tm.getCallState()) {
                    // 响铃
                    case TelephonyManager.CALL_STATE_RINGING:
                        pause();
                        break;
                    // 摘机
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        pause();
                        break;
                    // 空闲
                    case TelephonyManager.CALL_STATE_IDLE:
                        replay();
                        break;
                }
            }
        }
    }

}
