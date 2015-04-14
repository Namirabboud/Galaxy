package namir.arise.shapes;

import android.content.Context;
import android.opengl.Matrix;

import namir.arise.R;

/**
 * Created by namir on 4/4/15.
 */
public class Obstacle extends Texture {
    private static int resourceId = R.drawable.fireball;
    private static float translation ;
    private static float squareCoords[] = {
        -0.5f, 0.5f,    //top left
        -0.5f, -0.5f,   //bottom left
        0.5f, -0.5f,    //bottom right
        0.5f, 0.5f,     //top right
    };

    public Obstacle(final Context activityContext){
        super( activityContext, resourceId, squareCoords );
        setPositionY( 3.0f );
        translation = 0f;
    }

    public static void speedUp(){
        translation += 0.01;
    }

    public float[] generateMatrix( float[]  mpvMatrix ){
        float[] mModelMatrix    = new float[16];
        float[] mTempMatrix     = new float[16];
        float[] returnMatrix    = new float[16];

        Matrix.setIdentityM(mModelMatrix, 0);

        Matrix.translateM(mModelMatrix, 0, getPositionX(), getPositionY(), 0);
        setPositionY( getPositionY() - translation );


        mTempMatrix =  mpvMatrix.clone();
        Matrix.multiplyMM( returnMatrix, 0, mTempMatrix, 0, mModelMatrix, 0 );

        return returnMatrix;
    }
}
