package com.game;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.StringTokenizer;

import android.content.res.AssetManager;

/**
 * Class that handles model data from an .obj  file
 * This class can only parse .obj files with vt, vn, f and v coordinates.
 * It will break otherwise.
 * @author Iyaz Shaikh
 *
 */
 
public class ObjLoader {
	
	private final String VERTEX = "v ";
	private final String FACE = "f";
	private final String TEXTURE = "vt";
	private final String NORMAL = "vn";
	private BufferedReader fileReader; 
	private float[] vertices;
	private short[] indicesVertices;
	private float[] normals;
	private float[] colors;
	private ArrayList <Float> Vpositions = new ArrayList <Float> ();
	private ArrayList <Float> Vnormals = new ArrayList <Float> ();
	private ArrayList <Float> Vcolors = new ArrayList <Float> ();
	private ArrayList <Short> Ipositions = new ArrayList <Short> ();
	private ArrayList <Short> Inormals = new ArrayList <Short> ();
	private ArrayList <Short> Icolors = new ArrayList <Short> ();
	private ArrayList <Vertex> deIndexedVertices = new ArrayList <Vertex> ();
	private ArrayList <Short> indices = new ArrayList <Short>();
	private StringTokenizer scanner; 
	private ArrayList <Model> collectionOfModels;
	private AssetManager assets;
	/**
	 * Constructor for the data of a model
	 * @param filename
	 * @throws IOException
	 */

	
	public ObjLoader(AssetManager assets) throws IOException
	{
		//instantiate the variables
		
		this.assets = assets; 
		collectionOfModels = new ArrayList <Model> ();
	}
	
