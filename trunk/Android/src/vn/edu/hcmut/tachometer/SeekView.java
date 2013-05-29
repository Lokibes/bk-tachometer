package vn.edu.hcmut.tachometer;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.View;

public class SeekView extends GLSurfaceView {
	private SeekViewRenderer myRenderer;
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		//super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		int width = View.MeasureSpec.getSize(widthMeasureSpec);
		int height = View.MeasureSpec.getSize(heightMeasureSpec);
		
		//Log.e("onMeasure", width + " " + height);
		
	    setMeasuredDimension(width, height);
	    //android.util.Log.e("HEIGHT", "" + height);
	}
	
	public SeekView(Context context) {
		super(context);
		
        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);

        // Set the Renderer for drawing on the GLSurfaceView
        //Log.e("ChartView", "" + this.getWidth() + " " + this.getHeight());
        // Width, height are not available yet (context is uncompleted)
        myRenderer = new SeekViewRenderer();
        setRenderer(myRenderer);
        //setRenderer(null);

        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}

	
	public SeekView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);

        // Set the Renderer for drawing on the GLSurfaceView
        myRenderer = new SeekViewRenderer();
        setRenderer(myRenderer);

        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}
	
	public void drawLine(float x, float y)	{
		myRenderer.mChart.lineCoords[(int) x] = y;
	}
}
