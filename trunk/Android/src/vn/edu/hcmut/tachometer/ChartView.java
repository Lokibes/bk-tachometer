package vn.edu.hcmut.tachometer;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.View;

public class ChartView extends GLSurfaceView {
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		//super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		int width = View.MeasureSpec.getSize(widthMeasureSpec);
		int height = View.MeasureSpec.getSize(heightMeasureSpec);
		
		//Log.e("onMeasure", width + " " + height);
		
	    setMeasuredDimension(width, width);
	}
	
	public ChartView(Context context) {
		super(context);
		
        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);

        // Set the Renderer for drawing on the GLSurfaceView
        //Log.e("ChartView", "" + this.getWidth() + " " + this.getHeight());
        // Width, height are not available yet (context is uncompleted)
        setRenderer(new ChartViewRenderer());
        //setRenderer(null);

        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}

	
	public ChartView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(new ChartViewRenderer());

        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}
}
