package com.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

public class GLHelper {
	
	/**
	 * Handles all the GLES20 calls to load and compile a vertexShader
	 * Some code taken from learnopengles.com
	 * @param shader
	 * @return
	 */
	public static int loadShader(final int typeOfShader, final String shader)
	{
		if (typeOfShader == GLES20.GL_VERTEX_SHADER)
		{
			int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
			
			if (vertexShaderHandle != 0)
			{
				//Pass in the shader source.
				GLES20.glShaderSource(vertexShaderHandle, shader);
				
				//Compile the shader.
				GLES20.glCompileShader(vertexShaderHandle);
				//Get the compile status, if it remains 0, terminate the shader.
				int[] compileStatus = {0};
				GLES20.glGetShaderiv(vertexShaderHandle, GLES20.GL_COMPILE_STATUS,
						compileStatus, 0);
				
				if (compileStatus[0] == 0)
				{
					GLES20.glDeleteShader(vertexShaderHandle);
					vertexShaderHandle = 0;
				}
			}
			//If the shader didn't get created properly, throw an exception.
			if (vertexShaderHandle == 0)
			{
				throw new RuntimeException("The Vertex shader didn't get created");
			}
			
			return vertexShaderHandle;
		}
		else if(typeOfShader == GLES20.GL_FRAGMENT_SHADER)
		{
			int fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
			
			//Create and compile the shader source.
			if (fragmentShaderHandle != 0)
			{
				//Pass in the shader source.
				GLES20.glShaderSource(fragmentShaderHandle, shader);
				//Compile the shader.
				GLES20.glCompileShader(fragmentShaderHandle);
				//Get compile status. If it's 0, terminate the shader.
				int[] compileStatus = {0};
				GLES20.glGetShaderiv(fragmentShaderHandle, GLES20.GL_COMPILE_STATUS,
						compileStatus, 0);
				if (compileStatus[0] == 0)
				{
					GLES20.glDeleteShader(fragmentShaderHandle);
					fragmentShaderHandle = 0;
				}
			}
			return fragmentShaderHandle;
		}
		return 0;
	}
	
	public static int createAndLinkProgram(final int vertexShaderHandle, final int fragmentShaderHandle, final String[] attributes)
	{
		int programHandle = GLES20.glCreateProgram();
	
		if (programHandle != 0)
		{
		// Bind the vertex shader to the program.
			GLES20.glAttachShader(programHandle, vertexShaderHandle);	
	
		// Bind the fragment shader to the program.
			GLES20.glAttachShader(programHandle, fragmentShaderHandle);
	
		// Bind attributes
		if (attributes != null)
		{
			final int size = attributes.length;
			for (int i = 0; i < size; i++)
			{
				GLES20.glBindAttribLocation(programHandle, i, attributes[i]);
			}	
		}
	
		// Link the two shaders together into a program.
		GLES20.glLinkProgram(programHandle);
	
		// Get the link status.
		final int[] linkStatus = new int[1];
		GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);
	
		// If the link failed, delete the program.
		if (linkStatus[0] == 0)
		{	
			GLES20.glDeleteProgram(programHandle);
			programHandle = 0;
		}
		}
	
		if (programHandle == 0)
		{
			throw new RuntimeException("Error creating program.");
		}
	
		return programHandle;
	}
	
	public static int loadTexture(final Context context, final int resourceID)
	{
		
		final int textureHandle[] = new int[1];
		
		GLES20.glGenTextures(1, textureHandle, 0);
		

		if (textureHandle[0] != 0)
		{
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inScaled = false;
			
			final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), 
				resourceID, options);
			
			bitmap.getConfig();
			int bitmapFormat = bitmap.getConfig() == Config.RGB_565 ? GLES20.GL_RGBA : GLES20.GL_RGB;
			
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
	        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
	        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
	        
	        // Load the bitmap into the bound texture.
	        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
	 
	        // Recycle the bitmap, since its data has been loaded into OpenGL.
	        bitmap.recycle();
	    }
	 
	    if (textureHandle[0] == 0)
	    {
	        throw new RuntimeException("Error loading texture.");
	    }
		
		return textureHandle[0];
	}
}
