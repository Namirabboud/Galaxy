package namir.arise.shapes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import namir.arise.MyGLRenderer;

/**
 * Created by namir on 4/1/15.
 */
abstract class Texture {

    private final FloatBuffer vertexBuffer;
    private final ShortBuffer drawListBuffer;
    private final FloatBuffer mCubeTextureCoordinates;
    private final int mProgram;
    private final int resourceId;
    private int mTextureDataHandle;
    private float positionX;
    private float positionY;

    private static final String TAG = "TextureHelper";

    private final String vertexShaderCode =
        "attribute vec2 a_TexCoordinate;" +
        "varying vec2 v_TexCoordinate;" +
        "uniform mat4 uMVPMatrix;" +
        "attribute vec4 vPosition;" +
        "void main() {" +
        "   gl_Position = uMVPMatrix * vPosition;" +
        "   v_TexCoordinate = a_TexCoordinate;" +
        "}";

    private final String fragmentShaderCode =
        "precision mediump float;" +
        "uniform vec4 vColor;" +
        "uniform sampler2D u_Texture;" +
        "varying vec2 v_TexCoordinate;" +
        "void main() {" +
        "   gl_FragColor = (vColor * texture2D(u_Texture, v_TexCoordinate));" +
        "}";

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 2;

    private float squareCoords[] = new float[8];

    private short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; //Order to draw vertices
    private final int vertexStride = COORDS_PER_VERTEX * 4; //Bytes per vertex

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };

    public Texture(final Context activityContext, int resourceId, float[] mSquareCoords){
        /*
        constructor.
         */

        setSquareCoords( mSquareCoords );

        final Context mActivityContext;
        this.resourceId = resourceId;
        mActivityContext = activityContext;

        ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length * 4);
        //Use the Device's Native Byte Order
        bb.order(ByteOrder.nativeOrder());
        //Create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer();
        //Add the coordinates to the FloatBuffer
        vertexBuffer.put(squareCoords);
        //Set the Buffer to Read the first coordinate
        vertexBuffer.position(0);

        final float[] cubeTextureCoordinateData ={
                1f, 0f,
                1f, 1f,
                0f, 1f,
                0f, 0f,
        };

        mCubeTextureCoordinates = ByteBuffer.allocateDirect(cubeTextureCoordinateData.length * 4).
                order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeTextureCoordinates.put(cubeTextureCoordinateData).position(0);

        //Initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(squareCoords.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        // create empty OpenGL ES Program
        mProgram = GLES20.glCreateProgram();

        // add the vertex shader to program
        GLES20.glAttachShader(mProgram, vertexShader);

        // add the fragment shader to program
        GLES20.glAttachShader(mProgram, fragmentShader);

        //Texture Code
        GLES20.glBindAttribLocation(mProgram, 0, "a_TexCoordinate");

        // creates OpenGL ES program executables
        GLES20.glLinkProgram(mProgram);

        //Load the texture
        mTextureDataHandle = loadTextures(mActivityContext);

        positionX = positionY = 0;
    }

    private int loadTextures( Context context  ){
        /*
        this method will take in an android context and
        a resource ID and will return the ID of the loaded
        OpenGl texture
         */

        //generate a new texture ID
        final int[] textureObjectIds = new int[1];

        //generate one texture object
        //OpenGL will store the generated IDs in
        //textureObjectIds
        GLES20.glGenTextures(1, textureObjectIds, 0);

        if( textureObjectIds[0] == 0 ){
            Log.w(TAG, "could not generate a new OpenGl texture object");
            return 0;
        }

        //decompress the image into an Android bitmap
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;

        final Bitmap bitmap = BitmapFactory.decodeResource( context.getResources(), resourceId, options );

        if( bitmap == null ){
            Log.w( TAG, "Resource ID" + resourceId + "could not be decoded" );
            GLES20.glDeleteTextures( 1, textureObjectIds, 0 );
            return 0;
        }

        //future texture calls should be applied to this texture
        //object
        GLES20.glBindTexture( GLES20.GL_TEXTURE_2D, textureObjectIds[0] );

        GLES20.glTexParameteri( GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST );
        GLES20.glTexParameteri( GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_NEAREST );

        //load the bitmap data into OpenGL object
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

        //Now that the data’s been loaded into OpenGL,
        // we no longer need to keep the Android bitmap around.
        bitmap.recycle();

        return textureObjectIds[0];
    }

    public void draw( float[] mvpMatrix ){
        int mPositionHandle;
        int mColorHandle;
        int mMVPMatrixHandle;
        int mTextureUniformHandle;
        int mTextureCoordinateHandle;

        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                                     GLES20.GL_FLOAT, false,
                                     vertexStride, vertexBuffer);

        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);


        //Set Texture Handles and bind Texture
        mTextureUniformHandle = GLES20.glGetAttribLocation(mProgram, "u_Texture");
        mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgram, "a_TexCoordinate");

        //Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        //Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);

        //Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0);

        mCubeTextureCoordinates.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, 2,
                GLES20.GL_FLOAT, false, 0, mCubeTextureCoordinates);

        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, generateMatrix( mvpMatrix ), 0);

        // Draw the square
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        mCubeTextureCoordinates.position(0);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureCoordinateHandle);
    }

    public void setPositionX( float X ){
        this.positionX = X;
    }

    public float getPositionX(){
        return this.positionX;
    }

    public void setPositionY( float Y ){
        this.positionY = Y;
    }

    public float getPositionY(){
        return this.positionY;
    }

    public void setSquareCoords( float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4 ){
        squareCoords[0] = x1;
        squareCoords[1] = y1;
        squareCoords[2] = x2;
        squareCoords[3] = y2;
        squareCoords[4] = x3;
        squareCoords[5] = y3;
        squareCoords[6] = x4;
        squareCoords[7] = y4;
    }

    public void setSquareCoords( float[] mSquareCoords ){
        squareCoords[0] = mSquareCoords[0];
        squareCoords[1] = mSquareCoords[1];
        squareCoords[2] = mSquareCoords[2];
        squareCoords[3] = mSquareCoords[3];
        squareCoords[4] = mSquareCoords[4];
        squareCoords[5] = mSquareCoords[5];
        squareCoords[6] = mSquareCoords[6];
        squareCoords[7] = mSquareCoords[7];
    }

    abstract float[] generateMatrix( float[] mvpMatrix  );
}
