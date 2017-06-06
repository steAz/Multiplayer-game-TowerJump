package com.mygdx.game.desktop.gui;

import java.io.File;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;

public class GameImage
{
	private Texture imgTexture;
	private float width;
	private float height;
	
	//image will be scaled to the specified width and height
	public GameImage(String fileName, float imgWidth, float imgHeight)
	{
		FileHandle handle = new FileHandle(new File(fileName));
		imgTexture = new Texture(handle);
		width = imgWidth;
		height = imgHeight;
	}
	
	public Texture getTexture()
	{
		return imgTexture;
	}
	
	public float getWidth()
	{
		return width;
	}
	
	public float getHeight()
	{
		return height;
	}
}
