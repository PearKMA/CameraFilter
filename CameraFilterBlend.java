package com.hunglv.vintagecamera.filter.camera;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import androidx.annotation.NonNull;

import com.otaliastudios.cameraview.filter.BaseFilter;
import com.otaliastudios.cameraview.internal.GlUtils;

public class CameraFilterBlend extends BaseFilter {

    private static final String ALPHA_BLEND_FRAGMENT_SHADER = "#extension GL_OES_EGL_image_external : require\n"+
            " precision mediump float;\n" +
            "varying highp vec2 vTextureCoord;\n" +
            "\n" +
            " uniform samplerExternalOES sTexture;\n" +
            " uniform sampler2D inputImageTexture2;\n" +
            "\n" +
            " void main()\n" +
            " {\n" +
            "   lowp vec4 textureColor = texture2D(sTexture, vTextureCoord);\n" +
            "   float greyScale = dot(textureColor.rgb,vec3(0.222,0.707,0.071));\n" +
            "   textureColor.rgb = mix(textureColor.rgb,vec3(greyScale),0.6);\n" +
            "   lowp vec4 textureColor2 = texture2D(inputImageTexture2, vTextureCoord);\n" +
            "\n" +
            "   gl_FragColor = vec4(mix(textureColor.rgb, textureColor2.rgb, textureColor2.a * 0.6), textureColor.a);\n" +
            " }";

    public CameraFilterBlend() {
        positionTexture2 = -1;
    }
    private Bitmap texture2;
    private int positionTexture2;
    private int filterSource = -1;

    @Override
    public void onCreate(int programHandle) {
        super.onCreate(programHandle);
        if (filterSource == -1) {
            filterSource = loadTexture(texture2, -1, false);
        }
        positionTexture2 = GLES20.glGetUniformLocation(programHandle, "inputImageTexture2");
        GlUtils.checkLocation(positionTexture2, "inputImageTexture2");

    }

    @Override
    protected void onPreDraw(long timestampUs, float[] transformMatrix) {
        super.onPreDraw(timestampUs, transformMatrix);
        GLES20.glEnableVertexAttribArray(positionTexture2);
        GlUtils.checkError("glVertexAttribPointer");

        GlUtils.checkError("glVertexAttribPointer");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE3);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, filterSource);
        GLES20.glUniform1i(positionTexture2, 3);
        GlUtils.checkError("glUniform1f");
    }
    private static int loadTexture(final Bitmap img, final int usedTexId, final boolean recycle) {
        int[] textures = new int[1];
        if (usedTexId == -1) {
            GLES20.glGenTextures(1, textures, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, img, 0);
        } else {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, usedTexId);
            GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, img);
            textures[0] = usedTexId;
        }
        if (recycle) {
            img.recycle();
        }
        return textures[0];
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        GLES20.glDisableVertexAttribArray(positionTexture2);
        texture2 = null;
        positionTexture2 = -1;
    }
    @NonNull
    @Override
    public String getFragmentShader() {
        return ALPHA_BLEND_FRAGMENT_SHADER;
    }

    @SuppressWarnings("WeakerAccess")
    public void setBlendTexture(Bitmap bitmap) {
        texture2 = bitmap;
    }
}
