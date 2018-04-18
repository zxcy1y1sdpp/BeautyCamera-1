package com.sean.www.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.sean.www.camera.CameraEngine;
import com.sean.www.R;
import com.sean.www.adapter.FilterAdapter;
import com.sean.www.MagicEngine;
import com.sean.magicfilter.filter.helper.MagicFilterType;
import com.sean.magicfilter.utils.MagicParams;
import com.sean.www.camera.utils.CameraUtils;
import com.sean.www.widget.MagicCameraView;
import com.sean.www.helper.FocusHelper;
import com.sean.www.utils.TapAreaUtil;
import com.sean.www.view.FocusOverlay;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static com.sean.www.helper.FocusHelper.FOCUS_WAITING;

/**
 * Created by machenshuang on 2017/3/17.
 */
public class CameraActivity extends Activity{
    private LinearLayout mFilterLayout;
    private RecyclerView mFilterListView;
    private FilterAdapter mAdapter;
    private MagicEngine magicEngine;
    private boolean isRecording = false;
    private final int MODE_PIC = 1;
    private final int MODE_VIDEO = 2;
    private int mode = MODE_PIC;
    private FocusOverlay mFocuOverlayView;
    private FocusHelper mFocusHelper;
    private FrameLayout mCameraFlayout;
    private Matrix cameraToPreviewMatrix = new Matrix();
    private Matrix previewToCameraMatrix = new Matrix();
    private int mPreWidth;
    private int mPreHeight;


    private CameraEngine.CameraClickener mListener = new CameraEngine.CameraClickener() {
        @Override
        public void startFocus() {
            mFocusHelper.setHasFocusArea(false);
            mFocusHelper.setAutoFocus(true);
            mFocusHelper.setFocusComplete(FOCUS_WAITING,-1);
        }

        @Override
        public void stopFocus() {
            mFocusHelper.setAutoFocus(false);

            mFocuOverlayView.invalidate();
        }
    } ;

    private ImageView btn_shutter;
    private ImageView btn_mode;

    private ObjectAnimator animator;

    private final MagicFilterType[] types = new MagicFilterType[]{
            MagicFilterType.NONE,
            MagicFilterType.FAIRYTALE,
            MagicFilterType.SUNRISE,
            MagicFilterType.SUNSET,
            MagicFilterType.WHITECAT,
            MagicFilterType.BLACKCAT,
            MagicFilterType.SKINWHITEN,
            MagicFilterType.HEALTHY,
            MagicFilterType.SWEETS,
            MagicFilterType.ROMANCE,
            MagicFilterType.SAKURA,
            MagicFilterType.WARM,
            MagicFilterType.ANTIQUE,
            MagicFilterType.NOSTALGIA,
            MagicFilterType.CALM,
            MagicFilterType.LATTE,
            MagicFilterType.TENDER,
            MagicFilterType.COOL,
            MagicFilterType.EMERALD,
            MagicFilterType.EVERGREEN,
            MagicFilterType.CRAYON,
            MagicFilterType.SKETCH,
            MagicFilterType.AMARO,
            MagicFilterType.BRANNAN,
            MagicFilterType.BROOKLYN,
            MagicFilterType.EARLYBIRD,
            MagicFilterType.FREUD,
            MagicFilterType.HEFE,
            MagicFilterType.HUDSON,
            MagicFilterType.INKWELL,
            MagicFilterType.KEVIN,
            MagicFilterType.LOMO,
            MagicFilterType.N1977,
            MagicFilterType.NASHVILLE,
            MagicFilterType.PIXAR,
            MagicFilterType.RISE,
            MagicFilterType.SIERRA,
            MagicFilterType.SUTRO,
            MagicFilterType.TOASTER2,
            MagicFilterType.VALENCIA,
            MagicFilterType.WALDEN,
            MagicFilterType.XPROII
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        MagicEngine.Builder builder = new MagicEngine.Builder();
        magicEngine = builder
                .build((MagicCameraView)findViewById(R.id.glsurfaceview_camera));
        initView();
        CameraEngine.setCameraClistener(mListener);

    }

