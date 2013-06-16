package vn.edu.hcmut.tachometer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;

public class ChartViewRenderer implements GLSurfaceView.Renderer {
	//private Triangle mTriangle;
	public Chart mChart;
	
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {

        // Set the background frame color
    	//GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
    	GLES20.glClearColor(0.2f, 0.2f, 0.2f, 1.0f);
    	
        //mTriangle = new Triangle();
    	//Log.e("onSurfaceCreated", "" + width + " " + height);
    	mChart = new Chart();
    }

    @Override
    public void onDrawFrame(GL10 unused) {

        // Draw background color
    	GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    	
    	GLES20.glLineWidth(1.5f);
    	unused.glEnable(GL10.GL_MULTISAMPLE);
    	//GLES20.glEnable(GLES20.GL_BLEND);
    	//GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    	//unused.glEnable(GL10.GL_LINE_SMOOTH); 
    	//unused.glHint(GL10.GL_LINE_SMOOTH_HINT, GL10.GL_NICEST);

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
    	//"attribute vec2 a_TexCoordinate;" +  // Per-vertex texture coordinate information we will pass in.
    	//"varying vec2 v_TexCoordinate;" +   // This will be passed into the fragment shader.
        "void main() {" +
        "	gl_Position = Projection * vPosition;" +
        //"	v_TexCoordinate = a_TexCoordinate;" +
        "}";

    private final String fragmentShaderCode =
        "precision mediump float;" +
        "uniform vec4 vColor;" +
        //"uniform sampler2D u_Texture;" +    // The input texture.
        //"varying vec2 v_TexCoordinate;" +	// Interpolated texture coordinate per fragment.
     	//"diffuse = diffuse * (1.0 / (1.0 + (0.10 * distance)));" +	// Add attenuation.
        //"diffuse = diffuse + 0.3;" +	// Add ambient lighting
        "void main() {" +
        "	gl_FragColor = vColor;" +
        //"	gl_FragColor = (v_Color * diffuse * texture2D(u_Texture, v_TexCoordinate));" +
        "}";

    private final FloatBuffer vertexBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;

    // number of coordinates per vertex in this array
    //static final int COORDS_PER_VERTEX = 3;
    static final int COORDS_PER_VERTEX = 2;
    /** How many bytes per float. */
	static final int BYTES_PER_FLOAT = 4;	
    /*static float triangleCoords[] = { // in counterclockwise order:
         0.0f,  0.622008459f, 0.0f,   // top
        -0.5f, -0.311004243f, 0.0f,   // bottom left
         0.5f, -0.311004243f, 0.0f    // bottom right
    };
    public float lineCoords[] = {
       0.0f, 0.0f,
       500.0f, 500.0f
    };*/
    
    int pivots = CONFIGURES_FOR_DEBUGGING_PURPOSE.pivots;
    public float lineCoords[];
    
    //private final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
    private final int vertexCount = 4 / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // bytes per vertex

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };
    //float color[] = { 1.0f, 1.0f, 0.0f, 1.0f };

    // For texture rendering
    /** Store our model data in a float buffer. */
    //private final FloatBuffer mCubeTextureCoordinates;
     
    /** This will be used to pass in the texture. */
    //private int mTextureUniformHandle;
     
    /** This will be used to pass in model texture coordinate information. */
    //private int mTextureCoordinateHandle;
     
    /** Size of the texture coordinate data in elements. */
    //private final int mTextureCoordinateDataSize = 2;
     
    /** This is a handle to our texture data. */
    //private int mTextureDataHandle;
    
	 // S, T (or X, Y)
	 // Texture coordinate data.
	 // Because images have a Y axis pointing downward (values increase as you move down the image) while
	 // OpenGL has a Y axis pointing upward, we adjust for that here by flipping the Y axis.
	 // What's more is that the texture coordinates are the same for every face.
    final float[] faceTextureCoordinateData =	{ // Just a square
		0.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 1.0f,
        1.0f, 0.0f
   	};
	 
    public void setSize(int width, int height)	{
    	this.width = width;
    	this.height = height;
    }
    
	public static int loadTexture(final Context context, final int resourceId) {
		final int[] textureHandle = new int[1];

		GLES20.glGenTextures(1, textureHandle, 0);

		if (textureHandle[0] != 0) {
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inScaled = false; // No pre-scaling

			// Read in the resource
			final Bitmap bitmap = BitmapFactory.decodeResource(
					context.getResources(), resourceId, options);

			// Bind to the texture in OpenGL
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

			// Set filtering
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
					GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
					GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

			// Load the bitmap into the bound texture.
			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

			// Recycle the bitmap, since its data has been loaded into OpenGL.
			bitmap.recycle();
		}

		if (textureHandle[0] == 0) {
			throw new RuntimeException("Error loading texture.");
		}

		return textureHandle[0];
	}
    
    public Chart() {
    	//Log.e("Chart", "" + width + " " + height);
    	// Update the temp result in 5 latest steps.
    	
    	Random rnd = new Random();
    	
    	lineCoords = new float[pivots];
    	for (int i = 0; i < lineCoords.length; i ++)	{
    		//lineCoords[i] = rnd.nextFloat() * 450.0f;
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
        
        // add the coordinates to the FloatBuffer
        //vertexBuffer.put(lineCoords);
        
        // set the buffer to read the first coordinate
        //vertexBuffer.position(0);
        //vertexBuffer.flip();

        //mCubeTextureCoordinates = ByteBuffer.allocateDirect(faceTextureCoordinateData.length * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
        //mCubeTextureCoordinates.put(faceTextureCoordinateData).position(0);	
        
        // prepare shaders and OpenGL program
        int vertexShader = ChartViewRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = ChartViewRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        
        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        //GLES20.glActiveTexture(GLES20.GL_TEXTURE0);		 // add the first texture in our list
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
        
        /*mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgram, "a_TexCoordinate");
        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);*/
        
        /*for (int i = 0; i < lineCoords.length; i += lineCoords.length/pivots)	{
        	android.util.Log.e("DRAW", Float.toString(lineCoords[i]));
        }*/
        
        for (int i = 0; i < pivots - 1; i ++)	{
        	vertexBuffer.clear();
        	vertexBuffer.put(new float[]{ i * width/(pivots-1), lineCoords[i], (i + 1) * width/(pivots-1), lineCoords[(i + 1) % lineCoords.length] });
        	
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
            
            /*mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram, "u_Texture");
            // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
            GLES20.glUniform1i(mTextureUniformHandle, 0);*/
            
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
