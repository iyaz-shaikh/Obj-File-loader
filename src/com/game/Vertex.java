package com.game;

public class Vertex 
{
	
	public float positionX;
	public float positionY;
	public float positionZ;
	
	public float normalX;
	public float normalY;
	public float normalZ;
	
	public float texture1;
	public float texture2;
	
	public Vertex()
	{
		//Nothing to do.
	}
	
	
	public boolean equals(Vertex vertex)
	{
		return (this.positionX == vertex.positionX)
			&& (this.positionY == vertex.positionY)
			&& (this.positionZ == vertex.positionZ)
			&& (this.normalX == vertex.normalX)
			&& (this.normalY == vertex.normalY)
			&& (this.normalZ == vertex.normalZ)
			&& (this.texture1 == vertex.texture1)
			&& (this.texture2 == vertex.texture2);
	}

}
