package com.ads.clientconnection;

import com.ads.clientconnection.entity.ImageAndVideoEntity;

import java.util.ArrayList;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * @author Ly
 */
public class MyViewModel extends ViewModel {
    private MutableLiveData<ArrayList<ImageAndVideoEntity.FileEntity>> selected = new MutableLiveData<>();

    public void select(ArrayList<ImageAndVideoEntity.FileEntity> item) {
        selected.setValue(item);
    }
    public LiveData<ArrayList<ImageAndVideoEntity.FileEntity>> getSelected(){
        return selected;
    }
}