    private void initView(){
        mFilterLayout = (LinearLayout)findViewById(R.id.layout_filter);
        mFilterListView = (RecyclerView) findViewById(R.id.filter_listView);
        mCameraFlayout = findViewById(R.id.fl_camera);

        btn_shutter = (ImageView)findViewById(R.id.btn_camera_shutter);
        //btn_mode = (ImageView)findViewById(R.id.btn_camera_mode);

        findViewById(R.id.btn_camera_filter).setOnClickListener(btn_listener);
        findViewById(R.id.btn_camera_closefilter).setOnClickListener(btn_listener);
        findViewById(R.id.btn_camera_shutter).setOnClickListener(btn_listener);

        findViewById(R.id.btn_camera_beauty).setOnClickListener(btn_listener);

        //获取相机预览大小
        Point screenSize = new Point();
        getWindowManager().getDefaultDisplay().getSize(screenSize);

        mFocuOverlayView = new FocusOverlay(this);
        mCameraFlayout.addView(mFocuOverlayView);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mFocuOverlayView.getLayoutParams();
        params.width = screenSize.x;
        params.height = screenSize.x * 4 / 3;
        mFocuOverlayView.setLayoutParams(params);
        mPreWidth = params.width;
        mPreHeight = params.height;

        LinearLayout ll = (LinearLayout) ViewGroup.inflate(this,R.layout.camera_mode_switch,null);
        mCameraFlayout.addView(ll);

        ll.findViewById(R.id.btn_camera_switch).setOnClickListener(btn_listener);
        //ll.findViewById(R.id.btn_camera_mode).setOnClickListener(btn_listener);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mFilterListView.setLayoutManager(linearLayoutManager);

        mAdapter = new FilterAdapter(this, types);
        mFilterListView.setAdapter(mAdapter);
        mAdapter.setOnFilterChangeListener(onFilterChangeListener);


        animator = ObjectAnimator.ofFloat(btn_shutter,"rotation",0,360);
        animator.setDuration(500);
        animator.setRepeatCount(ValueAnimator.INFINITE);

        mFocusHelper = new FocusHelper(this,mFocuOverlayView);
    }

