/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tensorflow.lite.examples.detection;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.media.ImageReader.OnImageAvailableListener;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Size;
import android.util.TypedValue;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.tensorflow.lite.examples.detection.customview.OverlayView;
import org.tensorflow.lite.examples.detection.customview.OverlayView.DrawCallback;
import org.tensorflow.lite.examples.detection.env.BorderedText;
import org.tensorflow.lite.examples.detection.env.ImageUtils;
import org.tensorflow.lite.examples.detection.env.Logger;
import org.tensorflow.lite.examples.detection.tflite.Classifier;
import org.tensorflow.lite.examples.detection.tflite.TFLiteObjectDetectionAPIModel;
import org.tensorflow.lite.examples.detection.tracking.MultiBoxTracker;

/**
 * An activity that uses a TensorFlowMultiBoxDetector and ObjectTracker to detect and then track
 * objects.
 */
public class DetectorActivity extends CameraActivity implements OnImageAvailableListener {
  private static final Logger LOGGER = new Logger();

  // Configuration values for the prepackaged SSD model.
  private static final int TF_OD_API_INPUT_SIZE = 300;
  private static final boolean TF_OD_API_IS_QUANTIZED = true;
  private static final String TF_OD_API_MODEL_FILE = "detect.tflite";
  private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt";
  private static final DetectorMode MODE = DetectorMode.TF_OD_API;
  // Minimum detection confidence to track a detection.
  private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.6f;
  private static final boolean MAINTAIN_ASPECT = false;
  private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);
  private static final boolean SAVE_PREVIEW_BITMAP = false;
  private static final float TEXT_SIZE_DIP = 10;
  OverlayView trackingOverlay;
  private Integer sensorOrientation;

  private Classifier detector;

  private long lastProcessingTimeMs;
  private Bitmap rgbFrameBitmap = null;
  private Bitmap croppedBitmap = null;
  private Bitmap cropCopyBitmap = null;

  private boolean computingDetection = false;

  private long timestamp = 0;

  private Matrix frameToCropTransform;
  private Matrix cropToFrameTransform;

  private MultiBoxTracker tracker;

  private byte[] luminanceCopy;

  private BorderedText borderedText;
  private int count=0;//记录人数

  @Override
  public void onPreviewSizeChosen(final Size size, final int rotation) {
    final float textSizePx =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
    borderedText = new BorderedText(textSizePx);
    borderedText.setTypeface(Typeface.MONOSPACE);

    tracker = new MultiBoxTracker(this);

    int cropSize = TF_OD_API_INPUT_SIZE;

    try {
      detector =
          TFLiteObjectDetectionAPIModel.create(
              getAssets(),
              TF_OD_API_MODEL_FILE,
              TF_OD_API_LABELS_FILE,
              TF_OD_API_INPUT_SIZE,
              TF_OD_API_IS_QUANTIZED);
      cropSize = TF_OD_API_INPUT_SIZE;
    } catch (final IOException e) {
      e.printStackTrace();
      LOGGER.e(e, "Exception initializing classifier!");
      Toast toast =
          Toast.makeText(
              getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
      toast.show();
      finish();
    }

    previewWidth = size.getWidth();
    previewHeight = size.getHeight();

    sensorOrientation = rotation - getScreenOrientation();
    LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

    LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
    rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
    croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Config.ARGB_8888);

    frameToCropTransform =
        ImageUtils.getTransformationMatrix(
            previewWidth, previewHeight,
            cropSize, cropSize,
            sensorOrientation, MAINTAIN_ASPECT);

    cropToFrameTransform = new Matrix();
    frameToCropTransform.invert(cropToFrameTransform);

    trackingOverlay = (OverlayView) findViewById(R.id.tracking_overlay);
    trackingOverlay.addCallback(
        new DrawCallback() {
          @Override
          public void drawCallback(final Canvas canvas) {
            tracker.draw(canvas);
            if (isDebug()) {
              tracker.drawDebug(canvas);
            }
          }
        });
  }

  @Override
  protected void processImage() {
    ++timestamp;
    final long currTimestamp = timestamp;
    byte[] originalLuminance = getLuminance();
    tracker.onFrame(
        previewWidth,
        previewHeight,
        getLuminanceStride(),
        sensorOrientation,
        originalLuminance,
        timestamp);
    trackingOverlay.postInvalidate();

    // No mutex needed as this method is not reentrant.
    if (computingDetection) {
      readyForNextImage();
      return;
    }
    computingDetection = true;
    LOGGER.i("Preparing image " + currTimestamp + " for detection in bg thread.");

    rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);

    if (luminanceCopy == null) {
      luminanceCopy = new byte[originalLuminance.length];
    }
    System.arraycopy(originalLuminance, 0, luminanceCopy, 0, originalLuminance.length);
    readyForNextImage();

    final Canvas canvas = new Canvas(croppedBitmap);
    canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
    // For examining the actual TF input.
    if (SAVE_PREVIEW_BITMAP) {
      ImageUtils.saveBitmap(croppedBitmap);
    }
    runInBackground(
        new Runnable() {
          @Override
          public void run() {
            LOGGER.i("Running detection on image " + currTimestamp);
            final long startTime = SystemClock.uptimeMillis();
            final List<Classifier.Recognition> results = detector.recognizeImage(croppedBitmap);
            lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

            cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
            final Canvas canvas = new Canvas(cropCopyBitmap);
            final Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStyle(Style.STROKE);
            paint.setStrokeWidth(2.0f);

            float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
            switch (MODE) {
              case TF_OD_API:
                minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                break;
            }
            final List<Classifier.Recognition> mappedRecognitions =
                new LinkedList<Classifier.Recognition>();
            count=0;//记录人数
            for (final Classifier.Recognition result : results) {
              final RectF location = result.getLocation();
              if (location != null && result.getConfidence() >= minimumConfidence && result.getTitle().equals("person")) {//可以在这改
               System.out.println("Result"+result.getTitle()+result.getId());
                count++;
                canvas.drawRect(location, paint);
                cropToFrameTransform.mapRect(location);
                result.setLocation(location);
                mappedRecognitions.add(result);
              }
            }
            System.out.println(count);//记录人数
              //人数实时上报服务器
            SharedPreferences sharedPre=getSharedPreferences("config", MODE_PRIVATE);
            final String username=sharedPre.getString("username", "");
            final String threshold=sharedPre.getString("threshold", "");
            String mobiletype=android.os.Build.MODEL;
            new AsyncTask<String, Void, String>() {
              @Override
              protected String doInBackground(String... strings) {
                try {
                  URL url = new URL(strings[0]);
                  HttpURLConnection coon = (HttpURLConnection) url.openConnection();
                  coon.setDoOutput(true);
                  coon.setRequestMethod("POST");
                  DataOutputStream out = new DataOutputStream(coon.getOutputStream());
                  out.writeBytes("username=" + username + "&count=" + count+"&mobiletype"+mobiletype);
                  InputStream is = coon.getInputStream();
                  InputStreamReader isr = new InputStreamReader(is, "utf-8");
                  BufferedReader br = new BufferedReader(isr);
                  StringBuilder response = new StringBuilder();
                  String line;
                  while ((line = br.readLine()) != null) {
                    response.append(line);
                  }
                  br.close();
                  isr.close();
                  out.close();
                  return response.toString();
                } catch (MalformedURLException e) {
                  e.printStackTrace();
                } catch (IOException e) {
                  e.printStackTrace();
                }
                return null;
              }

              @Override
              protected void onPostExecute(String s) {
                if (s != null) {
                  if (s.equals("0")) {
                    System.out.println("上传失败！");
                  } else {
                    System.out.println("人数实时上传成功");
                  }
                  super.onPostExecute(s);
                }
              }
            }.execute("http://39.96.169.188/count_app/");


            if(count>Integer.parseInt(threshold)){//每次发送之前都检查threshold
              SimpleDateFormat formatter  =   new   SimpleDateFormat    ("yyyy年MM月dd日_HH时mm分ss秒");
              Date    curDate    =   new    Date(System.currentTimeMillis());//获取当前时间
              String    str    =    formatter.format(curDate);

              String filename=str+".png";
              ImageUtils.saveBitmap(rgbFrameBitmap,filename);
              //将异常图片上传至服务器
//              ByteArrayOutputStream baos = new ByteArrayOutputStream();
//              croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//              byte[] datas = baos.toByteArray();
//              System.out.println("bitmapArray:"+ Arrays.toString(datas));  ("");
              File file =new File("/sdcard/tensorflow/"+filename);
                new AsyncTask<String, Void, String>(){
                    @Override
                    protected String doInBackground(String... strings) {
                        try {
                            URL url = new URL(strings[0]);
                            upload.uploadFile(file,"http://39.96.169.188/abnormal_image/",username);
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                }.execute("http://39.96.169.188/abnormal_image/");
            }
            tracker.trackResults(mappedRecognitions, luminanceCopy, currTimestamp);
            trackingOverlay.postInvalidate();

            computingDetection = false;

            runOnUiThread(
                new Runnable() {
                  @Override
                  public void run() {
                    showFrameInfo(previewWidth + "x" + previewHeight);
                    showCropInfo(cropCopyBitmap.getWidth() + "x" + cropCopyBitmap.getHeight());
                    showInference(lastProcessingTimeMs + "ms");
                    TextView textView =findViewById(R.id.total_detected);
                    TextView textView_hide =findViewById(R.id.total_detected_hide);
                    textView_hide.setText(count+"");
                    textView.setText("当前总人数: "+count+"");
//                    if(count>0){
//                      try {
//                        record_start();
//                      } catch (IOException e) {
//                        e.printStackTrace();
//                      }
//                    }
                  }
                });
          }
        });
  }

  @Override
  protected int getLayoutId() {
    return R.layout.camera_connection_fragment_tracking;
  }

  @Override
  protected Size getDesiredPreviewFrameSize() {
    return DESIRED_PREVIEW_SIZE;
  }

  // Which detection model to use: by default uses Tensorflow Object Detection API frozen
  // checkpoints.
  private enum DetectorMode {
    TF_OD_API;
  }

  @Override
  protected void setUseNNAPI(final boolean isChecked) {
    runInBackground(() -> detector.setUseNNAPI(isChecked));
  }

  @Override
  protected void setNumThreads(final int numThreads) {
    runInBackground(() -> detector.setNumThreads(numThreads));
  }
//  protected void record_start() throws IOException {
//    File file = new File("/sdcard/video.mp4");
//                if (file.exists()) {
//                      // 如果文件存在，删除它，演示代码保证设备上只有一个录音文件
//                       file.delete();
//                  }
//    MediaRecorder mMediaRecorder = new MediaRecorder();
////    mMediaRecorder.reset();
////    mMediaRecorder.setPreviewDisplay(SurfaceHolder.getSurface());
//    mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//    mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);//Surface
//    mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
//    mMediaRecorder.setOutputFile(file.getAbsolutePath());
//    mMediaRecorder.setVideoEncodingBitRate(10000000);
//    mMediaRecorder.setVideoFrameRate(30);
//    mMediaRecorder.setVideoSize(previewWidth, previewHeight);
//    mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
//    mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
//
//    mMediaRecorder.prepare();
//    mMediaRecorder.start();
//
//  }

}