package com.game;



import java.io.IOException;

import android.content.res.AssetManager;
import android.test.AndroidTestCase;

public class ObjLoaderTest extends AndroidTestCase
{
	private ObjLoader loader;
	private AssetManager assets;
	
	public ObjLoaderTest()
	{
		//Nothing to do.
	}
	
	@Override
	public void setUp()
	{
		assets = getContext().getAssets();
		try {
			loader = new ObjLoader(assets);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testParse()
	{
		try 
		{
			loader.parseObject("cube4.obj");
		} 
		catch (IOException e) 
		{
			System.out.println("Didn't work");
		}
		assertEquals(loader.getModels().size() , 1);

	}
}
