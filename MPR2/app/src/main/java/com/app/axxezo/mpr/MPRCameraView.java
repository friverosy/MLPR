package com.app.axxezo.mpr;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import org.opencv.android.JavaCameraView;

import java.util.ArrayList;
import java.util.List;

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
    public void enableOnTouchFocus(final MainActivity act) {
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                EditText ed = (EditText) act.findViewById(R.id.editText);
                if (focusing)
                    return false;
                mCamera.cancelAutoFocus();
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    float x = event.getX();
                    float y = event.getY();
                    ed.setText("Display: " + event.getX() + "," + event.getY());
                    Rect touchRect = new Rect((int) (x - 100), (int) (y - 100), (int) (x + 100), (int) (y + 100));
                    final Rect targetFocusRect = new Rect(touchRect.left * 2000 / v.getWidth() - 1000, touchRect.top * 2000 / v.getHeight() - 1000, touchRect.right * 2000 / v.getWidth() - 1000, touchRect.bottom * 2000 / v.getHeight() - 1000);
                    List<Camera.Area> focusList = new ArrayList<Camera.Area>();
                    Camera.Area focusArea = new Camera.Area(targetFocusRect, 1000);
                    Camera.Parameters params = mCamera.getParameters();
                    params.setFocusAreas(focusList);
                    params.setMeteringAreas(focusList);
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                    mCamera.setParameters(params);
                    focusing = true;
                    mCamera.autoFocus(new Camera.AutoFocusCallback() {
                        @Override
                        public void onAutoFocus(boolean success, Camera camera) {
                            EditText ed = (EditText) act.findViewById(R.id.editText);
                            if (success)
                                ed.setText("Display: " + "Focus finished");
                            else
                                ed.setText("Display: " + "Focus failed");
                            focusing = false;
                        }
                    });
                }
                return false;
            }
        });
    }
}