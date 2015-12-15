package com.jafir.mymusic;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;

import com.jafir.mymusic.bean.ListData;
import com.jafir.mymusic.bean.Music;

import org.kymjs.kjframe.ui.ViewInject;

import java.util.List;

/**
 * Created by jafir on 15/12/14.
 */
public class PlayManager {

    private static PlayManager mInstance=null;
    private int mode;
    private int playing = Config.PLAYING_STOP;

    private List<Music> list;
    private int position = 0;
    private Context context;
    private MediaPlayer mMediaPlayer;



    private PlayManager(Context context) {
        list = ListData.getLocalList(context);
        mMediaPlayer = new MediaPlayer();

    }


    /**
     * 单例获得 播放管理器
     * @param context
     * @return
     */
    public static PlayManager getInstance(Context context) {
        if(mInstance == null)
        {
            mInstance = new PlayManager(context);
        }
        return mInstance;
    }

    /**
     * 设置播放模式
     * @param mode
     */
    public void setMode(int mode) {
        this.mode = mode;
    }

    /**
     * 获取现在的 播放模式
     * @return
     */
    public int getMode() {
        return this.mode;
    }

    /**
     * 获取播放的状态
     * @return
     */
    public int getPlaying() {
        return playing;
    }

    /**
     * 获取总时间
     * @return
     */
    public int getDuration() {
        int durat = 0;
        if (mMediaPlayer != null) {
            durat = mMediaPlayer.getDuration();
        }
        return durat;
    }


    /**
     * 获取当前的位置
     * @return
     */
    public int getCurrentPosition(){
        int currentPosition = 0;
        if (mMediaPlayer != null) {
            currentPosition = mMediaPlayer.getCurrentPosition();
        }
        return currentPosition;
    }


    /**
     * 获取 播放列表
     * @return
     */
    public List<Music> getList() {
        return this.list;
    }


    /**
     * 获取当前播放的音乐
     * @return
     */
    public Music getMusic() {
        Music music = null;
        if (position >= list.size()) {
            music = new Music();
            music.setArtist(Config.ARTIST);
            music.setTitle(Config.TITLE);
        } else {
            music = list.get(position);
        }
        return music;
    }



    // 将音乐播放跳转到某一时间点,以毫秒为单位
    public void seekTo(int msec) {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(msec);
        }
    }

    public void destroy() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            playing = Config.PLAYING_STOP;
        }
    }


    public void stop() {
        if (playing != Config.PLAYING_STOP) {
            mMediaPlayer.reset();
            playing = Config.PLAYING_STOP;
            context.sendBroadcast(new Intent(Config.RECEIVER_MUSIC_CHANGE));
        }
    }

    public void pause() {
        if (playing != Config.PLAYING_PAUSE) {
            mMediaPlayer.pause();
            playing = Config.PLAYING_PAUSE;
            context.sendBroadcast(new Intent(Config.RECEIVER_MUSIC_CHANGE));
        }
    }

    // 正在暂停，即将开始继续播放
    public Music replay() {
        if (playing != Config.PLAYING_PLAY) {
            mMediaPlayer.start();
            playing = Config.PLAYING_PLAY;
            context.sendBroadcast(new Intent(Config.RECEIVER_MUSIC_CHANGE));
        }
        return list.get(position);
    }

    public Music play(Context context, List<Music> list, int position) {
        // 如果有正在播放的歌曲，将它停止
        if (playing == Config.PLAYING_PLAY) {
            mMediaPlayer.reset();
        }
        mMediaPlayer = MediaPlayer.create(context,
                Uri.parse("file://" + list.get(position).getPath()));
        try {
            mMediaPlayer.start();
            this.list = list;
            this.position = position;
            this.context = context;
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    PlayManager.this.context.sendBroadcast(new Intent(
                            Config.RECEIVER_MUSIC_CHANGE));
                    completion(PlayManager.this.context, PlayManager.this.list,
                            PlayManager.this.position);
                }
            });
            playing = Config.PLAYING_PLAY;
            context.sendBroadcast(new Intent(Config.RECEIVER_MUSIC_CHANGE));
        } catch (NullPointerException e) {
            ViewInject.toast("亲，找不到歌曲了，存储卡拔掉了吗？");
        }
        return list.get(position);
    }

    public Music next(Context context) {
        Music music = null;
        if (list.size() < 1) {
            this.destroy();
        }else if(mode == Config.MODE_RANDOM){
            mMediaPlayer.reset(); // 停止上一首
            position = (int)(Math.random() * list.size());
        } else {
            mMediaPlayer.reset(); // 停止上一首
            position = (position + 1) % list.size();
        }
        play(context, list, position);
        music = list.get(position);
        return music;
    }

    public Music previous(Context context) {
        Music music = null;
        if (list.size() < 1) {
            this.destroy();
            music = null;
        } else {
            mMediaPlayer.reset(); // 停止上一首
            position = (position + list.size() - 1) % list.size();
            play(context, list, position);
            music = list.get(position);
        }
        return music;
    }

    /**
     * 当一首歌播放完了之后 采取的播放策略   单曲 循环 还是停止。。
     * @param context
     * @param list
     * @param position
     * @return
     */
    public Music completion(Context context, List<Music> list, int position) {
        Music music = null;
        switch (mode) {
            case Config.MODE_REPEAT_SINGLE:
                // 单曲播放
                stop();
                break;
            case Config.MODE_REPEAT_ALL:
                // 单曲循环
                music = play(context, list, position);
                break;
            case Config.MODE_SEQUENCE:
                // 列表循环
                music = play(context, list, (position + 1) % list.size());
                break;
            case Config.MODE_RANDOM:
                // 随机循环
                music = play(context, list, (int) (Math.random() * list.size()));
                break;
            default:
                break;
        }
        return music;
    }


}
