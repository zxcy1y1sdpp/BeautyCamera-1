package com.sean.www.camera;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.view.SurfaceView;

import com.sean.www.camera.utils.CameraUtils;

public class CameraEngine {
    private static Camera camera = null;
    private static int cameraID = 0;
    private static SurfaceTexture surfaceTexture;
    private static SurfaceView surfaceView;
    private static CameraClickener mListener;
    private static Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if (success){
                if (mListener!=null){
                    mListener.startFocus();
                }
            } else {
                mListener.stopFocus();
            }
        }
    };

    public static Camera getCamera(){
        return camera;
    }


    /**
     * 打开相机
     * @return
     */
    public static boolean openCamera(){
        if(camera == null){
            try{
                camera = Camera.open(cameraID);
                camera.autoFocus(autoFocusCallback);
                setDefaultParameters();
                return true;
            }catch(RuntimeException e){
                return false;
            }
        }
        return false;
    }

    public static boolean openCamera(int id){
        if(camera == null){
            try{
                camera = Camera.open(id);
                cameraID = id;
                setDefaultParameters();

                return true;
            }catch(RuntimeException e){
                return false;
            }
        }
        return false;
    }

    public static void releaseCamera(){
        if(camera != null){
            camera.setPreviewCallback(null);
            camera.autoFocus(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }



    public void resumeCamera(){
        openCamera();
    }

    public static void setParameters(Parameters parameters){
        camera.setParameters(parameters);
    }

    public static Parameters getParameters(){
        if(camera != null)
            return camera.getParameters();
        return null;
    }

    public static void switchCamera(){
        releaseCamera();
        cameraID = cameraID == 0 ? 1 : 0;
        openCamera(cameraID);
        startPreview(surfaceTexture);
    }

    private static void setDefaultParameters(){
        Parameters parameters = camera.getParameters();
        if (parameters.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

        //获取到相机最大的预览Size
        Size previewSize = CameraUtils.getLargePreviewSize(camera);
        parameters.setPreviewSize(previewSize.width, previewSize.height);
        //获取相机最大的图片Size
        Size pictureSize = CameraUtils.getLargePictureSize(camera);
        parameters.setPictureSize(pictureSize.width, pictureSize.height);
        parameters.setRotation(90);
        camera.setParameters(parameters);
    }

    private static Size getPreviewSize(){
        return camera.getParameters().getPreviewSize();
    }

    private static Size getPictureSize(){
        return camera.getParameters().getPictureSize();
    }

    public static void startPreview(SurfaceTexture surfaceTexture){
        if(camera != null)
            try {
                camera.setPreviewTexture(surfaceTexture);
                CameraEngine.surfaceTexture = surfaceTexture;
                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public static void startPreview(){
        if(camera != null)
            camera.startPreview();
        if (mListener!=null){
            mListener.startFocus();
        }

    }

    public static void stopPreview(){
        camera.stopPreview();
        if (mListener!=null){
            mListener.stopFocus();
        }
    }

    public static void setRotation(int rotation){
        Camera.Parameters params = camera.getParameters();
        params.setRotation(rotation);
        camera.setParameters(params);
    }

    public static void takePicture(Camera.ShutterCallback shutterCallback, Camera.PictureCallback rawCallback,
                                   Camera.PictureCallback jpegCallback){
        camera.takePicture(shutterCallback, rawCallback, jpegCallback);
    }

    public static boolean setFocusAndMeteringArea(List<CameraEngine.Area> focusAreas, List<CameraEngine.Area> meterAreas) {
        camera.cancelAutoFocus();
        List<Camera.Area> camera_areas = new ArrayList<Camera.Area>();
        List<Camera.Area> meter_areas = new ArrayList<Camera.Area>();
        for(CameraEngine.Area area : focusAreas) {
            camera_areas.add(new Camera.Area(area.rect, area.weight));
        }
        for(CameraEngine.Area area : meterAreas) {
            meter_areas.add(new Camera.Area(area.rect, area.weight));
        }
        Camera.Parameters parameters = getParameters();
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
            parameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
            setParameters(parameters);
            camera.autoFocus(new Camera.AutoFocusCallback() {
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

            setParameters(parameters);
        }
        return false;
    }

    /**
     * 获取相机的配置信息
     * @return 相机配置信息
     */
    public static com.sean.www.camera.utils.CameraInfo getCameraInfo(){
        com.sean.www.camera.utils.CameraInfo info = new com.sean.www.camera.utils.CameraInfo();
        Size size = getPreviewSize();
        CameraInfo cameraInfo = new CameraInfo();
        Camera.getCameraInfo(cameraID, cameraInfo);
        info.previewWidth = size.width;
        info.previewHeight = size.height;
        info.orientation = cameraInfo.orientation;
        info.isFront = cameraID == 1 ? true : false;
        size = getPictureSize();
        info.pictureWidth = size.width;
        info.pictureHeight = size.height;
        return info;
    }

    public static void setCameraClistener(CameraClickener listener){
        mListener = listener;
    }

    public interface CameraClickener{
        void startFocus();
        void stopFocus();
    }

    public static class Area {
        public Rect rect = null;
        public int weight = 0;

        public Area(Rect rect, int weight) {
            this.rect = rect;
            this.weight = weight;
        }
    }
}