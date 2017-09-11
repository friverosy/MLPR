package com.app.axxezo.mpr;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;

import org.opencv.android.JavaCameraView;

/**
 * Created by tstaig on 9/10/17.
 */

public class MPRCameraView extends JavaCameraView {
    boolean focusing = false;
    public MPRCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public void enableAutoFocus() {
        if (focusing)
            return;
        mCamera.cancelAutoFocus();
        Camera.Parameters params = mCamera.getParameters();
        //params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        params.setExposureCompensation(params.getMaxExposureCompensation());
        //params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
        mCamera.setParameters(params);
        focusing = true;
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                focusing = false;
            }
        });
    }
    public boolean isFocusing() {
        return focusing;
    }
}