    private FilterAdapter.onFilterChangeListener onFilterChangeListener = new FilterAdapter.onFilterChangeListener(){

        @Override
        public void onFilterChanged(MagicFilterType filterType) {
            magicEngine.setFilter(filterType);
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (grantResults.length != 1 || grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if(mode == MODE_PIC)
                takePhoto();
            else
                takeVideo();
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private View.OnClickListener btn_listener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                /*case R.id.btn_camera_mode:
                    switchMode();
                    break;*/
                case R.id.btn_camera_shutter:
                    if (PermissionChecker.checkSelfPermission(CameraActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_DENIED) {
                        ActivityCompat.requestPermissions(CameraActivity.this, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
                                v.getId());
                    } else {
                        if(mode == MODE_PIC)
                            takePhoto();
                        else
                            takeVideo();
                    }
                    break;
                case R.id.btn_camera_filter:
                    showFilters();
                    break;
                case R.id.btn_camera_switch:
                    magicEngine.switchCamera();
                    break;
                case R.id.btn_camera_beauty:
                    new AlertDialog.Builder(CameraActivity.this)
                            .setSingleChoiceItems(new String[] { "关闭", "1", "2", "3", "4", "5"}, MagicParams.beautyLevel,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        magicEngine.setBeautyLevel(which);
                                        dialog.dismiss();
                                    }
                                })
                            .setNegativeButton("取消", null)
                            .show();
                    break;
                case R.id.btn_camera_closefilter:
                    hideFilters();
                    break;
            }
        }
    };

    private void switchMode(){
        if(mode == MODE_PIC){
            mode = MODE_VIDEO;
            btn_mode.setImageResource(R.drawable.icon_camera);
        }else{
            mode = MODE_PIC;
            btn_mode.setImageResource(R.drawable.icon_video);
        }
    }

    private void takePhoto(){
        magicEngine.savePicture(getOutputMediaFile(),null);
    }

    private void takeVideo(){
        if(isRecording) {
            animator.end();
            magicEngine.stopRecord();
        }else {
            animator.start();
            magicEngine.startRecord();
        }
        isRecording = !isRecording;
    }

    private void showFilters(){
        ObjectAnimator animator = ObjectAnimator.ofFloat(mFilterLayout, "translationY", mFilterLayout.getHeight(), 0);
        animator.setDuration(200);
        animator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                findViewById(R.id.btn_camera_shutter).setClickable(false);
                mFilterLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }
        });
        animator.start();
    }

    private void hideFilters(){
        ObjectAnimator animator = ObjectAnimator.ofFloat(mFilterLayout, "translationY", 0 ,  mFilterLayout.getHeight());
        animator.setDuration(200);
        animator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // TODO Auto-generated method stub
                mFilterLayout.setVisibility(View.INVISIBLE);
                findViewById(R.id.btn_camera_shutter).setClickable(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                // TODO Auto-generated method stub
                mFilterLayout.setVisibility(View.INVISIBLE);
                findViewById(R.id.btn_camera_shutter).setClickable(true);
            }
        });
        animator.start();
    }


    public File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MagicCamera");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINESE).format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mListener = null;
    }

    public void draw(Canvas canvas, Paint paint){
        Rect rect = canvas.getClipBounds();
        mFocusHelper.draw(canvas, paint, rect, true);
    }

    public boolean touchEvent(MotionEvent event){
        int x = (int) event.getX();
        int y = (int) event.getY();
        mFocusHelper.setAutoFocus(true);
        mFocusHelper.setHasFocusArea(false);
        ArrayList<CameraEngine.Area> focusAreas = getAreas(x, y, 1.0f);
        ArrayList<CameraEngine.Area> meterAreas = getAreas(x, y, 1.5f);
        if( CameraUtils.setFocusAndMeteringArea(focusAreas, meterAreas) ) {
            mFocusHelper.setFocusScreen(x, y);
            mFocusHelper.setHasFocusArea(true);
            mFocusHelper.setFocusComplete(mFocusHelper.FOCUS_WAITING,-1);
            mFocuOverlayView.invalidate();
        }
        return true;
    }


    private ArrayList<CameraEngine.Area> getAreas(float x, float y, float areaMultiple) {
        calculateCameraToPreviewMatrix();

        Rect rect = TapAreaUtil.calculateTapArea((int)x, (int)y, areaMultiple, mPreWidth,
                mPreHeight, previewToCameraMatrix);

        ArrayList<CameraEngine.Area> areas = new ArrayList<CameraEngine.Area>();
        areas.add(new CameraEngine.Area(rect, 1));
        return areas;
    }

    private void calculateCameraToPreviewMatrix() {
        if (CameraEngine.getCamera() == null){
            return;
        }
        cameraToPreviewMatrix.reset();
        // from http://developer.android.com/reference/android/hardware/Camera.Face.html#rect
        // Need mirror for front camera
        boolean mirror = CameraEngine.getCameraInfo().isFront;
        cameraToPreviewMatrix.setScale(mirror ? -1 : 1, 1);
        // This is the value for android.hardware.Camera.setDisplayOrientation.
        cameraToPreviewMatrix.postRotate(CameraEngine.getCameraInfo().orientation);
        // Camera driver coordinates range from (-1000, -1000) to (1000, 1000).
        // UI coordinates range from (0, 0) to (width, height).
        int width = mPreWidth;
        int height = mPreHeight;
        cameraToPreviewMatrix.postScale(width / 2000f, height / 2000f);
        cameraToPreviewMatrix.postTranslate(width / 2f, height / 2f);

        if(!cameraToPreviewMatrix.invert(previewToCameraMatrix) ) {

        }
    }
}
