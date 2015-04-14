package namir.arise;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;


public class MainActivity extends ActionBarActivity {
    private MyGLSurfaceView mGLView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        mGLView = new MyGLSurfaceView(this);
        setContentView(mGLView);
    }

    @Override
    protected void onPause(){
        super.onPause();
        mGLView.stopSensors();
        mGLView.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        mGLView.onResume();
        mGLView.startSensors();
    }


}
