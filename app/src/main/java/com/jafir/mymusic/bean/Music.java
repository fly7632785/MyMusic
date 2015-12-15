package com.jafir.mymusic.bean;

import org.kymjs.kjframe.database.annotate.Id;
import org.kymjs.kjframe.database.annotate.Table;

import java.io.Serializable;

/**
 * Created by jafir on 15/12/14.
 */
@Table(name = "music")
public class Music implements Serializable{

    // 设置自定义主键
    @Id(column = "id")
    private int id;
    private String title;
    private String artist;
    private String path;
    private String size;


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }


    @Override
    public String toString() {
        return "Music{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", path='" + path + '\'' +
                ", size='" + size + '\'' +
                '}';
    }
}
