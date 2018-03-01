package com.example.mymemo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Toast;

import com.example.mymemo.view.PlayVideo;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class RecordVideo extends AppCompatActivity implements View.OnClickListener {

    private Button startStopRecord, play, cancel, save;

    private SurfaceView mSurfaceView;

    private SurfaceHolder mSurfaceHolder;

    private Camera mCamera;

    private Camera.Size mSize;

    private boolean isRecording = false;

    private int mCameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;

    private static final SparseIntArray orientations = new SparseIntArray();

    private MediaRecorder mRecorder;

    private int filedId;

    private String path;

    private File file;

    private File[] arrayFile;

    private Chronometer ch;

    static {
        orientations.append(Surface.ROTATION_0, 90);
        orientations.append(Surface.ROTATION_90, 0);
        orientations.append(Surface.ROTATION_180, 270);
        orientations.append(Surface.ROTATION_270, 180);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initWindow();
        setContentView(R.layout.activity_record_video);
        Intent intent = getIntent();
        filedId = intent.getIntExtra("filedId", 0);
        file = new File(Directory.getDirectory(this, filedId, Directory.VIDEO_DIRECTORY)).
                getParentFile();
        arrayFile = file.listFiles();
        initView();
    }

    private void initWindow() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
    }

    private void initView() {
        mSurfaceView = (SurfaceView) findViewById(R.id.surface_view);
        SurfaceHolder holder = mSurfaceView.getHolder();
        holder.setKeepScreenOn(true);
        holder.setFormat(PixelFormat.TRANSPARENT);
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mSurfaceHolder = holder;
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                mSurfaceHolder = holder;
                if(mCamera == null) {
                    return;
                }
                try {
                    mCamera.setPreviewDisplay(holder);
                    mCamera.startPreview();
                } catch(Exception e) {
                    e.printStackTrace();
                    mCamera.release();
                    finish();
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if(isRecording && mCamera != null) {
                    mCamera.lock();
                }
                mSurfaceHolder = null;
                mSurfaceView = null;
                releaseMediaRecorder();
                releaseCamera();
            }
        });
        startStopRecord = (Button) findViewById(R.id.start_stop_record);
        startStopRecord.setOnClickListener(this);
        play = (Button) findViewById(R.id.play_video);
        play.setOnClickListener(this);
        cancel = (Button) findViewById(R.id.cancel);
        cancel.setOnClickListener(this);
        save = (Button) findViewById(R.id.save);
        save.setOnClickListener(this);
        ch = (Chronometer) findViewById(R.id.ch);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.start_stop_record:
                if(isRecording) {
                    stopRecord();
                } else {
                    startRecord();
                }
                break;

            case R.id.play_video:
                if(isRecording) {
                    stopRecord();
                }
                if(path != null) {
                    PlayVideo.actionStart(this, path);
                } else if(arrayFile.length > 0) {
                    PlayVideo.actionStart(this, arrayFile[arrayFile.length - 1].getAbsolutePath());
                } else if(new File(Directory.getDirectory(this, filedId, Directory.VIDEO_DIRECTORY)).
                        getParentFile().listFiles().length == 0) {
                    Toast.makeText(this, "暂无文件可播放，请先录制视频", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.cancel:
                if(isRecording) {
                    stopRecord();
                }
                if(path != null) {
                    file = new File(path);
                    file.delete();
                }
                finish();
                break;
            case R.id.save:
                if(isRecording) {
                    stopRecord();
                }
                if(arrayFile != null) {
                    for(File f : arrayFile) {
                        f.delete();
                    }
                }
                if(path != null) {
                    Intent intent = new Intent();
                    intent.putExtra("videoPath", path);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    Toast.makeText(this, "还没录制视频", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    private void startRecord() {
        if(mRecorder == null) {
            mRecorder = new MediaRecorder();
        }
        if(mCamera != null) {
            mCamera.stopPreview();
            mCamera.unlock();
            mRecorder.setCamera(mCamera);
        }
        try {
            mRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mRecorder.setVideoEncodingBitRate(1024 * 1024);
            mRecorder.setVideoFrameRate(30);
            mSurfaceHolder.setFixedSize(320, 240);
            mRecorder.setVideoSize(320, 240);
            mRecorder.setMaxDuration(60 * 1000);
            mRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
            mRecorder.setOrientationHint(90);
            if(path != null) {
                File file = new File(path);
                file.delete();
            }
            path = Directory.getDirectory(this, filedId, Directory.VIDEO_DIRECTORY);
            if(path != null) {
                mRecorder.setOutputFile(path);
                mRecorder.prepare();
                mRecorder.start();
                isRecording = true;
                startStopRecord.setText("停止拍摄");
            }
            ch.setBase(SystemClock.elapsedRealtime());
            ch.setFormat("%s");
            ch.start();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void stopRecord() {
        try {
            mRecorder.stop();
            mRecorder.reset();
            startStopRecord.setText("重新拍摄");
            isRecording = false;
            ch.stop();
            ch.setText("00:00");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void releaseMediaRecorder() {
        if(mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }
    }

    private void releaseCamera() {
        if(mCamera != null) {
            try {
                mCamera.stopPreview();
                mCamera.setPreviewCallback(null);
                mCamera.unlock();
                mCamera.release();
            } catch(RuntimeException e) {
                e.printStackTrace();
            } finally {
                mCamera = null;
            }
        }
    }

    private void initCamera() {
        if(Camera.getNumberOfCameras() == 2) {
            mCamera = Camera.open(mCameraFacing);
        } else {
            mCamera = Camera.open();
        }
        CameraSizeComparator sizeComparator = new CameraSizeComparator();
        Camera.Parameters parameters = mCamera.getParameters();
        if(mSize == null) {
            List<Camera.Size> vSizeList = parameters.getSupportedPreviewSizes();
            Collections.sort(vSizeList, sizeComparator);
            for(int i = 0; i < vSizeList.size(); i++) {
                Camera.Size size = vSizeList.get(i);
                if(size.width > 800 && size.height > 480) {
                    mSize = size;
                    break;
                }
            }
        }
        parameters.setPreviewSize(mSize.width, mSize.height);
        List<String> focusModesList = parameters.getSupportedFocusModes();
        if(focusModesList.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        } else if(focusModesList.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }
        mCamera.setParameters(parameters);
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int orientation = orientations.get(rotation);
        mCamera.setDisplayOrientation(orientation);
    }

    private class CameraSizeComparator implements Comparator<Camera.Size> {
        @Override
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            if(lhs.width == rhs.width) {
                return 0;
            } else if(lhs.width > rhs.width) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(isRecording) {
            stopRecord();
        }
        releaseCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(isRecording) {
            stopRecord();
        }
    }

    public static Intent actionStart(Context context, int filedId) {
        Intent intent = new Intent(context, RecordVideo.class);
        intent.putExtra("filedId", filedId);
        return intent;
    }
}
