package com.ustc.wsn.mydataapp.bean.cubeView;

/**
 * Created by halo on 2018/3/26.
 */

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView.Renderer;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.ustc.wsn.mydataapp.Listenter.TrackSensorListener;
import com.ustc.wsn.mydataapp.bean.PhoneState;
import com.ustc.wsn.mydataapp.bean.StoreData;
import com.ustc.wsn.mydataapp.bean.math.myMath;

public class MyRender implements Renderer {
    private final String TAG = MyRender.class.toString();

    private volatile float[] eulerRead = {0f, 0f, 0f};
    private volatile float[] Euler = {0f, 0f, 0f};
    int one = 0x10000*2;
    private IntBuffer colorBuffer;
    private int[] colors = new int[]{

            one / 4, one, 0, one, one / 4, one, 0, one, one / 4, one, 0, one, one / 4, one, 0, one,

            one, one / 4, 0, one, one, one / 4, 0, one, one, one / 4, 0, one, one, one / 4, 0, one, one, one, 0, one, one, one, 0, one, one, one, 0, one, one, one, 0, one, one, 0, 0, one, one, 0, 0, one, one, 0, 0, one, one, 0, 0, one,

            0, 0, one, one, 0, 0, one, one, 0, 0, one, one, 0, 0, one, one,

            one, 0, one, one, one, 0, one, one, one, 0, one, one, one, 0, one, one,};

    private IntBuffer quaterBuffer;
    private int[] quaterVertices = new int[]{one/2, one, -one/10, -one/2, one, -one/10, one/2, one, one/10, -one/2, one, one/10,

            one/2, -one, one/10, -one/2, -one, one/10, one/2, -one, -one/10, -one/2, -one, -one/10,

            one/2, one, one/10, -one/2, one, one/10, one/2, -one, one/10, -one/2, -one, one/10,

            one/2, -one, -one/10, -one/2, -one, -one/10, one/2, one, -one/10, -one/2, one, -one/10,

            -one/2, one, one/10, -one/2, one, -one/10, -one/2, -one, one/10, -one/2, -one, -one/10,

            one/2, one, -one/10, one/2, one, one/10, one/2, -one, -one/10, one/2, -one, one/10};

    @Override
    public void onDrawFrame(GL10 gl) {

        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
        gl.glLoadIdentity();

        gl.glTranslatef(0.0f, 0.0f, -4.0f);


        gl.glRotatef(Euler[2], 0.0f, 0.0f, -1.0f);
        gl.glRotatef(Euler[1], 1.0f, 0.0f, 0.0f);
        gl.glRotatef(Euler[0], 0.0f, 1.0f, 0.0f);

        gl.glColorPointer(4, GL10.GL_FIXED, 0, colorBuffer);
        gl.glVertexPointer(3, GL10.GL_FIXED, 0, quaterBuffer);


        for (int i = 0; i < 6; i++) {
            gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, i * 4, 4);
        }

        gl.glFinish();
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);

        Log.d(TAG, "cube view");
        float[] euler = eulerRead.clone();

        Euler[0] = euler[0] / myMath.PI * 180;
        Euler[1] = euler[1] / myMath.PI * 180;
        Euler[2] = euler[2] / myMath.PI * 180;

    }

    public void updateEuler(float[] e){
        eulerRead = e.clone();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        float ratio = (float) width / height;

        gl.glViewport(0, 0, width, height);

        gl.glMatrixMode(GL10.GL_PROJECTION);

        gl.glLoadIdentity();

        gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);

        gl.glMatrixMode(GL10.GL_MODELVIEW);

        gl.glLoadIdentity();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        gl.glShadeModel(GL10.GL_SMOOTH);

        gl.glClearColor(0, 0, 0, 0);


        gl.glClearDepthf(1.0f);

        gl.glEnable(GL10.GL_DEPTH_TEST);

        gl.glDepthFunc(GL10.GL_LEQUAL);


        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(colors.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        colorBuffer = byteBuffer.asIntBuffer();
        colorBuffer.put(colors);
        colorBuffer.position(0);

        byteBuffer = ByteBuffer.allocateDirect(quaterVertices.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        quaterBuffer = byteBuffer.asIntBuffer();
        quaterBuffer.put(quaterVertices);
        quaterBuffer.position(0);
    }
}