	/**
	 * parses an obj file without textures
	 */
	public void parseObjectNoTextures(String filename) throws IOException
	{
		FloatBuffer verticesPBuffer;
		FloatBuffer verticesNBuffer;
		FloatBuffer verticesTCBuffer;
		ShortBuffer indicesPBuffer;
		
		
		InputStream filein = assets.open(filename);
		fileReader = new BufferedReader(new InputStreamReader(filein));
		//Read from the file
		String line = "";
		line = fileReader.readLine();
		
		
		while(line != null)
		{
			if (line.startsWith(VERTEX))
			{
				//Store the vertex in the proper array and continue.
				processVertex(line);
				line = fileReader.readLine();
			}
			else if (line.startsWith(NORMAL))
			{
				processNormal(line);
				line = fileReader.readLine();
			}
			else if (line.startsWith(FACE))
			{
				//Store the face in the proper array and continue
				processFace(line);
				line = fileReader.readLine();
			}
			else
			{
				line = fileReader.readLine();
			}
		}
		
		try {
			fileReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
		if (Vpositions.size() == 0 || Vnormals.size() == 0)
		{
			return;
		}
		short maxIndex = 0;
		//Deindexing the vertices
		for (int i = 0; i < Ipositions.size(); i++)
		{
			Vertex vertex = new Vertex();
			vertex.positionX = Vpositions.get(Ipositions.get(i).intValue() * 3).floatValue();
			vertex.positionY = Vpositions.get((Ipositions.get(i).intValue() * 3) + 1).floatValue();
			vertex.positionZ = Vpositions.get((Ipositions.get(i).intValue() * 3) + 2).floatValue();
			vertex.normalX = Vnormals.get(Inormals.get(i).intValue() * 3).floatValue();
			vertex.normalY = Vnormals.get((Inormals.get(i).intValue() * 3) + 1).floatValue();
			vertex.normalZ = Vnormals.get((Inormals.get(i).intValue() * 3) + 2).floatValue();
			boolean isContained = false;
			int index = 0; //if the deIndexedVertices contains the vertex, then
						   //we can simply take the index of where it is and
						   //put it in the indices again.
			for (int k = 0; k < deIndexedVertices.size(); k++)
			{
				//Search the deIndexed vertices for the vertex.
				if (vertex.equals(deIndexedVertices.get(k)))
				{
					isContained = true;
					index = k;
				}
			}
			//If the vertex isn't contained, then make a new one
			//in the VBO and expand the maxIndex.
			if (!isContained)
			{
				deIndexedVertices.add(vertex);
				indices.add(maxIndex);
				maxIndex++;
			}
			else
			{
				//The vertex is contained, and all we need to do is
				//add the index of it to the indices arraylist.
				indices.add((short) index);
			}
		}
		
		vertices = new float[deIndexedVertices.size() * 3];
		normals = new float[deIndexedVertices.size() * 3];
		indicesVertices = new short[indices.size()];
		int positionCount = 0;
		
		//Get all the data into the proper arrays.
		for (int i = 0; i < deIndexedVertices.size(); i++)
		{
			vertices[positionCount] = deIndexedVertices.get(i).positionX;
			vertices[positionCount + 1] = deIndexedVertices.get(i).positionY;
			vertices[positionCount + 2] = deIndexedVertices.get(i).positionZ;
			normals[positionCount] = deIndexedVertices.get(i).normalX;
			normals[positionCount + 1] = deIndexedVertices.get(i).normalY;
			normals[positionCount + 2] = deIndexedVertices.get(i).normalZ;
			positionCount = positionCount + 3;
		}
		
		for (int i = 0; i < indices.size(); i++)
		{
			indicesVertices[i] = indices.get(i).shortValue();
		}

		
		//Make the buffers for the positions, normals, and texture colors
		verticesPBuffer = ByteBuffer.allocateDirect(vertices.length * 4).order
			(ByteOrder.nativeOrder()).asFloatBuffer();
		verticesPBuffer.position(0);
		verticesPBuffer.put(vertices);
		verticesPBuffer.position(0);
		
		verticesNBuffer = ByteBuffer.allocateDirect(normals.length * 4).order
			(ByteOrder.nativeOrder()).asFloatBuffer();
		verticesNBuffer.position(0);
		verticesNBuffer.put(normals);
		verticesNBuffer.position(0);


		//Do the same for the indices.
		indicesPBuffer = ByteBuffer.allocateDirect(indicesVertices.length * 2).order
		(ByteOrder.nativeOrder()).asShortBuffer();
		indicesPBuffer.position(0);
		indicesPBuffer.put(indicesVertices);
		indicesPBuffer.position(0);

		
		collectionOfModels.add(new Model(filename, indicesPBuffer, verticesPBuffer, verticesNBuffer));
	}
	
	/**
	 * parses the obj file
	 * @throws IOException 
	 */
	public void parseObject(String filename) throws IOException
	{

		FloatBuffer verticesPBuffer;
		FloatBuffer verticesNBuffer;
		FloatBuffer verticesTCBuffer;
		ShortBuffer indicesPBuffer;
		
		
		InputStream filein = assets.open(filename);
		fileReader = new BufferedReader(new InputStreamReader(filein));
		//Read from the file
		String line = "";
		line = fileReader.readLine();
		
		
		while(line != null)
		{
			if (line.startsWith(VERTEX))
			{
				//Store the vertex in the proper array and continue.
				processVertex(line);
				line = fileReader.readLine();
			}
			else if (line.startsWith(NORMAL))
			{
				processNormal(line);
				line = fileReader.readLine();
			}
			else if (line.startsWith(FACE))
			{
				//Store the face in the proper array and continue
				processFace(line);
				line = fileReader.readLine();
			}
			else if (line.startsWith(TEXTURE))
			{
				processColor(line);
				line = fileReader.readLine();
			}
			else
			{
				line = fileReader.readLine();
			}
		}
		
		try {
			fileReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
		if (Vpositions.size() == 0 || Vnormals.size() == 0 || Vcolors.size() == 0)
		{
			return;
		}
		short maxIndex = 0;
		//Deindexing the vertices
		for (int i = 0; i < Ipositions.size(); i++)
		{
			Vertex vertex = new Vertex();
			vertex.positionX = Vpositions.get(Ipositions.get(i).intValue() * 3).floatValue();
			vertex.positionY = Vpositions.get((Ipositions.get(i).intValue() * 3) + 1).floatValue();
			vertex.positionZ = Vpositions.get((Ipositions.get(i).intValue() * 3) + 2).floatValue();
			vertex.normalX = Vnormals.get(Inormals.get(i).intValue() * 3).floatValue();
			vertex.normalY = Vnormals.get((Inormals.get(i).intValue() * 3) + 1).floatValue();
			vertex.normalZ = Vnormals.get((Inormals.get(i).intValue() * 3) + 2).floatValue();
			vertex.texture1 = Vcolors.get(Icolors.get(i).intValue() * 2).floatValue();
			vertex.texture2 = Vcolors.get((Icolors.get(i).intValue() * 2) + 1).floatValue();
			boolean isContained = false;
			int index = 0; //if the deIndexedVertices contains the vertex, then
						   //we can simply take the index of where it is and
						   //put it in the indices again.
			for (int k = 0; k < deIndexedVertices.size(); k++)
			{
				//Search the deIndexed vertices for the vertex.
				if (vertex.equals(deIndexedVertices.get(k)))
				{
					isContained = true;
					index = k;
				}
			}
			//If the vertex isn't contained, then make a new one
			//in the VBO and expand the maxIndex.
			if (!isContained)
			{
				deIndexedVertices.add(vertex);
				indices.add(maxIndex);
				maxIndex++;
			}
			else
			{
				//The vertex is contained, and all we need to do is
				//add the index of it to the indices arraylist.
				indices.add((short) index);
			}
		}
		
		vertices = new float[deIndexedVertices.size() * 3];
		normals = new float[deIndexedVertices.size() * 3];
		colors = new float[deIndexedVertices.size() * 2];
		indicesVertices = new short[indices.size()];
		int positionCount = 0;
		int colorCount = 0;
		
		//Get all the data into the proper arrays.
		for (int i = 0; i < deIndexedVertices.size(); i++)
		{
			vertices[positionCount] = deIndexedVertices.get(i).positionX;
			vertices[positionCount + 1] = deIndexedVertices.get(i).positionY;
			vertices[positionCount + 2] = deIndexedVertices.get(i).positionZ;
			normals[positionCount] = deIndexedVertices.get(i).normalX;
			normals[positionCount + 1] = deIndexedVertices.get(i).normalY;
			normals[positionCount + 2] = deIndexedVertices.get(i).normalZ;
			colors[colorCount] = deIndexedVertices.get(i).texture1;
			colors[colorCount + 1] = deIndexedVertices.get(i).texture2;
			positionCount = positionCount + 3;
			colorCount = colorCount + 2;
		}
		
		for (int i = 0; i < indices.size(); i++)
		{
			indicesVertices[i] = indices.get(i).shortValue();
		}

		
		//Make the buffers for the positions, normals, and texture colors
		verticesPBuffer = ByteBuffer.allocateDirect(vertices.length * 4).order
			(ByteOrder.nativeOrder()).asFloatBuffer();
		verticesPBuffer.position(0);
		verticesPBuffer.put(vertices);
		verticesPBuffer.position(0);
		
		verticesNBuffer = ByteBuffer.allocateDirect(normals.length * 4).order
			(ByteOrder.nativeOrder()).asFloatBuffer();
		verticesNBuffer.position(0);
		verticesNBuffer.put(normals);
		verticesNBuffer.position(0);

		verticesTCBuffer = ByteBuffer.allocateDirect(colors.length * 4).order
			(ByteOrder.nativeOrder()).asFloatBuffer();
		verticesTCBuffer.position(0);
		verticesTCBuffer.put(colors);
		verticesTCBuffer.position(0);

		//Do the same for the indices.
		indicesPBuffer = ByteBuffer.allocateDirect(indicesVertices.length * 2).order
		(ByteOrder.nativeOrder()).asShortBuffer();
		indicesPBuffer.position(0);
		indicesPBuffer.put(indicesVertices);
		indicesPBuffer.position(0);

		
		collectionOfModels.add(new Model(filename, indicesPBuffer, verticesPBuffer, verticesNBuffer, 
			verticesTCBuffer));
		
	}
	
	
	/**
	 * Processes a line of text and gets the vertices out of it
	 * @param vertex
	 */
	public void processVertex(String vertex)
	{
		scanner = new StringTokenizer(vertex);
		//Scan the string for any vertices
		String element = scanner.nextToken();
		while(scanner.hasMoreTokens())
		{
			element = scanner.nextToken();
			Vpositions.add(Float.valueOf(element));
		}
	}	
	/**
	 * Parses the string for the vertex colors and adds them to the 
	 * color array
	 * @param vertex
	 */
	public void processColor(String color)
	{
		scanner = new StringTokenizer(color);
		//Scan the string for any vertices
		String element = scanner.nextToken();
		while(scanner.hasMoreTokens())
		{
			element = scanner.nextToken();
			Vcolors.add(Float.valueOf(element));
		}
	}	
	/**
	 * Processes a line of text and gets the faces out of it
	 * @param vertex
	 */
	public void processFace(String face)
	{
		int tracker = 0; //if its 0, it needs to parse a vertex, 
						 //1 is normals, 2 is colors
		scanner = new StringTokenizer(face);
		//Scan the string for any vertices
		String element = scanner.nextToken();
		while(scanner.hasMoreTokens())
		{
			if (tracker == 0)
			{
				element = scanner.nextToken();
				Ipositions.add((short) (Short.valueOf(element) - 1));
				tracker++;
			}
			else if(tracker == 1)
			{
				element = scanner.nextToken();
				Icolors.add((short) (Short.valueOf(element) - 1));
				tracker++;
			}
			else if(tracker == 2)
			{
				element = scanner.nextToken();
				Inormals.add((short) (Short.valueOf(element) - 1));
				tracker++;
			}
			else
			{
				tracker = 0;
			}
		}
	}
	/**
	 * Same thing as above but with normals
	 * @param normal
	 */
	public void processNormal(String normal)
	{
		scanner = new StringTokenizer(normal);
		
		String element = scanner.nextToken();
		while(scanner.hasMoreTokens())
		{
			element = scanner.nextToken();
			Vnormals.add(Float.valueOf(element));
		}
	}
	
	public ArrayList <Model> getModels()
	{
		return collectionOfModels;
	}
	

}
