package com.sean.www.camera;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.util.Log;

import com.sean.www.R;
import com.sean.www.activity.CameraActivity;
import com.sean.www.camera.utils.CameraUtils;
import com.sean.www.camera.utils.FileUtils;
import com.tzutalin.dlib.Constants;
import com.tzutalin.dlib.FaceDet;
import com.tzutalin.dlib.VisionDetRet;

import static com.tzutalin.dlibtest.ImageUtils.convertYUV420SPToARGB8888;

public class CameraEngine {
    private static final String TAG = "CameraEngine";
    private static Camera camera = null;
    private static int cameraID = 0;
    private static SurfaceTexture surfaceTexture;
    private static CameraClickener mListener;
    private static final int INPUT_SIZE = 128;
    private static byte[] mBuffer;
    private static Bitmap mRGBBitmap = null;
    private static Bitmap mRotateBitmap = null;
    private static Bitmap mCroppedBitmap = null;
    private static FaceDet mFaceDet;
    private static boolean mUseArSticker;
    private static Paint mFaceLandmardkPaint;
    private static volatile boolean mhasFaceDet;
    private static CameraActivity.MyHandler mHandler = null;
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

    private static Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (data == null || data.length == 0) {
                Log.d(TAG, "date null");
                return;
            }

            // 数据长度不符合YUV420sp格式
            if (data.length != getCameraInfo().previewWidth * getCameraInfo().previewHeight * 1.5) {
                Log.d(TAG, "not equals");
                camera.addCallbackBuffer(mBuffer);
                return;
            }


