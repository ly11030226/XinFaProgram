package com.ads.clientconnection.view.ImagePicker.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.ads.clientconnection.view.ImagePicker.model.AlbumModel;
import com.ads.clientconnection.view.ImagePicker.view.AlbumItem;

import java.util.ArrayList;


/**
 * Created by fengyongge on 2016/5/24
 */
public class AlbumAdapter extends MBaseAdapter<AlbumModel> {
	public AlbumAdapter(Context context, ArrayList<AlbumModel> models) {
		super(context, models);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		AlbumItem albumItem = null;
		if (convertView == null) {
			albumItem = new AlbumItem(context);
			convertView = albumItem;
		} else
			albumItem = (AlbumItem) convertView;
		albumItem.update(models.get(position));
		return convertView;
	}

}
