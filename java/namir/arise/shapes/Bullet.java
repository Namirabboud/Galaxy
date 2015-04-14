package namir.arise.shapes;

import android.content.Context;
import android.opengl.Matrix;

import namir.arise.R;

/**
 * Created by namir on 4/2/15.
 */
public class Bullet extends Texture{
    private static int ResourceId = R.drawable.bullet_2;
    private static float squareCoords[] = {
        -0.08f, 0.2f,
        -0.08f, -0.2f,
        0.08f, -0.2f,
        0.08f, 0.2f
    };

    public Bullet( final Context activityContext ){
        //instructor of the parent now bullet is a
        //texture that have the bullet shape
        super( activityContext, ResourceId, squareCoords );
    }

    public float[] generateMatrix( float[]  mpvMatrix ){
        float[] mModelMatrix    = new float[16];
        float[] mTempMatrix     = new float[16];
        float[] returnMatrix    = new float[16];

        Matrix.setIdentityM(mModelMatrix, 0);

        Matrix.translateM(mModelMatrix, 0, getPositionX(), getPositionY(), 0);
        this.setPositionY( getPositionY() + 0.1f );

        mTempMatrix =  mpvMatrix.clone();
        Matrix.multiplyMM( returnMatrix, 0, mTempMatrix, 0, mModelMatrix, 0 );

        return returnMatrix;
    }

}
