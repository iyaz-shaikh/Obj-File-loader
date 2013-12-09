package com.game;




import java.io.IOException;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.game.R;


import android.content.Context;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLES20;
import android.opengl.Matrix;
/**
 * Renderer for the Game GLSurfaceView
 * Some code taken from learnopengles.com
 * @author Iyaz
 *
 */
public class ObjRenderer implements Renderer{

	/**
	* Store the model matrix. This matrix is used to move models from 
	* object space (where each model can be thought
	* of being located at the center of the universe) to world space.
	*/
	private float[] mModelMatrix = new float[16];

	/**
	* Store the view matrix. This can be thought of as our camera. 
	* This matrix transforms world space to eye space;
	* it positions things relative to our eye.
	*/
	private float[] mViewMatrix = new float[16];

	/** Store the projection matrix. This is used to project the scene
	 *  onto a 2D viewport. */
	private float[] mProjectionMatrix = new float[16];
	
	private float[] mMVMatrix = new float[16];
	/** Allocate storage for the final combined matrix. This will be 
	 * passed into the shader program. */
	private float[] mMVPMatrix = new float[16];
	private ObjLoader loader;
	private final int bytesPerFloat = 4;
	private final int bytesPerShort = 2;
	/** These will be used to pass in the matrices. */
	private int mMVPMatrixHandle;
	private int mMVMatrixHandle;
	
	/** These will be used to pass in model information. */
	private int mPositionHandle;
	private int mLightPosHandle;
	private int mNormalHandle;
	private int mColorHandle;
	private int mTextureCoordsHandle;
	private int mTextureUniformHandle;
	
	private int mProgramHandle;
	private int mPointProgramHandle;
	private int mTextureDataHandle;
	

	/**
	* Stores a copy of the model matrix specifically for the light position.
	*/
	private float[] mLightModelMatrix = new float[16];	
	
	private int verticesPBuffer;
	private int verticesNBuffer;
	private int verticesTCBuffer;
	private int indicesPBuffer;
	private Context context;
	
	/** Used to hold a light centered on the origin in model space. We need a 4th coordinate so we can get translations to work when
	* we multiply this by our transformation matrices. */
	private final float[] mLightPosInModelSpace = new float[] {0.0f, 0.0f, 0.0f, 1.0f};

	/** Used to hold the current position of the light in world space (after transformation via model matrix). */
	private final float[] mLightPosInWorldSpace = new float[4];

	/** Used to hold the transformed position of the light in eye space (after transformation via modelview matrix) */
	private final float[] mLightPosInEyeSpace = new float[4];
	
	private final String pointVertexShader = 
		"uniform mat4 u_MVPMatrix; \n"
		+ "attribute vec4 a_Position; \n"
		+ "void main() \n"
		+ "{" 
		+ "gl_Position = u_MVPMatrix * a_Position; \n"
		+	"gl_PointSize = 5.0; \n"
		+ "} \n";
	
	private final String pointFragmentShader = 
		 "precision mediump float; 					\n"
       	+ "void main() 								\n"
		+ "{"
		+ "gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);  \n"
		+ "} 										\n";
	
	private final String vertexShader =
		"uniform mat4 u_MVPMatrix; 					\n" //Model-View-Projection Matrix.
		+ "uniform mat4 u_MVMatrix;					\n" //Model-View Matrix.
		
		+ "attribute vec3 a_Normal; 				\n" //Per-vertex Normal of the obj
		+ "attribute vec4 a_Position; 				\n"	// Per-vertex position information we will pass in.
		+ "attribute vec2 a_TextureCoords;          \n"
		+ "varying vec3 v_position;					\n"
		+ "varying vec3 v_normal;					\n"	
		+ "varying vec2 v_TextureCoords;            \n"
		
		
		+ "void main() 								\n" // The entry point for our vertex shader.
		+ "{"
		+ "v_TextureCoords = a_TextureCoords;       \n"
		+ "v_position = vec3(u_MVMatrix * a_Position);    \n"
		+ "v_normal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));        \n"
		+ "gl_Position = u_MVPMatrix * a_Position;  \n"
		+ "} 										\n"; 

