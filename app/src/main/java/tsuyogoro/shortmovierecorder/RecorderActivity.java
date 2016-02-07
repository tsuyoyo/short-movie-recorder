package tsuyogoro.shortmovierecorder;

import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

// 動画の撮影はここをみる
// http://developer.android.com/intl/ja/guide/topics/media/camera.html#capture-video
public class RecorderActivity extends AppCompatActivity {

    public static final String PARAM_KEY_SAVE_PATH = "param_save_path";

    public static final String PARAM_KEY_RECORDING_LENGTH_SECOND = "param_recording_length";

    public static final String RESULT_SAVED_PATH = "result_saved_path";

    private Camera mCamera;

    private SurfaceView mSurfaceView;

    private MediaRecorder mMediaRecorder;

    private int mRecordingLengthSeconds;

    private String mRecordingFilePath;

    private SurfaceHolder.Callback mSurfaceListener = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mCamera = Camera.open();
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (Exception e) {
                Toast.makeText(RecorderActivity.this,
                        "Failed to startPreview : " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            if (holder.getSurface() == null) {
                return;
            }

            try {
                mCamera.stopPreview();
            } catch (Exception e) {
                // ignore: tried to stop a non-existent preview
            }

            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (Exception e) {

            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            mCamera.release();
            mCamera = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recording_activity);

        FloatingActionButton shutterBtn = (FloatingActionButton) findViewById(R.id.shutter_btn);
        shutterBtn.setOnClickListener(mShutterBtnListener);

        mRecordingLengthSeconds = getIntent().getExtras().getInt(PARAM_KEY_RECORDING_LENGTH_SECOND);
        mRecordingFilePath = getIntent().getExtras().getString(PARAM_KEY_SAVE_PATH);

        mSurfaceView = (SurfaceView) findViewById(R.id.camera_surface);
        SurfaceHolder holder = mSurfaceView.getHolder();
        holder.addCallback(mSurfaceListener);
    }

    private View.OnClickListener mShutterBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            prepareVideoRecorder();
            mMediaRecorder.start();

            AsyncTask<Integer, Integer, Void> timerTask = new AsyncTask<Integer, Integer, Void>() {

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    findViewById(R.id.shutter_btn).setVisibility(View.INVISIBLE);
                    findViewById(R.id.recording_timer_text).setVisibility(View.VISIBLE);
                }

                @Override
                protected Void doInBackground(Integer... params) {
                    int countDown = params[0] + 1;

                    while (0 < countDown) {
                        publishProgress(countDown--);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Log.e("CameraRecord", "Interrupted");
                            return null;
                        }
                    }

                    return null;
                }

                @Override
                protected void onProgressUpdate(Integer... values) {
                    super.onProgressUpdate(values);
                    ((TextView) findViewById(R.id.recording_timer_text)).setText(String.valueOf(values[0]));
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);

                    mMediaRecorder.stop();
                    releaseMediaRecorder();
                    mCamera.lock();

                    Intent result = new Intent();
                    result.putExtra(RESULT_SAVED_PATH, mRecordingFilePath);
                    setResult(RESULT_OK, result);
                    finish();
                }
            };

            timerTask.execute(mRecordingLengthSeconds);
        }
    };

    private boolean prepareVideoRecorder() {
        mMediaRecorder = new MediaRecorder();

        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        CamcorderProfile camcorderProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_1080P);
        mMediaRecorder.setProfile(camcorderProfile);

        mMediaRecorder.setOutputFile(mRecordingFilePath);

        mMediaRecorder.setPreviewDisplay(mSurfaceView.getHolder().getSurface());

        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            releaseMediaRecorder();
            return false;
        }

        return true;
    }

    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }

}
