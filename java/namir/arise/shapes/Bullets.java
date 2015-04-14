package namir.arise.shapes;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by namir on 4/1/15.
 */
public class Bullets {
    private static float spaceShipX;
    private static int NUMBER_OF_BULLETS = 10;
    private static List<Bullet> bullets = new ArrayList<Bullet>();
    private static List<Bullet> Fire    = new ArrayList<Bullet>();
    private static int fire = 0;
    private static final String TAG = "Bullets";

    public Bullets( final Context activityContext ){
        for ( int i = 0; i < NUMBER_OF_BULLETS; i++ ) {
            Bullet bullet = new Bullet(activityContext);
            //set the position of the flame at start
            //to be equal to the ship
            //so that it seems that the ship is firing
            bullets.add(bullet);
        }
    }

    public void drawBullets( float[] mvpMatrix, Texture spaceShip ){
        for( int i = 0; i < Fire.size(); i++ ){
            if( Fire.get(i).getPositionY() > 3.0f )
                Fire.remove(i);
            else Fire.get(i).draw( mvpMatrix );

        }
        spaceShipX = spaceShip.getPositionX();
    }

    public static void addFire(){
        if( fire >= NUMBER_OF_BULLETS )
            fire = 0;
        bullets.get(fire).setPositionX(spaceShipX);
        bullets.get(fire).setPositionY(0);
        Fire.add( bullets.get(fire) );
        fire++;
    }

    public static float getBulletsSize(){
        return bullets.size();
    }

    public static List<Bullet> getBullets(){
        return Fire;
    }

    public static void remove( int index ){
        Fire.remove(index);
    }

}
