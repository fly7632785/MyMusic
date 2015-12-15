package com.jafir.mymusic;

/**
 * Created by jafir on 15/12/14.
 */
public class Config {

    /** 数据库名字 */
    public static final String DB_NAME = "jafir_music_db";
    /** 是否启动调试模式 */
    public static final boolean isDebug = true;



    /** 播放器状态 */
    public static final int PLAYING_STOP = 0;
    public static final int PLAYING_PAUSE = 1;
    public static final int PLAYING_PLAY = 2;


    /** 音乐改变的广播 */
    public static final String RECEIVER_MUSIC_CHANGE = "con.jafir.music.music_change";
    /** 歌曲扫描完成广播 */
    public static final String RECEIVER_UPDATE_MUSIC_LIST = "com.jafir.music.music_scan_success";
    public static final String RECEIVER_MUSIC_SCAN_FAIL = "con.jafir.music.music_scan_fail";
    public static final String SCAN_MUSIC_COUNT = "scan_count";
    public static final String  TITLE = "Jafir's Music";
    public static final String  ARTIST = "Jafir";
    public static boolean changeMusicInfo = false;

    /** 播放列表循环模式 */
    public static final int MODE_REPEAT_SINGLE = 0;
    public static final int MODE_REPEAT_ALL = 1;
    public static final int MODE_SEQUENCE = 2;
    public static final int MODE_RANDOM = 3;
}
