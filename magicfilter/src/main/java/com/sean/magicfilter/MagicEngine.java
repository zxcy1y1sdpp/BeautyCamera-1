package com.sean.magicfilter;

import android.util.Log;

import com.sean.magicfilter.camera.CameraEngine;
import com.sean.magicfilter.filter.helper.MagicFilterType;
import com.sean.magicfilter.utils.MagicParams;
import com.sean.magicfilter.helper.SavePictureTask;
import com.sean.magicfilter.widget.MagicCameraView;
import com.sean.magicfilter.widget.base.MagicBaseView;

import java.io.File;
import java.util.logging.Logger;

/**
 * Created by machenshuang on 2017/2/25.
 */

/**
 * 美颜相机管理类，主要保存图片和记录，设置美颜等级
 */
public class MagicEngine {

    private static final String TAG = "MagicEngine";
    private static MagicEngine magicEngine;

    public static MagicEngine getInstance(){
        if(magicEngine == null)
            throw new NullPointerException("MagicEngine must be built first");
        else
            return magicEngine;
    }

    private MagicEngine(Builder builder){

    }

    public void setFilter(MagicFilterType type){
        MagicParams.magicBaseView.setFilter(type);
    }

    public void savePicture(File file, SavePictureTask.OnPictureSaveListener listener){
        if (file!=null){
            Log.d(TAG,"file is not null");
        } else {
            Log.d(TAG,"file is null");
        }
        SavePictureTask savePictureTask = new SavePictureTask(file, listener);
        MagicParams.magicBaseView.savePicture(savePictureTask);
    }

    public void startRecord(){
        if(MagicParams.magicBaseView instanceof MagicCameraView)
            ((MagicCameraView)MagicParams.magicBaseView).changeRecordingState(true);
    }

    public void stopRecord(){
        if(MagicParams.magicBaseView instanceof MagicCameraView)
            ((MagicCameraView)MagicParams.magicBaseView).changeRecordingState(false);
    }

    public void setBeautyLevel(int level){
        if(MagicParams.magicBaseView instanceof MagicCameraView && MagicParams.beautyLevel != level) {
            MagicParams.beautyLevel = level;
            ((MagicCameraView) MagicParams.magicBaseView).onBeautyLevelChanged();
        }
    }

    public void switchCamera(){
        CameraEngine.switchCamera();
    }

    public static class Builder{

        public MagicEngine build(MagicBaseView magicBaseView) {
            MagicParams.context = magicBaseView.getContext();
            MagicParams.magicBaseView = magicBaseView;
            return new MagicEngine(this);
        }

        public Builder setVideoPath(String path){
            MagicParams.videoPath = path;
            return this;
        }

        public Builder setVideoName(String name){
            MagicParams.videoName = name;
            return this;
        }

    }
}
