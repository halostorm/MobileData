package com.ustc.wsn.mydataapp.bean.cubeView;

/**
 * Created by halo on 2018/3/26.
 */

import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

import com.ustc.wsn.mydataapp.bean.PhoneState;
import com.ustc.wsn.mydataapp.bean.math.myMath;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static javax.microedition.khronos.opengles.GL10.GL_VERTEX_ARRAY;

public class MyRender implements Renderer {
    private final String TAG = MyRender.class.toString();

    private volatile float[] eulerRead = {0f, 0f, 0f};
    private volatile float[] Euler = {0f, 0f, 0f};
    private PhoneGlModel phone;

    public MyRender(){
        phone = new PhoneGlModel();
    }

    @Override
    public void onDrawFrame(GL10 gl) {

        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        gl.glEnableClientState(GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
        gl.glLoadIdentity();

        gl.glTranslatef(0.0f, 0.0f, -4.0f);


        gl.glRotatef(Euler[2], 0.0f, 0.0f, -1.0f);
        gl.glRotatef(Euler[1], 1.0f, 0.0f, 0.0f);
        gl.glRotatef(Euler[0], 0.0f, 1.0f, 0.0f);

        gl.glColorPointer(4, GL10.GL_FIXED, 0, phone.colorBuffer);
        gl.glVertexPointer(3, GL10.GL_FIXED, 0, phone.quaterBuffer);


        for (int i = 0; i < 6; i++) {
            gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, i * 4, 4);
        }

        gl.glFinish();
        gl.glDisableClientState(GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);

        //Log.d(TAG, "cube view");
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

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(phone.colors.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        phone.colorBuffer = byteBuffer.asIntBuffer();
        phone.colorBuffer.put(phone.colors);
        phone.colorBuffer.position(0);

        byteBuffer = ByteBuffer.allocateDirect(phone.quaterVertices.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        phone.quaterBuffer = byteBuffer.asIntBuffer();
        phone.quaterBuffer.put(phone.quaterVertices);
        phone.quaterBuffer.position(0);
    }
}