	private final String fragmentShader =
		"precision mediump float; \n"	// Set the default precision to medium. We don't need as high of a
										// precision in the fragment shader.
		+ "uniform vec3 lightPos; \n"
		+ "uniform sampler2D u_Texture;				\n"

		+ "varying vec3 v_position; \n"	//passed in from the vertex shader.
		+ "varying vec3 v_normal; \n"
		+ "varying vec2 v_TextureCoords; \n"
		
		+ "void main() 			  \n"	// The entry point for our fragment shader.
		+ "{ 					  \n"
		+ "float distance = length(lightPos - v_position); \n"
		+ "vec3 lightVector = normalize(lightPos - v_position); \n"
		+ "float diffuse = max(dot(lightVector, v_normal), 0.1); \n"
		+ "diffuse = diffuse * (1.0 / (1.0 + (0.25 * distance * distance))); \n"
		+ " gl_FragColor = diffuse * texture2D(u_Texture, v_TextureCoords);\n"	// Pass the color directly through the pipeline.
		+ "} 					  \n";	



	
	/**
	 * Constructor for the GameRenderer. This includes an objloader so the game
	 * renderer can get data on models from the assets folder.
	 * @param loader
	 */
	public ObjRenderer(ObjLoader loader, Context context)
	{
		this.context = context;
		this.loader = loader;
		try {
			loader.parseObject("cube4.obj");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	



	@Override
	public void onSurfaceChanged(GL10 arg0, int width, int height) 
	{
		//Set the viewport to be the size of the screen.
		GLES20.glViewport(0, 0, width, height);


		// Create a new perspective projection matrix. The height will stay the same
		// while the width will vary as per aspect ratio.
		final float ratio = (float) width / height;
		final float left = -ratio;
		final float right = ratio;
		final float bottom = -1.0f;
		final float top = 1.0f;
		final float near = 1.0f;
		final float far = 10.0f;

		Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
		
	}

	@Override
	public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) 
	{
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		//generate the VBOS and IBOS for the vertices and indices
		final int buffers[] = new int[4];
		GLES20.glGenBuffers(4, buffers, 0);
		//The VBO generation
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[0]);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, loader.getModels().get(0).
				getVerticesPositions().capacity() * bytesPerFloat, 
				loader.getModels().get(0).getVerticesPositions(), 
				GLES20.GL_STATIC_DRAW);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[1]);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, loader.getModels().get(0).
				getVerticesNormals().capacity() * bytesPerFloat, 
				loader.getModels().get(0).getVerticesNormals(), 
				GLES20.GL_STATIC_DRAW);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[2]);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, loader.getModels().get(0).
				getVerticesColors().capacity() * bytesPerFloat, 
				loader.getModels().get(0).getVerticesColors(), 
				GLES20.GL_STATIC_DRAW);
		
		//The IBO generation
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, buffers[3]);
		GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, loader.getModels().get(0).
				getIndicesPositions().capacity() * bytesPerShort, 
				loader.getModels().get(0).getIndicesPositions(), 
				GLES20.GL_STATIC_DRAW);
		
		verticesPBuffer = buffers[0];
		verticesNBuffer = buffers[1];
		verticesTCBuffer = buffers[2];
		indicesPBuffer = buffers[3];
		
		
		// Position the eye behind the origin.
		final float eyeX = 0.0f;
		final float eyeY = 0.0f;
		final float eyeZ = 5.0f;

		// We are looking toward the distance
		final float lookX = 0.0f;
		final float lookY = 0.0f;
		final float lookZ = -5.0f;

		// Set our up vector. This is where our head would be pointing were we holding the camera.
		final float upX = 0.0f;
		final float upY = 1.0f;
		final float upZ = 0.0f;

		// Set the view matrix. This matrix can be said to represent the camera position.
		// NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
		// view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
		Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);		
		  
					
		final int vertexShaderHandle = GLHelper.loadShader(GLES20.GL_VERTEX_SHADER, 
			vertexShader);
		final int fragmentShaderHandle = GLHelper.loadShader(GLES20.GL_FRAGMENT_SHADER, 
			fragmentShader);
		mProgramHandle = GLHelper.createAndLinkProgram
			(vertexShaderHandle, fragmentShaderHandle, 
			new String[] {"a_Position", "a_Normal", 
			"a_TextureCoords"});
		
        final int pointVertexShaderHandle = GLHelper.loadShader(GLES20.GL_VERTEX_SHADER, pointVertexShader);
        final int pointFragmentShaderHandle = GLHelper.loadShader(GLES20.GL_FRAGMENT_SHADER, pointFragmentShader);
        mPointProgramHandle = GLHelper.createAndLinkProgram(pointVertexShaderHandle, pointFragmentShaderHandle,
         new String[] {"a_Position"}); 
        

		
		mTextureDataHandle = GLHelper.loadTexture(context, R.drawable.metal);
		
		GLES20.glEnable(GLES20.GL_CULL_FACE);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
	}
	
	@Override
	public void onDrawFrame(GL10 arg0) 
	{
		if (mProgramHandle != 0)
		{
			GLES20.glUseProgram(mProgramHandle);
		}
		else
		{
			throw new RuntimeException("Program isn't valid");
		}
		
        mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Position");
        mNormalHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Normal");
        mTextureCoordsHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_TextureCoords");
        mTextureUniformHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_Texture");
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVPMatrix");
        mMVMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVMatrix");
        mLightPosHandle = GLES20.glGetUniformLocation(mProgramHandle, "lightPos");
		
		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
		
		GLES20.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);
		
        // Set program handles. These will later be used to pass in values to the program.

        
        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        
        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);
        
        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0); 
        
        Matrix.setIdentityM(mLightModelMatrix, 0);
        Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, 2.0f);

               
        Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
        Matrix.multiplyMV(mLightPosInEyeSpace, 0, mViewMatrix, 0, mLightPosInWorldSpace, 0); 
        
        // Draw the triangle facing straight on.
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.rotateM(mModelMatrix, 0, 45.0f, 1.0f, 1.0f, 1.0f);  
        drawModel();
        
        // Draw a point to indicate the light.
        GLES20.glUseProgram(mPointProgramHandle);
        drawLight();
	}
	
	private void drawLight() 
	{
		final int pointMVPMatrixHandle = GLES20.glGetUniformLocation(mPointProgramHandle, "u_MVPMatrix");
        final int pointPositionHandle = GLES20.glGetAttribLocation(mPointProgramHandle, "a_Position");
        
        // Pass in the position.
        GLES20.glVertexAttrib3f(pointPositionHandle, mLightPosInModelSpace[0], mLightPosInModelSpace[1], mLightPosInModelSpace[2]);

        // Since we are not using a buffer object, disable vertex arrays for this attribute.
        GLES20.glDisableVertexAttribArray(pointPositionHandle);

        // Pass in the transformation matrix.
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mLightModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(pointMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        // Draw the point.
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1);
		
	}

	private void drawModel()
	{
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, verticesPBuffer);
		GLES20.glEnableVertexAttribArray(mPositionHandle);
		GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT,
				false, 0, 0);
		
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, verticesNBuffer);
		GLES20.glEnableVertexAttribArray(mNormalHandle);
		GLES20.glVertexAttribPointer(mNormalHandle, 3, GLES20.GL_FLOAT,
				false, 0, 0);
		
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, verticesTCBuffer);
		GLES20.glEnableVertexAttribArray(mTextureCoordsHandle);
		GLES20.glVertexAttribPointer(mTextureCoordsHandle, 2, GLES20.GL_FLOAT,
				false, 0, 0);
		
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indicesPBuffer);
        Matrix.multiplyMM(mMVMatrix, 0, mViewMatrix, 0, mModelMatrix, 0); 
        
		// This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVMatrix, 0); 
       
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVMatrix, 0);
		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
	
//         Pass in the light position in eye space.
        GLES20.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);
        
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 
        	loader.getModels().get(0).getIndicesPositions().capacity(), GLES20.GL_UNSIGNED_SHORT, 0); 
        
	}

}
