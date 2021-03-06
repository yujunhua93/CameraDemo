package com.example.camerademo1;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import androidx.annotation.NonNull;

import static androidx.core.math.MathUtils.clamp;

public class Camera2Proxy {

    private static final String TAG = "Camera2Proxy";
    private Activity mActivity;

    private int mCameraId = CameraCharacteristics.LENS_FACING_FRONT;

    private Size mPreviewSize;
    private CameraManager mCameraManager;
    private CameraCharacteristics mCameraCharacteristics;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCameraCaptureSession;

    private CaptureRequest.Builder mPreviewRequestBuilder;

    private CaptureRequest mCaptureRequest;

    private Handler mBackgroundHandler;

    private HandlerThread mBackgroundThread;
    private ImageReader mImageReader;
    private Surface mPreviewSurface;
    private OrientationEventListener mOrientationEventListener;

    private int mDisplayRotate = 0;
    private int mDeviceOrientation = 0; // ???????????????????????????????????????
    private int mZoom = 1; // ??????
    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice = camera;
            initPreviewRequest();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            releaseCamera();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            releaseCamera();
        }
    };


    public void setPreviewSurface(SurfaceHolder holder) {
        mPreviewSurface = holder.getSurface();
    }

    public void setPreviewSurface(SurfaceTexture surfaceTexture) {
        surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        mPreviewSurface = new Surface(surfaceTexture);
    }

    public Camera2Proxy(Activity activity){
        mActivity = activity;
        mCameraManager = (CameraManager) mActivity.getSystemService(Context.CAMERA_SERVICE);
        mOrientationEventListener = new OrientationEventListener(mActivity) {
            @Override
            public void onOrientationChanged(int orientation) {
                mDeviceOrientation = orientation;
            }
        };
    }

    private Size chooseOptimalSize(Size[] sizes, int viewWidth, int viewHeight, Size pictureSize) {
        int totalRotation = getRotation();
        boolean swapRotation = totalRotation == 90 || totalRotation == 270;
        int width = swapRotation ? viewHeight : viewWidth;
        int height = swapRotation ? viewWidth : viewHeight;
        return getSuitableSize(sizes, width, height, pictureSize);
    }

    @SuppressLint("MissingPermission")
    public void openCamera(int width, int height) {
        Log.v(TAG, "openCamera");
        startBackgroundThread(); // ?????? releaseCamera() ???????????? stopBackgroundThread()
        mOrientationEventListener.enable();
        try {
            mCameraCharacteristics = mCameraManager.getCameraCharacteristics(Integer.toString(mCameraId));
            StreamConfigurationMap map = mCameraCharacteristics.get(CameraCharacteristics
                    .SCALER_STREAM_CONFIGURATION_MAP);
            // ????????????????????????????????????????????????????????????
            Size largest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new
                    CompareSizesByArea());
            Log.d(TAG, "picture size: " + largest.getWidth() + "*" + largest.getHeight());
            mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(), ImageFormat.JPEG, 2);
            // ????????????????????????????????????????????????????????????????????????????????????????????????????????????
            Log.d(TAG, "preview size: " + mPreviewSize.getWidth() + "*" + mPreviewSize.getHeight());
            mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), width, height, largest);
            // ???????????????
            mCameraManager.openCamera(Integer.toString(mCameraId), mStateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private Size getSuitableSize(Size[] sizes, int width, int height, Size pictureSize) {
        int minDelta = Integer.MAX_VALUE; // ?????????????????????????????????????????????????????????????????????????????????
        int index = 0; // ????????????????????????????????????
        float aspectRatio = pictureSize.getHeight() * 1.0f / pictureSize.getWidth();
        Log.d(TAG, "getSuitableSize. aspectRatio: " + aspectRatio);
        for (int i = 0; i < sizes.length; i++) {
            Size size = sizes[i];
            // ???????????????????????????
            if (size.getWidth() * aspectRatio == size.getHeight()) {
                int delta = Math.abs(width - size.getWidth());
                if (delta == 0) {
                    return size;
                }
                if (minDelta > delta) {
                    minDelta = delta;
                    index = i;
                }
            }
        }
        return sizes[index];
    }

    private int getRotation() {
        int displayRotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
        switch (displayRotation) {
            case Surface.ROTATION_0:
                displayRotation = 90;
                break;
            case Surface.ROTATION_90:
                displayRotation = 0;
                break;
            case Surface.ROTATION_180:
                displayRotation = 270;
                break;
            case Surface.ROTATION_270:
                displayRotation = 180;
                break;
        }
        int sensorOrientation = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        mDisplayRotate = (displayRotation + sensorOrientation + 270) % 360;
        return mDisplayRotate;
    }

    private void startBackgroundThread() {
        if (mBackgroundThread == null || mBackgroundHandler == null) {
            mBackgroundThread = new HandlerThread("CameraBackground");
            mBackgroundThread.start();
            mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
        }
    }

    private void initPreviewRequest() {
        try {
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(mPreviewSurface); // ????????????????????? Surface
            mCameraDevice.createCaptureSession(Arrays.asList(mPreviewSurface, mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            mCameraCaptureSession = session;
                            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest
                                    .CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                            // ??????????????????
                            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest
                                    .CONTROL_AE_MODE_ON_AUTO_FLASH);
                            // ??????????????????????????????
                            mCaptureRequest = mPreviewRequestBuilder.build();
                            startPreview();
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                        }
                    },mBackgroundHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void startPreview() {
        if (mCameraCaptureSession == null || mPreviewRequestBuilder == null) {
            Log.w(TAG, "startPreview: mCaptureSession or mPreviewRequestBuilder is null");
            return;
        }
        try {
            // ?????????????????????????????????????????????
            mCameraCaptureSession.setRepeatingRequest(mCaptureRequest, null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    public void stopPreview() {
        if (mCameraCaptureSession == null || mPreviewRequestBuilder == null) {
            Log.w(TAG, "stopPreview: mCaptureSession or mPreviewRequestBuilder is null");
            return;
        }
        try {
            mCameraCaptureSession.stopRepeating();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void releaseCamera() {
        Log.v(TAG, "releaseCamera");
        if (null != mCameraCaptureSession) {
            mCameraCaptureSession.close();
            mCameraCaptureSession = null;
        }
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
        if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
        }
        mOrientationEventListener.disable();
        stopBackgroundThread(); // ?????? openCamera() ???????????? startBackgroundThread()
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Size getPreviewSize() {
        return mPreviewSize;
    }


    public void focusOnPoint(double x, double y, int width, int height) {
        if (mCameraDevice == null || mPreviewRequestBuilder == null) {
            return;
        }
        // 1. ???????????????view???????????????
        int previewWidth = mPreviewSize.getWidth();
        int previewHeight = mPreviewSize.getHeight();
        if (mDisplayRotate == 90 || mDisplayRotate == 270) {
            previewWidth = mPreviewSize.getHeight();
            previewHeight = mPreviewSize.getWidth();
        }
        // 2. ???????????????????????????????????????view???????????????????????????????????????
        double tmp;
        double imgScale;
        double verticalOffset = 0;
        double horizontalOffset = 0;
        if (previewHeight * width > previewWidth * height) {
            imgScale = width * 1.0 / previewWidth;
            verticalOffset = (previewHeight - height / imgScale) / 2;
        } else {
            imgScale = height * 1.0 / previewHeight;
            horizontalOffset = (previewWidth - width / imgScale) / 2;
        }
        // 3. ?????????????????????????????????????????????
        x = x / imgScale + horizontalOffset;
        y = y / imgScale + verticalOffset;
        if (90 == mDisplayRotate) {
            tmp = x;
            x = y;
            y = mPreviewSize.getHeight() - tmp;
        } else if (270 == mDisplayRotate) {
            tmp = x;
            x = mPreviewSize.getWidth() - y;
            y = tmp;
        }
        // 4. ????????????????????????????????????????????????????????????????????????
        Rect cropRegion = mPreviewRequestBuilder.get(CaptureRequest.SCALER_CROP_REGION);
        if (cropRegion == null) {
            Log.w(TAG, "can't get crop region");
            cropRegion = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        }
        int cropWidth = cropRegion.width();
        int cropHeight = cropRegion.height();
        if (mPreviewSize.getHeight() * cropWidth > mPreviewSize.getWidth() * cropHeight) {
            imgScale = cropHeight * 1.0 / mPreviewSize.getHeight();
            verticalOffset = 0;
            horizontalOffset = (cropWidth - imgScale * mPreviewSize.getWidth()) / 2;
        } else {
            imgScale = cropWidth * 1.0 / mPreviewSize.getWidth();
            horizontalOffset = 0;
            verticalOffset = (cropHeight - imgScale * mPreviewSize.getHeight()) / 2;
        }
        // 5. ?????????????????????????????????????????????????????????????????????????????????
        x = x * imgScale + horizontalOffset + cropRegion.left;
        y = y * imgScale + verticalOffset + cropRegion.top;
        double tapAreaRatio = 0.1;
        Rect rect = new Rect();
        rect.left = clamp((int) (x - tapAreaRatio / 2 * cropRegion.width()), 0, cropRegion.width());
        rect.right = clamp((int) (x + tapAreaRatio / 2 * cropRegion.width()), 0, cropRegion.width());
        rect.top = clamp((int) (y - tapAreaRatio / 2 * cropRegion.height()), 0, cropRegion.height());
        rect.bottom = clamp((int) (y + tapAreaRatio / 2 * cropRegion.height()), 0, cropRegion.height());
        // 6. ?????? AF???AE ???????????????????????????????????? rect
        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, new MeteringRectangle[]{new MeteringRectangle
                (rect, 1000)});
        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_REGIONS, new MeteringRectangle[]{new MeteringRectangle
                (rect, 1000)});
        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CameraMetadata
                .CONTROL_AE_PRECAPTURE_TRIGGER_START);
        try {
            // 7. ???????????????????????????????????????????????????
            mCameraCaptureSession.capture(mPreviewRequestBuilder.build(), mAfCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    private final CameraCaptureSession.CaptureCallback mAfCaptureCallback = new CameraCaptureSession.CaptureCallback() {

        private void process(CaptureResult result) {
            Integer state = result.get(CaptureResult.CONTROL_AF_STATE);
            if (null == state) {
                return;
            }
            if (state == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED || state == CaptureResult
                    .CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest
                        .CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.FLASH_MODE_OFF);
                startPreview();
            }
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            process(result);
        }
    };


    public void handleZoom(boolean isZoomIn) {
        if (mCameraDevice == null || mCameraCharacteristics == null || mPreviewRequestBuilder == null) {
            return;
        }
        int maxZoom = mCameraCharacteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM).intValue()
                * 10;
        Rect rect = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        if (isZoomIn && mZoom < maxZoom) {
            mZoom++;
        } else if (mZoom > 1) {
            mZoom--;
        }
        int minW = rect.width() / maxZoom;
        int minH = rect.height() / maxZoom;
        int difW = rect.width() - minW;
        int difH = rect.height() - minH;
        int cropW = difW * mZoom / 100;
        int cropH = difH * mZoom / 100;
        cropW -= cropW & 3;
        cropH -= cropH & 3;
        Rect zoomRect = new Rect(cropW, cropH, rect.width() - cropW, rect.height() - cropH);
        mPreviewRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoomRect);
        mCaptureRequest = mPreviewRequestBuilder.build();
        startPreview(); // ???????????? start preview ????????????
    }

    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }

}
