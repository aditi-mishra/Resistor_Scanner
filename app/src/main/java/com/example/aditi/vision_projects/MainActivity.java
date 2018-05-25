package com.example.aditi.vision_projects;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import static org.opencv.core.CvType.CV_8UC1;

public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private ResistorImageProcessor _resistorProcessor;
    private ResistorCameraView _resistorCameraView;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
    private static final String TAG = "MainActivity";
    private CameraBridgeViewBase cameraBridgeViewBase;
    static {
        if(!OpenCVLoader.initDebug()){
            Log.d(TAG, "OpenCV not loaded");
        } else {
            Log.d(TAG, "OpenCV loaded");
        }
    }
    private BaseLoaderCallback _loaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    _resistorCameraView.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    private BaseLoaderCallback mLoaderCallback	=	new
            BaseLoaderCallback(this)	{
                @Override
                //This	is	the	callback	method	called	once	the	OpenCV	//manager
                 //       is	connected
                public	void	onManagerConnected(int	status)	{
                    switch	(status)	{
                        //Once	the	OpenCV	manager	is	successfully	connected	we	can	enable	the
                      //  camera	interaction	with	the	defined	OpenCV	camera	view
                        case	LoaderCallbackInterface.SUCCESS:
                        {
                            Log.i(TAG,	"OpenCV	loaded	successfully");
                            cameraBridgeViewBase.enableView();
                        }	break;
                        default:
                        {
                            super.onManagerConnected(status);
                        }	break;
                    }
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        _resistorCameraView = (ResistorCameraView) findViewById(R.id.HelloVisionView);
        _resistorCameraView.setVisibility(SurfaceView.VISIBLE);
        _resistorCameraView.setZoomControl((SeekBar) findViewById(R.id.CameraZoomControls));
        _resistorCameraView.setCvCameraViewListener(this);



        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

//        cameraBridgeViewBase	=	(CameraBridgeViewBase)
//                findViewById(R.id.HelloVisionView);
//        //Set	the	view	as	visible
//        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
//        //Register	your	activity	as	the	callback	object	to	handle	//camera
//       // frames
//        cameraBridgeViewBase.setCvCameraViewListener(this);
        _resistorProcessor = new ResistorImageProcessor();
        SharedPreferences settings = getPreferences(0);
        if(!settings.getBoolean("shownInstructions", false))
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.dialog_message)
                    .setTitle(R.string.dialog_title)
                    .setNeutralButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("shownInstructions", true);
            editor.apply();
        }

    }


    @Override
    public	void	onResume(){
        super.onResume();
//Call	the	async	initialization	and	pass	the	callback	object	we
//created	later,	and	chose	which	version	of	OpenCV	library	to	//load.
    //    Just	make	sure	that	the	OpenCV	manager	you	installed	//supports	the
     //   version	you	are	trying	to	load.
        _loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
//        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_10,	this,
//                mLoaderCallback);
    }
    @Override
    public void onPause()
    {
        super.onPause();
        if (_resistorCameraView != null)
            _resistorCameraView.disableView();
    }

    public void onDestroy() {
        super.onDestroy();
        if (_resistorCameraView != null)
            _resistorCameraView.disableView();
    }
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat	cameraFram=inputFrame.rgba();
        double	[]	pixelValue=cameraFram.get(0,	0);
        double	redChannelValue=pixelValue[0];
        double	greenChannelValue=pixelValue[1];
        double	blueChannelValue=pixelValue[2];
        Log.i(TAG,	"red	channel	value:	"+redChannelValue);
        Log.i(TAG,	"green	channel	value:	"+greenChannelValue);
        Log.i(TAG,	"blue	channel	value:	"+blueChannelValue);
      //  return	inputFrame.rgba();
        return _resistorProcessor.processFrame(inputFrame);
    }
}
