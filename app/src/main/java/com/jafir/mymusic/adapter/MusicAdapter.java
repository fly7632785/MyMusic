package com.jafir.mymusic.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jafir.mymusic.R;
import com.jafir.mymusic.bean.Music;

import java.util.ArrayList;

/**
 * Created by jafir on 15/12/14.
 */
public class MusicAdapter extends BaseAdapter {


    private Context context;
    private ArrayList<Music> mData;

    public MusicAdapter(Context context, ArrayList<Music> mData) {
        this.context = context;
        this.mData = mData;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void updateDate(ArrayList<Music> mMusicList) {
        if(mMusicList != null){
            mData.clear();
            mData.addAll(mMusicList);
            notifyDataSetChanged();
        }

    }

    static class ViewHolder {
        TextView tv_title;
        TextView tv_artist;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = View
                    .inflate(context, R.layout.list_item_music, null);
            holder = new ViewHolder();
            holder.tv_title = (TextView) convertView
                    .findViewById(R.id.list_item_title);
            holder.tv_artist = (TextView) convertView
                    .findViewById(R.id.list_item_artist);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tv_title.setText(mData.get(position).getTitle());
        holder.tv_artist.setText(mData.get(position).getArtist());
        return convertView;
    }


}
