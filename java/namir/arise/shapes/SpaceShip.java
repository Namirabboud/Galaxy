package namir.arise.shapes;

import android.content.Context;
import android.opengl.Matrix;

import namir.arise.MyGLRenderer;
import namir.arise.R;

/**
 * Created by namir on 4/2/15.
 */
public class SpaceShip extends Texture {
    private static int sourceId = R.drawable.fighter;
    private int Life;
    private static float squareCoords[] = {
        -0.5f, 0.5f,    //top left
        -0.5f, -0.5f,   //bottom left
        0.5f, -0.5f,    //bottom right
        0.5f, 0.5f,     //top right
    };
    private float prevSlide = 0.0f;

    public SpaceShip( final Context activityContext ){
        super( activityContext, sourceId, squareCoords );
        Life = 3;
    }

    public void lostLife(){
        this.Life -= 1;
    }

    public float[] generateMatrix( float[]  mpvMatrix ){
        float[] mModelMatrix    = new float[16];
        float[] mTempMatrix     = new float[16];
        float[] returnMatrix    = new float[16];

        Matrix.setIdentityM(mModelMatrix, 0);

        //translate mTriangle with sensors
        //adjust the sensetivity of the sensors
        if( Math.abs ( prevSlide - ( float ) MyGLRenderer.SENSORE_ACCELEROMETER_X ) > 0.08 )
            setPositionX((float)MyGLRenderer.SENSORE_ACCELEROMETER_X  / 5);

        prevSlide = (float)MyGLRenderer.SENSORE_ACCELEROMETER_X ;
        Matrix.translateM(mModelMatrix, 0, getPositionX(), -1f, 0);

        mTempMatrix =  mpvMatrix.clone();
        Matrix.multiplyMM( returnMatrix, 0, mTempMatrix, 0, mModelMatrix, 0 );

        return returnMatrix;
    }
}
