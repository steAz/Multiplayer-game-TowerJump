package com.mygdx.game.desktop.gui;

public class GamePlatform
{
	private int x;
	private int y;
	private int numberOfBlocks;
	public static final int PLATFORM_WIDTH = 64;

	public GamePlatform(int x, int y, int numberOfBlocks)
	{
		this.x = x;
		this.y = y;
		this.numberOfBlocks = numberOfBlocks;
	}

	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}

	public int getNumberOfBlocks()
	{
		return numberOfBlocks;
	}
        
    public int getRightEndX()
    {
        return x + (numberOfBlocks * PLATFORM_WIDTH);
    }

    public int getPlatformWidth()
    {
        return numberOfBlocks * PLATFORM_WIDTH;
    }
}
