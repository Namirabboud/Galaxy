package namir.arise.shapes.GlText_trial;

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
import java.util.Vector;

import namir.arise.MyGLRenderer;
import namir.arise.R;

/**
 * Created by namir on 4/9/15.
 */
public class GL_Text {

    private float LETTER_TEXTURE_WIDTH = 0.125f;
    private float LETTER_DISPLAY_WIDTH = 0.2f;

    private float vertex[];
    private float cubeTextureCoordinateData[];
    private short drawOrder[];

    private int vertexIndex;
    private int TextureCoordinateIndex;
    private int drawOrderIndex;

    private FloatBuffer vertexBuffer;
    private FloatBuffer mCubeTextureCoordinates;
    private ShortBuffer drawListBuffer;

    private int mProgram;
    private int mTextureDataHandle;
    private final Context mActivityContext;

    private int letterNumber;

    public Vector<TextObject> txtCollection;

    private static final String TAG = "GL_Text";

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
    private final int vertexStride = COORDS_PER_VERTEX * 4; //Bytes per vertex

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };

    public GL_Text( final Context activityContext ){
        /*
        constructor.
         */
        mActivityContext = activityContext;

        //initialize the arrays
        vertex                      = new float[ 2 * 10 ];
        cubeTextureCoordinateData   = new float[ 2 * 10 ];
        drawOrder                   = new short[ 10 ];

        vertexIndex             = 0;
        TextureCoordinateIndex  = 0;
        drawOrderIndex          = 0;

        txtCollection = new Vector<TextObject>();

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

    }

    public void addText( TextObject objct ){
        //add object to the txtCollection vector
        txtCollection.add( objct );
    }

    public void removeText( TextObject object ){
        for ( int i = 0; i < txtCollection.size(); i ++ ){
            if ( txtCollection.get(i) == object ) {
                txtCollection.remove(i);
            }
        }
    }

    public void prepareToDraw(){
        /*
        this function will
            1-  count the number of letters to
                allocate the true size for the arrays;
            2-  add right coordination to the arrays
                to each letter
            3-  put arrays in buffers
         */

        vertexIndex = 0;
        TextureCoordinateIndex = 0;
        drawOrderIndex = 0;

        letterNumber = 0;

        for ( int i = 0; i < txtCollection.size(); i++ )
            for( int letter = 0; letter < txtCollection.get(i).text.length(); letter ++ )
                letterNumber++;

        /*
        now we have the number of the letters stored in letterNumber
        lets allocate the right size to our arrays
        */

        vertex                      = new float[ 8 * letterNumber ];
        cubeTextureCoordinateData   = new float[ 8 * letterNumber ];
        drawOrder                   = new short[ 6 * letterNumber ];

        letterNumber = 0; //refresh the letter number

        //looping texts
        for ( int i = 0; i < txtCollection.size(); i++ ){
            convertTextToArrayData( txtCollection.get(i) );
        }

        convertToBuffers();
    }

    private void convertTextToArrayData( TextObject object ){
        float[] localVertex     = new float[ 8 ];
        float[] localTexture    = new float[ 8 ];
        short[] localDrawOrder  = new short[ 6 ];

        String text = object.text;
        float X     = object.positionX;
        float Y     = object.positionY;

        //looping each letter
        for( int letterIndex = 0; letterIndex < text.length(); letterIndex ++ ){
            int c_val  = (int)text.charAt(letterIndex);

            int index   = getLetterIndex( c_val );
            int row     = index / 8;
            int column  = index % 8;

            /* calculation of the localVertex array
            that contains the position of the letters
            on the screen */

            /* bottom right */
            localVertex[0] = X - (letterIndex * 0.2f)  + LETTER_DISPLAY_WIDTH;
            localVertex[1] = Y + LETTER_DISPLAY_WIDTH;

             /* top right */
            localVertex[2] = X - (letterIndex * 0.2f)  + LETTER_DISPLAY_WIDTH;
            localVertex[3] = Y;

            /* top left */
            localVertex[4] = X - (letterIndex * 0.2f);
            localVertex[5] = Y;

            /* bottom left */
            localVertex[6] = X - (letterIndex * 0.2f);
            localVertex[7] = Y + LETTER_DISPLAY_WIDTH;

            /* calculation of the localTexture array
            containing the position of the letters in the
            texture */

            /* top left */
            localTexture[0] = column * LETTER_TEXTURE_WIDTH;
            localTexture[1] = row * LETTER_TEXTURE_WIDTH;

            /* bottom left */
            localTexture[2] = localTexture[0];
            localTexture[3] = localTexture[1] + LETTER_TEXTURE_WIDTH;

            /* bottom right */
            localTexture[4] = localTexture[0] + LETTER_TEXTURE_WIDTH;
            localTexture[5] = localTexture[3];

            /* top right */
            localTexture[6] = localTexture[4];
            localTexture[7] = localTexture[1];

            /* calculation of the localDrawOrder array */
            localDrawOrder[0] = (short)(letterNumber * 4);
            localDrawOrder[1] = (short)(localDrawOrder[0] + 1);
            localDrawOrder[2] = (short)(localDrawOrder[0] + 2);
            localDrawOrder[3] = localDrawOrder[0];
            localDrawOrder[4] = localDrawOrder[2];
            localDrawOrder[5] = (short)(localDrawOrder[0] + 3);

            letterNumber ++;

            addDataToArrays( localVertex, localTexture, localDrawOrder );

        }
    }

    private void convertToBuffers(){
        ByteBuffer bb = ByteBuffer.allocateDirect(vertex.length * 4);
        //Use the Device's Native Byte Order
        bb.order(ByteOrder.nativeOrder());
        //Create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer();
        //Add the coordinates to the FloatBuffer
        vertexBuffer.put(vertex);
        //Set the Buffer to Read the first coordinate
        vertexBuffer.position(0);

        //texture buffer
        mCubeTextureCoordinates = ByteBuffer.allocateDirect(cubeTextureCoordinateData.length * 4).
                order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeTextureCoordinates.put(cubeTextureCoordinateData).position(0);

        //Initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(vertex.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        //Load the texture
        mTextureDataHandle = loadTextures(mActivityContext);
    }

    private void addDataToArrays( float[] localVertex, float[] localTexture, short[] localDrawOrder ){
        /*
        this function adds the content of the input arrays
        to our global arrays
         */

        for ( int i = 0; i < localVertex.length; i++ ){
            vertex[vertexIndex] = localVertex[i];
            vertexIndex ++;
        }

        for ( int i = 0; i < localTexture.length; i++ ){
            cubeTextureCoordinateData[ TextureCoordinateIndex ] = localTexture[i];
            TextureCoordinateIndex ++;
        }

        for ( int i = 0; i < localDrawOrder.length; i++ ){
            drawOrder[ drawOrderIndex ] = localDrawOrder[i];
            drawOrderIndex ++;
        }

    }

    private int getLetterIndex( int c_val ){
        int indx = -1;

        // Retrieve the index
        if( c_val > 64 && c_val < 91 ) // A-Z
            indx = c_val - 65;
        else if( c_val > 96 && c_val < 123 ) // a-z
            indx = c_val - 97;
        else if( c_val > 47 && c_val < 58 ) // 0-9
            indx = c_val - 48 + 26;
        else if( c_val == 43 ) // +
            indx = 38;
        else if( c_val == 45 ) // -
            indx = 39;
        else if( c_val == 33 ) // !
            indx = 36;
        else if( c_val == 63 ) // ?
            indx = 37;
        else if( c_val == 61 ) // =
            indx = 40;
        else if( c_val == 58 ) // :
            indx = 41;
        else if( c_val == 46 ) // .
            indx = 42;
        else if( c_val == 44 ) // ,
            indx = 43;
        else if( c_val == 42 ) // *
            indx = 44;
        else if( c_val == 36 ) // $
            indx = 45;

        return indx;
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
        //final BitmapFactory.Options options = new BitmapFactory.Options();
        //options.inScaled = false;

        final Bitmap bitmap = BitmapFactory.decodeResource( context.getResources(), R.drawable.font_chaka );

        if( bitmap == null ){
            GLES20.glDeleteTextures( 1, textureObjectIds, 0 );
            return 0;
        }

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture( GLES20.GL_TEXTURE_2D, textureObjectIds[0] );

        GLES20.glTexParameteri( GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR );
        GLES20.glTexParameteri( GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR );

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

        mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgram, "a_TexCoordinate");

        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, 2,
                                     GLES20.GL_FLOAT, false, 0, mCubeTextureCoordinates);

        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);


        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);


        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        //Set Texture Handles and bind Texture
        mTextureUniformHandle = GLES20.glGetAttribLocation(mProgram, "u_Texture");

        //Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        //Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);

        //Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0);

        // Draw the square
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        mCubeTextureCoordinates.position(0);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureCoordinateHandle);
    }
}
