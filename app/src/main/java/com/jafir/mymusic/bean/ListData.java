package com.jafir.mymusic.bean;

import android.content.Context;

import com.jafir.mymusic.Config;

import org.kymjs.kjframe.KJDB;

import java.util.List;

/**
 * Created by jafir on 15/12/14.
 */
public class ListData {
    // 本地列表
    private static List<Music> localList;

    private static void refresh(Context context) {
        KJDB db = KJDB.create(context, Config.DB_NAME, Config.isDebug);
        if (localList == null || Config.changeMusicInfo) {
            localList = db.findAll(Music.class);
            Config.changeMusicInfo = false;
        }
    }

    public static List<Music> getLocalList(Context context) {
        refresh(context);
        return localList;
    }

}