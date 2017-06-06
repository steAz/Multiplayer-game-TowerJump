package com.mygdx.game.desktop.gui;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.desktop.business.Player;
import com.mygdx.game.desktop.business.Position;
import com.mygdx.game.desktop.gui.PlatformsContainer;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.PixelFormat;

import java.awt.Point;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class GameGui implements Screen
{
    public GameImage wallImage;
    public GameImage platformImage;

    final GameMain game;
    final int id;
    private Thread bgThread;
	private SpriteBatch batch;
	private GameImage backgroundImage;
	private PlatformsContainer platforms;
    private int screenMovingTempo;
    private int tempoModifier;

	private int bottomScreenHeight;
	private int topScreenHeight;
    private Player[] players = new Player[4];
    private Position[] positions = new Position[4];
    private Color[] colors = new Color[4];

	public static final int WINDOW_WIDTH = 1200;
	public static final int WINDOW_HEIGHT = 700;

    //public static final int LEFT_WALL_BEGIN_X = (int)(0.1 * WINDOW_WIDTH);
    public static final int LEFT_WALL_BEGIN_X = 0;
    public static final int RIGHT_WALL_BEGIN_X = (int)(WINDOW_WIDTH - 0.3 * WINDOW_WIDTH);

	//public static final int LEFT_WALL_BEGIN_X = 80;
	//public static final int RIGHT_WALL_BEGIN_X = 1100;

	public GameGui(final GameMain game) {
		this.game = game;
		id = game.id;
        //batch is needed to do all the drawing in gameScreen
        batch = new SpriteBatch();
        //loading images:
        backgroundImage = new GameImage("desktop/assets/background.jpg", 64f, 64f);
        wallImage = new GameImage("desktop/assets/wall.jpg", 64f, 64f);
        platformImage = new GameImage("desktop/assets/platform.jpg", 64f, 32f);
        colors[0] = Color.BLUE;
        colors[1] = Color.CORAL;
        colors[2] = Color.FOREST;
        colors[3] = Color.GRAY;

        bottomScreenHeight = 0;
        topScreenHeight = WINDOW_HEIGHT;
        tempoModifier = 0;
        screenMovingTempo = 0;

        platforms = new PlatformsContainer();
        for (int i = 0; i < 7; ++i)
            platforms.addNewRandom();

        for (int i = 0; i < game.players; ++i) {
            players[i] = new Player((LEFT_WALL_BEGIN_X + 2 * (wallImage.getWidth())),
                    (platformImage.getHeight()),
                    new GameImage("desktop/assets/mario.jpg", 32f, 32f));
            positions[i] = new Position((LEFT_WALL_BEGIN_X + 2 * (wallImage.getWidth())), (platformImage.getHeight()));
        }
        runPositionThread();
	}


	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
	}

    @Override
    public void show() {

    }

    @Override
	public void render(float delta) {
		//drawing graphics
		//System.out.println(screenMovingTempo);
		batch.begin();
		drawBackground();
        drawWalls();
        if (bottomScreenHeight < 20)
            drawStartingPlatform();
		drawPlatforms();
        drawPlayers();
		batch.end();

        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE))
            Gdx.app.exit();

        if(players[id].getY() > WINDOW_HEIGHT / 2)
        {
            updateTempoModifier();
            setTempo();
            moveScreenUp(screenMovingTempo);
        }

		//removing not needed platforms:
		platforms.removePlatformsBelow(bottomScreenHeight - 10);
	}

    private void drawBackground()
	{
		Texture backgroundTexture = backgroundImage.getTexture();
		float width = backgroundImage.getWidth();
		float height = backgroundImage.getHeight();
//		int startPos = LEFT_WALL_BEGIN_X + 2*((int)wallImage.getWidth());
//		int endPos = RIGHT_WALL_BEGIN_X - 1*((int)wallImage.getWidth());
        int startPos = 0;
        int endPos = RIGHT_WALL_BEGIN_X;

		for(int i = startPos; i < endPos; i += width)
		{
			for(int j = 0; j < WINDOW_HEIGHT + bottomScreenHeight; j += height)
			{
				batch.draw(backgroundTexture, i, j - bottomScreenHeight, width, height);
			}
		}
	}

	private void drawWalls()
	{
		Texture wallTexture = wallImage.getTexture();
		float width = wallImage.getWidth();
		float height = wallImage.getHeight();
		for(int i=0;i < WINDOW_HEIGHT + bottomScreenHeight; i+=width)
		{
			batch.draw(wallTexture, LEFT_WALL_BEGIN_X, i - bottomScreenHeight, width, height);
			batch.draw(wallTexture, LEFT_WALL_BEGIN_X + width, i - bottomScreenHeight, width, height);
			batch.draw(wallTexture, RIGHT_WALL_BEGIN_X, i - bottomScreenHeight, width, height);
			batch.draw(wallTexture, RIGHT_WALL_BEGIN_X - width, i - bottomScreenHeight, width, height);
		}
	}

	private void drawStartingPlatform()
	{
		Texture platformTexture = platformImage.getTexture();
		float width = platformImage.getWidth();
		float height = platformImage.getHeight();

		int startPos = LEFT_WALL_BEGIN_X + 2*(int)(wallImage.getWidth());
		int endPos = RIGHT_WALL_BEGIN_X - (int)(wallImage.getWidth());

		for(int i = startPos; i < endPos; i+= width)
		{
			batch.draw(platformTexture, i, 0, width, height);
		}
	}

	//draws all platforms between bottom and top of the screen
	private void drawPlatforms()
	{
		for(GamePlatform p : platforms)
		{
			//if we already drawn all visible platforms
			if(p.getY() > topScreenHeight)
                break;
			if(p.getY() > bottomScreenHeight)
			{
				int yScreenPosition;
				//calculate position on the screen of the platform
				yScreenPosition = p.getY() - bottomScreenHeight;
				batch.draw(platformImage.getTexture(), p.getX(), yScreenPosition,
						GamePlatform.PLATFORM_WIDTH * p.getNumberOfBlocks(), platformImage.getHeight());
			}
		}
	}

    private void drawPlayers()
    {
        for(int i = 0; i < game.players; ++i) {
            batch.setColor(colors[i]);
            float x = positions[i].x;
            float y = positions[i].y - bottomScreenHeight;
            GameImage img = players[i].getImage();
            float width = img.getWidth();
            float height = img.getHeight();
            players[i].move(this);
            positions[i].x = players[i].getX();
            positions[i].y = players[i].getY();
            batch.draw(img.getTexture(), x, y, width, height);
        }
        batch.setColor(Color.WHITE);
    }

    //tempo is integer > 1
    private void moveScreenUp(int tempo)
    {
        bottomScreenHeight += tempo;
        topScreenHeight += tempo;
        if(platforms.size() < 15)
            platforms.addNewRandom();
    }

    //sets speed of screen movement
    private void setTempo()
    {
        float distanceFromScreenBottom = positions[id].y - bottomScreenHeight;
        float percentageOfScreen = distanceFromScreenBottom / WINDOW_HEIGHT;
        if(percentageOfScreen < 0.5)
            screenMovingTempo = 2;
        else if (percentageOfScreen >= 0.5 && percentageOfScreen < 0.75)
            screenMovingTempo = 3;
        else if (percentageOfScreen >= 0.75 && percentageOfScreen < 0.85)
            screenMovingTempo = 5;
        else
            screenMovingTempo = 7;

        screenMovingTempo += tempoModifier;
    }

    private void updateTempoModifier()
    {
        tempoModifier = bottomScreenHeight / 3000;
    }

    private void runPositionThread() {
        bgThread = new Thread(new Runnable() {
            @Override
            public void run () {
                Gdx.app.log("Client(ingame)", "Running bg thread");
                while(true) {
                    try {
                        synchronized (game.oos) {
                            game.oos.writeObject(new Position(players[id].getX(), players[id].getY()));
                        }
                        try {
                            synchronized (bgThread) {
                                bgThread.wait(2);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        synchronized (game.ois) {
                            loadPositions((Position[]) game.ois.readObject());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    try {
                        synchronized (bgThread) {
                            bgThread.wait(10);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        bgThread.start();
    }

    private void loadPositions(Position[] poss) {
        //Gdx.app.log("Client(ingame)", "My position: " + positions[id].x + " " + positions[id].y);
        for (int i = 0; i < game.players; ++i)
        {
            Gdx.app.log("Client(ingame)", "Position " + i + " from server: " + poss[i].x + " " + poss[i].y);
            if (i != id) {
                positions[i].x = poss[i].x;
                positions[i].y = poss[i].y;
            }
        }
    }

	@Override
	public void pause() {
		// TODO Auto-generated method stub
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
	}

    @Override
    public void hide() {

    }

    @Override
	public void dispose() {
		batch.dispose();
	}

    public ArrayList<GamePlatform> getPlatforms()
    {
        return platforms.getPlatformsOnScreen(topScreenHeight);
    }

}
