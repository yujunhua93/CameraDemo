package com.example.camerademo1.gl;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class MyGLSurfaceView extends GLSurfaceView {

    private TriggerRender mMyGlRender;

    public MyGLSurfaceView(Context context) {
        super(context);
        setEGLContextClientVersion(2);

        mMyGlRender = new TriggerRender();

        setRenderer(mMyGlRender);

        // Set the RenderMode
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }
}
