package com.game;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Contains all of the data for a .obj File
 * @author Iyaz
 *
 */
public class Model {
	
	private ShortBuffer indicesPositions;
	private FloatBuffer verticesPositions;
	private FloatBuffer verticesNormals;
	private FloatBuffer verticesColors;
	private String filename;
	
	
	public Model(String filename, ShortBuffer indicesPositions,
			FloatBuffer verticesPositions, FloatBuffer
			verticesNormals, FloatBuffer verticesColors)
	{
		this(filename, indicesPositions, verticesPositions, verticesNormals);
		this.verticesColors = verticesColors;
		
	}
	
	public Model(String filename, ShortBuffer indicesPositions,
			FloatBuffer verticesPositions, FloatBuffer
			verticesNormals)
	{
		this.filename = filename;
		this.verticesPositions = verticesPositions;
		this.verticesNormals = verticesNormals;
		this.indicesPositions = indicesPositions;
	}
	
	public FloatBuffer getVerticesPositions()
	{
		return this.verticesPositions;
	}
	
	public ShortBuffer getIndicesPositions()
	{
		return this.indicesPositions;
	}
	
	public FloatBuffer getVerticesNormals()
	{
		return this.verticesNormals;
	}
	
	public FloatBuffer getVerticesColors()
	{
		return this.verticesColors;
	}
	
	
	public String getFilename()
	{
		return this.filename;
	}

}
