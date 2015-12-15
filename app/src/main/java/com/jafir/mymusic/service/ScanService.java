package com.jafir.mymusic.service;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.provider.MediaStore;

import com.jafir.mymusic.Config;
import com.jafir.mymusic.bean.Music;

import org.kymjs.kjframe.KJDB;
import org.kymjs.kjframe.utils.KJLoger;
import org.kymjs.kjframe.utils.StringUtils;

/**
 * Created by jafir on 15/12/14.
 */
public class ScanService extends IntentService {


    private int count;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public ScanService() {
        super("com.jafir.mymusic.service.scan");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        scan();

    }

    private void scan() {

        boolean result = false;
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,null,null,null,null
        );

        if(cursor == null){
            result = false;
        }else {
            KJDB db = KJDB.create(this, Config.DB_NAME, Config.isDebug);
            db.deleteByWhere(Music.class, null);
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
                    .moveToNext()) {
                // String id = cursor.getString(cursor
                // .getColumnIndex(MediaStore.Audio.Media._ID));
                String title = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Audio.Media.TITLE));
                String artist = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String path = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Audio.Media.DATA));
                String size = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Audio.Media.SIZE));
                if (StringUtils.toInt(size, 0) > 700 * 1024) {
                    Music music = new Music();
                    music.setTitle(title);
                    music.setArtist(artist);
                    music.setPath(path);
                    music.setSize(size);
                    db.save(music);
                    count++;
                    KJLoger.debug("找到音乐：" + music.getTitle());
                }
            }
            result = true;
            cursor.close();
        }

        if(result){
            Config.changeMusicInfo = true;
        }

        // 发送扫描成功或失败的广播
        Intent musicScan = new Intent();
        String action = result ? Config.RECEIVER_UPDATE_MUSIC_LIST
                : Config.RECEIVER_MUSIC_SCAN_FAIL;
        musicScan.setAction(action);
        musicScan.putExtra(Config.SCAN_MUSIC_COUNT, count);
        sendBroadcast(musicScan);

    }


}
