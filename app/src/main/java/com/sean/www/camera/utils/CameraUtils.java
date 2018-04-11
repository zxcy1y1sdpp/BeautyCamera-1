package com.sean.www.camera.utils;

import android.hardware.Camera;

import com.sean.www.camera.CameraEngine;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by machenshuang on 2017/2/25.
 */
public class CameraUtils {

    public static Camera.Size getLargePictureSize(Camera camera){
        if(camera != null){
            List<Camera.Size> sizes = camera.getParameters().getSupportedPictureSizes();
            Camera.Size temp = sizes.get(0);
            for(int i = 1;i < sizes.size();i ++){
                float scale = (float)(sizes.get(i).height) / sizes.get(i).width;
                if(temp.width < sizes.get(i).width && scale < 0.6f && scale > 0.5f)
                    temp = sizes.get(i);
            }
            return temp;
        }
        return null;
    }

    /**
     * 获取预览时候的Size
     * @param camera
     * @return
     */
    public static Camera.Size getLargePreviewSize(Camera camera){
        if(camera != null){
            List<Camera.Size> sizes = camera.getParameters().getSupportedPreviewSizes();
            Camera.Size temp = sizes.get(0);
            for(int i = 1;i < sizes.size();i ++){
                if(temp.width < sizes.get(i).width)
                    temp = sizes.get(i);
            }
            return temp;
        }
        return null;
    }

    public static boolean setFocusAndMeteringArea(List<CameraEngine.Area> focusAreas, List<CameraEngine.Area> meterAreas) {
        CameraEngine.getCamera().cancelAutoFocus();
        List<Camera.Area> camera_areas = new ArrayList<Camera.Area>();
        List<Camera.Area> meter_areas = new ArrayList<Camera.Area>();
        for(CameraEngine.Area area : focusAreas) {
            camera_areas.add(new Camera.Area(area.rect, area.weight));
        }
        for(CameraEngine.Area area : meterAreas) {
            meter_areas.add(new Camera.Area(area.rect, area.weight));
        }
        Camera.Parameters parameters = CameraEngine.getParameters();
        String focus_mode = parameters.getFocusMode();
        // getFocusMode() is documented as never returning null, however I've had null pointer exceptions reported in Google Play
        if( parameters.getMaxNumFocusAreas() != 0
                && focus_mode != null
                && ( focus_mode.equals(Camera.Parameters.FOCUS_MODE_AUTO)
                || focus_mode.equals(Camera.Parameters.FOCUS_MODE_MACRO)
                || focus_mode.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)
                || focus_mode.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO) ) ) {

            parameters.setFocusAreas(camera_areas);
            // also set metering areas
            parameters.setMeteringAreas(meter_areas);
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            CameraEngine.setParameters(parameters);
            CameraEngine.getCamera().autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    Camera.Parameters params = camera.getParameters();
                    //params.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                    camera.setParameters(params);
                }
            });
            return true;
        }
        else if( parameters.getMaxNumMeteringAreas() != 0 ) {
            parameters.setMeteringAreas(meter_areas);

            CameraEngine.setParameters(parameters);
        }
        return false;
    }
}
