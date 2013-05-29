package vn.edu.hcmut.tachometer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

public class ChartViewRenderer implements GLSurfaceView.Renderer {
	//private Triangle mTriangle;
	public Chart mChart;
	
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {

        // Set the background frame color
    	GLES20.glClearColor(0.2f, 0.2f, 0.2f, 1.0f);
    	
        //mTriangle = new Triangle();
    	//Log.e("onSurfaceCreated", "" + width + " " + height);
    	mChart = new Chart();
    }

    @Override
    public void onDrawFrame(GL10 unused) {

        // Draw background color
    	GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // Draw
        //mTriangle.draw();
    	//Log.e("onDrawFrame", "" + width + " " + height);
    	//mChart.applyOrtho(0, width, 0, height, 1, -1);
    	mChart.draw();
    }
    
    @Override
    // This is the first function that context is created completely,
    // hence the width, height are available to use
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
    	//GLES20.glViewport(0, 0, width, height);
    	//Log.e("onSurfaceChanged", "" + width + " " + height);
    	mChart.setSize(width, height);
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

}

class Chart	{
	private int width, height = 0;
	
    private final String vertexShaderCode =
    	"uniform mat4 Projection;" +
        "attribute vec4 vPosition;" +
        "void main() {" +
        "  gl_Position = Projection * vPosition;" +
        "}";

    private final String fragmentShaderCode =
        "precision mediump float;" +
        "uniform vec4 vColor;" +
        "void main() {" +
        "  gl_FragColor = vColor;" +
        "}";

    private final FloatBuffer vertexBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;

    // number of coordinates per vertex in this array
    //static final int COORDS_PER_VERTEX = 3;
    static final int COORDS_PER_VERTEX = 2;
    /*static float triangleCoords[] = { // in counterclockwise order:
         0.0f,  0.622008459f, 0.0f,   // top
        -0.5f, -0.311004243f, 0.0f,   // bottom left
         0.5f, -0.311004243f, 0.0f    // bottom right
    };
    public float lineCoords[] = {
       0.0f, 0.0f,
       500.0f, 500.0f
    };*/
    
    public float lineCoords[];
    
    //private final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
    private final int vertexCount = 4 / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // bytes per vertex

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };

    public void setSize(int width, int height)	{
    	this.width = width;
    	this.height = height;
    }
    
    public Chart() {
    	//Log.e("Chart", "" + width + " " + height);
    	lineCoords = new float[1000];
    	for (int i = 0; i < lineCoords.length; i ++)	{
    		lineCoords[i] = 0.0f;
    	}
    	
        // initialize vertex byte buffer for shape coordinates
    	// (number of coordinate values * 4 bytes per float)
        //ByteBuffer bb = ByteBuffer.allocateDirect(lineCoords.length * 4);
    	ByteBuffer bb = ByteBuffer.allocateDirect(4 * 4);
    	
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer();
        
//        // add the coordinates to the FloatBuffer
//        vertexBuffer.put(lineCoords);
//        
//        // set the buffer to read the first coordinate
//        //vertexBuffer.position(0);
//        vertexBuffer.flip();

        // prepare shaders and OpenGL program
        int vertexShader = ChartViewRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = ChartViewRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        
        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables
    }

    //GLES20.glOrthof(-width/2, width/2, -height/2, height/2, 1, -1);
    public void applyOrtho(float left, float right, float bottom, float top, float near, float far)	{
        float a = 2.0f / (right - left);
        float b = 2.0f / (top - bottom);
        float c = -2.0f / (far - near);

        float tx = - (right + left)/(right - left);
        float ty = - (top + bottom)/(top - bottom);
        float tz = - (far + near)/(far - near);

        float ortho[] = {
            a,	0,	0,	0,
            0,	b,	0,	0,
            0,	0,	c,	0,
            tx,	ty,	tz,	1
        };
        FloatBuffer fb = FloatBuffer.allocate(ortho.length);
        fb.put(ortho);
        fb.flip();

        int projectionUniform = GLES20.glGetUniformLocation(mProgram, "Projection");
        GLES20.glUniformMatrix4fv(projectionUniform, 1, false, fb);
    }
    
    public void draw() {
    	//Log.e("draw", "" + width + " " + height);
    	applyOrtho(0, width, 0, height, 1, -1);
    	
        // Add program to OpenGL environment
    	GLES20.glUseProgram(mProgram);
    	
        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        
        for (int i = 0; i < lineCoords.length; i ++)	{
        	vertexBuffer.clear();
        	vertexBuffer.put(new float[]{ i, 250 - lineCoords[i], i, 250 + lineCoords[i] });
        	/*if (i == 0)	{
        		vertexBuffer.clear();
        		vertexBuffer.put(new float[]{ i, 0, i, 500 });
        	}*/
            
            vertexBuffer.flip();
            
            // Prepare the triangle coordinate data
            GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                                         GLES20.GL_FLOAT, false,
                                         vertexStride, vertexBuffer);

            // get handle to fragment shader's vColor member
            mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

            // Set color for drawing the triangle
            GLES20.glUniform4fv(mColorHandle, 1, color, 0);
            
            // Draw the triangle
            GLES20.glDrawArrays(GLES20.GL_LINES, 0, vertexCount);
        }
        
        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}

class Triangle {

    private final String vertexShaderCode =
        "attribute vec4 vPosition;" +
        "void main() {" +
        "  gl_Position = vPosition;" +
        "}";

    private final String fragmentShaderCode =
        "precision mediump float;" +
        "uniform vec4 vColor;" +
        "void main() {" +
        "  gl_FragColor = vColor;" +
        "}";

    private final FloatBuffer vertexBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static float triangleCoords[] = { // in counterclockwise order:
         0.0f,  0.622008459f, 0.0f,   // top
        -0.5f, -0.311004243f, 0.0f,   // bottom left
         0.5f, -0.311004243f, 0.0f    // bottom right
    };
    private final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // bytes per vertex

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };

    public Triangle() {
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                triangleCoords.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        vertexBuffer.put(triangleCoords);
        // set the buffer to read the first coordinate
        vertexBuffer.position(0);

        // prepare shaders and OpenGL program
        int vertexShader = ChartViewRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = ChartViewRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables
    }

    public void draw() {
        // Add program to OpenGL environment
    	GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                                     GLES20.GL_FLOAT, false,
                                     vertexStride, vertexBuffer);

        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}
