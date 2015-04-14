package namir.arise.shapes;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by namir on 4/4/15.
 */
public class Obstacles {
    private static int NUMBER_OF_OBSTICALS = 10;
    private  List<Obstacle> obsticals = new ArrayList<Obstacle>();
    private  List<Obstacle> displayed = new ArrayList<Obstacle>();

    private static final String TAG = "MyGLRenderer";

    public Obstacles(final Context activityContext){
        for( int i = 0; i < NUMBER_OF_OBSTICALS; i++ ){
            Obstacle obstical = new Obstacle( activityContext );
            obsticals.add( obstical );
        }
    }

    public void drawObstacles( float[] mvpMatrix ){
        if( displayed.size() == 0 ){
            Obstacle.speedUp();
            float positionY = 3.0f;
            for( int i = 0; i < obsticals.size(); i++ ){
                //put the obstacles in the displayed array
                //each have a different coordination then
                //the previous one
                displayed.add(obsticals.get(i));
                displayed.get(i).setPositionX( randFloat( 1.3f, -1.3f ) );
                displayed.get(i).setPositionY( positionY );
                positionY += 1.5f;
            }
        }
        for( int i = 0; i < displayed.size(); i++ ){
            if( displayed.get(i).getPositionY() < -2.0f )
                displayed.remove(i);
            else displayed.get(i).draw(mvpMatrix);
        }
    }

    private float randFloat( float max, float min ) {
        Random rand = new Random();
        float fin = rand.nextFloat() * (max - min) + min;
        return fin;
    }

    public List<Obstacle> getDisplayed(){
        return this.displayed;
    }

    public void remove( int index ){
        displayed.remove( index );
    }
}