            //要重新调用，不然只会调用一次
            camera.addCallbackBuffer(mBuffer);
            doFaceDetect(data);
        }
    };

    /**
     * 设置是否启动ar贴纸的标志
     * @param flag boolean
     */
    public void setUseArSticker(boolean flag){
        mUseArSticker = flag;
    }

    /**
     * 人脸识别
     * @param data 预览当前帧的数据
     */
    public static void doFaceDetect(byte[] data){

        int previewWidth = getCameraInfo().previewWidth;
        int previewHeight = getCameraInfo().previewHeight;


        int[] rgbs = new int[previewWidth * previewHeight];

        convertYUV420SPToARGB8888(data, rgbs, previewWidth, previewHeight, false);

        if (null == mRGBBitmap){
            mRGBBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);
        }
        //Log.d(TAG, previewWidth + "," + previewHeight);
        //Log.d(TAG, mRGBBitmap.getRowBytes() * previewHeight + "");
        mRGBBitmap.setPixels(rgbs, 0, previewWidth, 0, 0, previewWidth, previewHeight);

        Matrix mtx = new Matrix();
        mtx.preScale(-1.0f, 1.0f);
        mtx.postRotate(90.0f);
        if (null == mRotateBitmap){
            mRGBBitmap = Bitmap.createBitmap(mRGBBitmap.getWidth(), mRGBBitmap.getHeight(),
                    Bitmap.Config.ARGB_8888);
        }

        if (null == mCroppedBitmap){
            float ratio = (float) mRGBBitmap.getWidth() / mRGBBitmap.getHeight();
            mCroppedBitmap = Bitmap.createBitmap(INPUT_SIZE, (int) (INPUT_SIZE * ratio),
                    Bitmap.Config.ARGB_8888);
        }

        drawResizedBitmap(mRotateBitmap, mCroppedBitmap);

        if (null != mHandler){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (!new File(Constants.getFaceShapeModelPath()).exists()) {
                        FileUtils.copyFileFromRawToOthers(R.raw.shape_predictor_68_face_landmarks, Constants.getFaceShapeModelPath());
                    }

                    List<VisionDetRet> results = null;

                    if (mhasFaceDet){
                        results = mFaceDet.detect(mCroppedBitmap);
                        //Log.d(TAG, results.toString());
                    }


                    // Draw on bitmap
                    if (results != null) {
                        for (final VisionDetRet ret : results) {
                            Log.d(TAG, ret.toString());
                            float resizeRatio = 1.0f;
                            Rect bounds = new Rect();
                            bounds.left = (int) (ret.getLeft() * resizeRatio);
                            bounds.top = (int) (ret.getTop() * resizeRatio);
                            bounds.right = (int) (ret.getRight() * resizeRatio);
                            bounds.bottom = (int) (ret.getBottom() * resizeRatio);
                            Canvas canvas = new Canvas(mRGBBitmap);
                            canvas.drawRect(bounds, mFaceLandmardkPaint);

                            // Draw landmark
                            ArrayList<Point> landmarks = ret.getFaceLandmarks();
                            for (Point point : landmarks) {
                                int pointX = (int) (point.x * resizeRatio);
                                int pointY = (int) (point.y * resizeRatio);
                                canvas.drawCircle(pointX, pointY, 2, mFaceLandmardkPaint);
                            }
                        }
                    }


                    mHandler.sendEmptyMessage(4);
                }
            }).start();
        }
        ///mIsComputing = false;

    }

    public static Camera getCamera(){
        return camera;
    }

    private static void drawResizedBitmap(final Bitmap src, final Bitmap dst) {
        final Matrix matrix = new Matrix();

        final float scaleFactorW = (float) dst.getWidth() / src.getWidth();
        final float scaleFactorH = (float) dst.getHeight() / src.getHeight();
        matrix.postScale(scaleFactorW, scaleFactorH);

        final Canvas canvas = new Canvas(dst);
        canvas.drawBitmap(src, matrix, null);
    }


    /*private static void drawLandMark(VisionDetRet ret) {
        Log.d(TAG, ret.toString());
        float resizeRatio = 1.0f;
        //float resizeRatio = 2.5f;    // 预览尺寸 480x320  /  截取尺寸 192x128  (另外悬浮窗尺寸是 810x540)
        Rect bounds = new Rect();
        bounds.left = (int) (ret.getLeft() * resizeRatio);
        bounds.top = (int) (ret.getTop() * resizeRatio);
        bounds.right = (int) (ret.getRight() * resizeRatio);
        bounds.bottom = (int) (ret.getBottom() * resizeRatio);

        if (previewSize != null) {
            final Bitmap mBitmap = Bitmap.createBitmap(previewSize.getHeight(), previewSize.getWidth(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(mBitmap);
            canvas.drawRect(bounds, mFaceLandmarkPaint);

            final ArrayList<Point> landmarks = ret.getFaceLandmarks();
            for (Point point : landmarks) {
                int pointX = (int) (point.x * resizeRatio);
                int pointY = (int) (point.y * resizeRatio);
                canvas.drawCircle(pointX, pointY, 2, mFaceLandmarkPaint);
            }

            mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    ivDraw.setImageBitmap(mBitmap);
                }
            });
        }
    }*/

    /**
     * 打开相机
     * @return
     */
    public static boolean openCamera(int id){
        if(camera == null){
            try{
                camera = Camera.open(id);
                cameraID = id;
                setDefaultParameters();
                camera.autoFocus(autoFocusCallback);
                if (cameraID == 1){
                    int size = getCameraInfo().previewWidth * getCameraInfo().previewHeight;
                    size = size * ImageFormat.getBitsPerPixel(getParameters().getPreviewFormat()) / 8;
                    mBuffer = new byte[size];
                    camera.addCallbackBuffer(mBuffer);
                    camera.setPreviewCallbackWithBuffer(mPreviewCallback);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            mFaceDet = new FaceDet(Constants.getFaceShapeModelPath());

                            mFaceLandmardkPaint = new Paint();
                            mFaceLandmardkPaint.setColor(Color.WHITE);
                            mFaceLandmardkPaint.setStrokeWidth(2);
                            mFaceLandmardkPaint.setStyle(Paint.Style.STROKE);
                            mhasFaceDet = true;
                        }
                    }).start();
                } else {
                    camera.setPreviewCallbackWithBuffer(null);
                }
                return true;
            }catch(RuntimeException e){
                return false;
            }
        }
        return false;
    }

    public static void releaseCamera(){
        if(null != camera){
            if (null != mFaceDet){
                mhasFaceDet = false;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mFaceDet.release();
                        mFaceDet = null;
                        mRGBBitmap.recycle();
                        mRotateBitmap.recycle();
                        mCroppedBitmap.recycle();
                        mRGBBitmap = null;
                        mRotateBitmap = null;
                        mCroppedBitmap = null;
                    }
                }).start();

            }
            camera.setPreviewCallback(null);
            camera.autoFocus(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }



    public void resumeCamera(){
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
            if (cameraID == 0){
                mListener.startFocus();
            }
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

    public static void setHandler(CameraActivity.MyHandler handler){

        if (null == mHandler){
            mHandler = handler;
        }
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