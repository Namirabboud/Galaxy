package namir.arise;

import android.content.Context;
import android.content.SharedPreferences;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import namir.arise.shapes.Bullet;
import namir.arise.shapes.Bullets;
import namir.arise.shapes.GlText_trial.GL_Text;
import namir.arise.shapes.GlText_trial.TextObject;
import namir.arise.shapes.Obstacle;
import namir.arise.shapes.Obstacles;
import namir.arise.shapes.SpaceShip;

/**
 * Created by namir on 3/30/15.
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {
    private final Context mActivityContext;

    //private Triangle mTriangle;
    private SpaceShip spaceShip;
    private Bullets mBullets;
    private Obstacles mObstacles;
    private GL_Text trial_1;
    private TextObject Score;
    private TextObject txt;

    private int score = 0;
    private boolean kill = false;

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];

    public static double SENSORE_ACCELEROMETER_X = 0.0;


    private static final String TAG = "MyGLRenderer";

    //constructor
    public MyGLRenderer(final Context activityContext){
        mActivityContext = activityContext;
    }

    public void onSurfaceCreated( GL10 unused, EGLConfig config){
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        spaceShip   = new SpaceShip(mActivityContext);
        mBullets    = new Bullets(mActivityContext);
        mObstacles  = new Obstacles(mActivityContext);
        trial_1     = new GL_Text( mActivityContext );
        Score       = new TextObject( String.valueOf(score), -1.0f, 1.8f );
        txt         = new TextObject( "score:", 1.0f, 1.8f );
        trial_1.addText( txt );
        trial_1.addText(Score);
        trial_1.prepareToDraw();

        SharedPreferences sharedPref = mActivityContext.getSharedPreferences("LevelScores",
                Context.MODE_PRIVATE);

    }

    public void onDrawFrame(GL10 unused) {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -7, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);


        if( kill != true ){
            spaceShip.draw(mMVPMatrix);
            mObstacles.drawObstacles(mMVPMatrix);
            mBullets.drawBullets(mMVPMatrix, spaceShip);
            checkCollision();
        }

        trial_1.draw(mMVPMatrix);

    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {

        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);

    }

    public static int loadShader(int type, String shaderCode){
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    private void checkCollision(){
        /*
        about checkCollision:
        obstacles whether it is being hit by a the gun, the obstacle
        will be removed and the bullet as well

        or the obstacle will hit the spaceship
        and you will loose life.
         */
        List<Obstacle> obstacles = mObstacles.getDisplayed();
        List<Bullet> bullets     = Bullets.getBullets();

        for( int i = 0; i < obstacles.size(); i++ ){
            for( int j = 0; j < bullets.size(); j++ ){
                if( Math.abs(bullets.get(j).getPositionY() - obstacles.get(i).getPositionY()) < 0.3f
                    && Math.abs( bullets.get(j).getPositionX() - obstacles.get(i).getPositionX() ) < 0.3f )
                {
                    //when bullet hit obstacle
                    mObstacles.remove(obstacles.indexOf( obstacles.get(i) ));
                    Bullets.remove(bullets.indexOf( bullets.get(j) ));
                    score ++;
                    Score.text = String.valueOf(score);
                    trial_1.prepareToDraw();
                    return;
                }
            }

            //check if obstacle hits the
            if( Math.abs( obstacles.get(i).getPositionX() - spaceShip.getPositionX() ) < 0.2f
            && Math.abs( obstacles.get(i).getPositionY() - (spaceShip.getPositionY() - 0.4) ) < 0.01f ){
                //check the high score
                SharedPreferences prefs = mActivityContext.getSharedPreferences("myPrefsKey", Context.MODE_PRIVATE);
                int oldScore = prefs.getInt("key", 0);
                if( score > oldScore ){
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putInt("key", score);
                    edit.commit();
                }
                kill = true;
                trial_1.removeText( txt );
                trial_1.removeText( Score );
                TextObject gameOver = new TextObject( "akal", 1f, 1f );
                TextObject highScore = new TextObject( String.valueOf( "highscore:" + prefs.getInt( "key", 0 ) ),1f,1.5f );
                trial_1.addText( highScore );
                trial_1.addText( gameOver );
                trial_1.prepareToDraw();
            }

        }
    }

}
