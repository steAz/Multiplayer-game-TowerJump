package com.mygdx.game.desktop.gui;

import java.util.ArrayList;
import java.util.Random;

public class PlatformsContainer extends ArrayList<GamePlatform>
{
	private int bottomHeight;//Y coord of the lowest platform
	private int topHeight;//Y coord of the heighest platform
	private int lastPlatformX; //X coordinate of the highest platform


	public PlatformsContainer()
	{
		bottomHeight = 0;
		topHeight = 0;
		lastPlatformX = GameGui.WINDOW_WIDTH / 2;
	}

	//adds new random platform above the heighest one
	public void addNewRandom()
	{
		//random width: (from 2 to 6)
		Random gen = new Random();
		int width = 2 + gen.nextInt(3);

		int minX = GameGui.LEFT_WALL_BEGIN_X + 3 * GamePlatform.PLATFORM_WIDTH;
		int maxX = GameGui.RIGHT_WALL_BEGIN_X - (width + 1) * GamePlatform.PLATFORM_WIDTH;
		int randX;
		boolean properX = false;

		//to make sure it'll be possible to jump to the next platform (500 for example)
		do{
			randX = minX + gen.nextInt(maxX - minX);
			if(Math.max(randX, lastPlatformX) - Math.min(randX, lastPlatformX) < 500) properX = true;
			else properX = false;
		}while(!properX);

		lastPlatformX = randX;

		//200 for example. It will depend on how jumping will work in future
		int randY = topHeight + 100 + gen.nextInt(150);

		topHeight = randY;

		add(new GamePlatform(randX, randY, width));
	}

    public int getTopHeight()
	{
		return topHeight;
	}

	//removes all platforms below the bottomBound Y coordinate
	public void removePlatformsBelow(int bottomBound)
	{
        ArrayList<GamePlatform> platformsToDelete = new ArrayList<GamePlatform>();
		if(!this.isEmpty())
		{
			for(GamePlatform p : this)
			{
				if(p.getY() < bottomBound ) platformsToDelete.add(p);
			}
		}
		this.removeAll(platformsToDelete);
	}

    public ArrayList<GamePlatform> getPlatformsOnScreen(int topScreen) {
        ArrayList<GamePlatform> platformsOnScreen = new ArrayList<GamePlatform>();

        for(GamePlatform platform : this) {
            if (platform.getY() < topScreen)
                platformsOnScreen.add(platform);
        }
        return platformsOnScreen;
    }
}
