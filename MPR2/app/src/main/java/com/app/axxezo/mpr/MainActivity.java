package com.app.axxezo.mpr;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static org.opencv.imgproc.Imgproc.MORPH_RECT;

/**
 * Created by nmartin on 10-01-17.
 */

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private MPRCameraView mOpenCvCameraView;

    Mat mRgba;
    Bitmap bmp;
    Boolean isOn = false;
    TessOCR mTessOCR;
    public static final int CV_32F=5;
    private static final String TAG ="OpenCV" ;

    private static final int TEMPLATE_IMAGE = R.drawable.patente;
    Mat templ;
    Rect temprec;
    Mat result;
    Mat img;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    Mat bgr = null;
                    try {
                        bgr = Utils.loadResource(getApplicationContext(), TEMPLATE_IMAGE, Imgcodecs.CV_LOAD_IMAGE_COLOR);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // convert the image to rgba
                    templ = new Mat();
                    Imgproc.cvtColor(bgr, templ, Imgproc.COLOR_BGR2GRAY);

                    temprec = new Rect(0, 0, templ.width(), templ.height());

                    // init the result matrix
                    result = new Mat();
                    img = new Mat();


                    mOpenCvCameraView.enableView();

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                loadList();
            }});
        t.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "called onCreate");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        mOpenCvCameraView = (MPRCameraView) findViewById(R.id.opcv_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.disableFpsMeter();
        mOpenCvCameraView.setCvCameraViewListener(this);
        ListView lv = (ListView) findViewById(R.id.list_data);
        final Button button = (Button) findViewById(R.id.button);
        final Button button2 = (Button) findViewById(R.id.button2);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(isOn == false){
                    isOn = true;
                }else{
                    isOn=false;
                }
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DatabaseHelper db = new DatabaseHelper(getApplicationContext());
                EditText ed = (EditText) findViewById(R.id.editText);
                String currentDateandTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

                if(ed.getText().toString()!=""){

                    Log.d("ADDBD",ed.getText().toString()+" "+currentDateandTime);
                    db.add_platedata(new PlateData(ed.getText().toString(),currentDateandTime));
                }

                db.close();
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        loadList();
                    }});

                t.start();

                //Send Json Http Post
                JSONObject jsend = new JSONObject();
                try {
                    ADataPost apost = new ADataPost();
                    jsend.put("pnumber",ed.getText().toString());
                    jsend.put("pdate",currentDateandTime);
                    Log.d("ADDDB_JSON1",jsend.toString());
                    apost.execute(jsend);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> arg0, final View arg1, final int pos, long id) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(MainActivity.this, R.style.AlertDialogNew));
                final TextView lv = (TextView) arg1.findViewById(R.id.t_list_plate_id);

                // set title
                alertDialogBuilder.setTitle("Delete");

                // set dialog message
                alertDialogBuilder
                        .setMessage("Delete data?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String Plate_id =lv.getText().toString();
                                DatabaseHelper db = new DatabaseHelper(getApplicationContext());
                                db.delete_platedata(Integer.parseInt(Plate_id));//Delete
                                Thread t = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        loadList();
                                    }});

                                t.start();
                                db.close();

                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // if this button is clicked, just close
                                // the dialog box and do nothing
                                dialog.cancel();
                            }
                        });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();

                return true;
            }
        });
        mTessOCR = new TessOCR(getApplicationContext());
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    /**
     * This method is invoked when camera preview has started. After this method is invoked
     * the frames will start to be delivered to client via the onCameraFrame() callback.
     *
     * @Param width  -  the width of the frames that will be delivered
     * @Param height - the height of the frames that will be delivered
     */
    @Override
    public void onCameraViewStarted(int width, int height) {
        mOpenCvCameraView.enableAutoFocus();
        //
        //
    }

    /**
     * This method is invoked when camera preview has been stopped for some reason.
     * No frames will be delivered via onCameraFrame() callback after this method is called.
     */
    @Override
    public void onCameraViewStopped() {


    }

    /**
     * This method is invoked when delivery of the frame needs to be done.
     * The returned values - is a modified frame which needs to be displayed on the screen.
     *
     * @Param inputFrame
     */
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        System.gc();
        img = inputFrame.gray();
        Mat img_color = inputFrame.rgba();
        // Source image to display
        Mat img_display = new Mat();
        img.copyTo( img_display );

        Imgproc.rectangle(img_color, new Point((img_color.width()/2)-70,img_color.height()/2-25), new Point((img_color.width()/2)+70,img_color.height()/2+25), new Scalar(0,255,0), 6);

        if(isOn && !mOpenCvCameraView.isFocusing()){
            try {
                Mat croppedPart;
                Rect out = new Rect(new Point(img_display.width()/2-70,img_display.height()/2-25), new Point(img_display.width()/2+70,img_display.height()/2+25));
                croppedPart = img.submat(out);
                PlateProcessing pp = new PlateProcessing();
                Mat pre_img = pp.getLicensePlate(croppedPart, null, null, 0);
                //Mat pre_img = pp.getLicensePlate(img, null, null, 0);

                /*Image Post-processing*/

                Imgproc.adaptiveThreshold(croppedPart, croppedPart, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 15, 5);
                Imgproc.erode(croppedPart, croppedPart, Imgproc.getStructuringElement(MORPH_RECT, new Size(2, 2)));
                Imgproc.dilate(croppedPart, croppedPart, Imgproc.getStructuringElement(MORPH_RECT, new Size(2, 2)));

                /*End Image Post-processing*/

                bmp = Bitmap.createBitmap(pre_img.width(), pre_img.height(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(pre_img, bmp);
                bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight());

            } catch (Exception e) {
                Log.d(TAG, "cropped part data error " + e.getMessage());
            }
            if (bmp != null && isOn==true) {
                doOCR(bmp);
            }
            isOn=false;
        }
        if (!isOn)
            mOpenCvCameraView.enableAutoFocus();
        return img_color;

    }

    private void doOCR(final Bitmap bitmap) {
        final String text = mTessOCR.getOCRResult(bitmap);

        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);

        final String result = normalized.replaceAll("[^A-Z0-9]", "");//If not alphanumeric, then garbage.....
        runOnUiThread(new Runnable() {
            public void run() {
                ImageView iv = (ImageView) findViewById(R.id.imageView2);
                iv.setImageBitmap(bitmap);
                EditText ed = (EditText) findViewById(R.id.editText);
                ed.setText(result);
            }
        });
    }

    class ADataPost extends AsyncTask<JSONObject,Void,Boolean> {

        @Override
        protected Boolean doInBackground(JSONObject... JObj) {
            try {

                Http http = new Http();
                String out = JObj[0].toString()+"";
                Log.d("ADDDB_HTTP",out+"");
                String resp = http.Post("http://192.168.2.106:8888", out.toString()+"", "text/plain");

            } catch (Exception e) {
                e.printStackTrace();
                return false;

            }
            return true;
        }


    }

    public void loadList(){
        Log.d("ADDDB_load", "fffff"+"");
        DatabaseHelper db = new DatabaseHelper(getApplication());
        final ListView lv = (ListView) findViewById(R.id.list_data);
        int count = db.platedata_count();
        final ArrayList<ListPlateItem> data = db.get_platedata_all();
        runOnUiThread(new Runnable() {
            public void run() {
                lv.setAdapter(new ItemPlateAdapter(getBaseContext(), data));
            }
        });
    }

}
