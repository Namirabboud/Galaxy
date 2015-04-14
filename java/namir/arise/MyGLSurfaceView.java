package namir.arise;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

import java.util.List;

import namir.arise.shapes.Bullets;

/**
 * Created by namir on 3/30/15.
 */
public class MyGLSurfaceView extends GLSurfaceView implements SensorEventListener {
    private final MyGLRenderer mRenderer;
    private SensorManager mSensorManager;

    private static final String TAG = "MyGlSurfaceView";

    //constructor
    public MyGLSurfaceView( Context context){
        super(context);

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        mRenderer = new MyGLRenderer( context );

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(mRenderer);

        //get instance of the SensorManager so that sensors can be registered/unregistered
        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);

        // Render the view only when there is a change in the drawing data
        //setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public boolean onTouchEvent( MotionEvent e){
        switch(e.getAction()){
            case MotionEvent.ACTION_DOWN:
                Bullets.addFire();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onSensorChanged( SensorEvent event ){
        if( event.sensor.getType() == Sensor.TYPE_ACCELEROMETER ){
            mRenderer.SENSORE_ACCELEROMETER_X = event.values[0];
            //requestRender();
        }
    }

    @Override
    public void onAccuracyChanged( Sensor sensor, int accuracy ){}

    public void startSensors(){
        //get the sensor list
        List<Sensor> listSensors = mSensorManager.getSensorList( Sensor.TYPE_ALL );

        //iterate through sensor list and activate desired sensor
        if( listSensors.size() > 0 ){
            for( int i = 0; i < listSensors.size(); i++ ){
                Sensor itemSensor = listSensors.get(i);

                switch ( itemSensor.getType() ){

                    case Sensor.TYPE_ACCELEROMETER:
                        mSensorManager.registerListener( this, itemSensor,
                                                        SensorManager.SENSOR_DELAY_GAME);
                        break;

                    default:
                        break;
                }
            }
        }
    }

    public void stopSensors(){
        mSensorManager.unregisterListener(this);
    }

}
