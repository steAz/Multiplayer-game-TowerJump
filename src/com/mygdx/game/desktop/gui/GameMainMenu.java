package com.mygdx.game.desktop.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import org.lwjgl.opengl.Display;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class GameMainMenu implements Screen {

    final GameMain game;
    private SpriteBatch batch;
    private GameImage backgroundImage;
    private GameImage wallImage;
    private Skin skin;
    private Stage stage;
    private Thread bgThread;
    private String[] names;
    private Label players;
    private Label statusLabel;


    public GameMainMenu(final GameMain game) {
        this.game = game;
        //batch is needed to do all the drawing in gameScreen
        batch = new SpriteBatch();
        //loading images:
        backgroundImage = new GameImage("desktop/assets/background.jpg", 64f, 64f);
        wallImage = new GameImage("desktop/assets/wall.jpg", 64f, 64f);
        loadSkin();
        //gui
        stage = new Stage(new ScreenViewport());
        VerticalGroup vg = new VerticalGroup();
        Label welcomeLabel = new Label("Welcome to TowerJump!", skin);
        TextButton playConnectButton;
        if (!game.isServer)
            playConnectButton = new TextButton("Connect to server", skin);
        else
            playConnectButton = new TextButton("Start game", skin);
        statusLabel = new Label("STATUS: NOT CONNECTED", skin);
        players = new Label("Players connected: 0", skin);
        vg.addActor(welcomeLabel);
        vg.addActor(playConnectButton);
        vg.addActor(statusLabel);
        vg.addActor(players);
        vg.space(50);
        vg.setPosition((GameGui.WINDOW_WIDTH/2), GameGui.WINDOW_HEIGHT - 200);
        welcomeLabel.setColor(255, 255, 255, 255);
        stage.addActor(vg);

        Gdx.input.setInputProcessor(stage);

        runBgThread();


        // Wire up a click listener to our button
        if (!game.isServer) {
            playConnectButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    connectToServer();
                    synchronized (bgThread)
                    {
                        bgThread.notify();
                    }
                }
            });
        }
        else
        {
            //Connect to server immediately as we are the host
            connectToServer();
            synchronized (bgThread)
            {
                bgThread.notify();
            }
            playConnectButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    game.server.startGame();
                    dispose();
                    game.startGame();
                }
            });
        }
    }

    private void loadSkin() {
        FileHandle fileHandle = Gdx.files.internal("desktop/assets/neutralizer/skin/neutralizer-ui.json");
        FileHandle atlasFile = fileHandle.sibling("neutralizer-ui.atlas");
        TextureAtlas atlas = new TextureAtlas(atlasFile);
        skin = new Skin(fileHandle, atlas);
    }
    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        game.batch.begin();
        Texture backgroundTexture = backgroundImage.getTexture();
        float width = backgroundImage.getWidth();
        float height = backgroundImage.getHeight();
        int startPos = 0;
        int endPos = GameGui.WINDOW_WIDTH;

        for(int i = startPos; i < endPos; i += width)
        {
            for(int j = 0; j < GameGui.WINDOW_HEIGHT; j += height)
            {
                game.batch.draw(backgroundTexture, i, j, width, height);
            }
        }
        //game.font.draw(game.batch, "Welcome to TowerJump!", GameGui.WINDOW_WIDTH / 2, 150);
        game.batch.end();
        stage.act(delta);
        stage.draw();

//        if (Gdx.input.isTouched()) {
//            game.setScreen(new GameGui(game));
//            dispose();
//        }
    }

    private void runBgThread() {
        //Run a thread to get player list and know if game has started
        bgThread = new Thread(new Runnable() {
            @Override
            public void run () {
                //Waiting for connection with server
                try {
                    synchronized (bgThread) {
                        bgThread.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Gdx.app.log("Client Thread", "Client connected -> bg thread started");
                while(true) {
                    try {
                        Object obj = game.ois.readObject();
                        if (obj instanceof String) {
                            String type = (String) obj;
                            if (type.equals("Number")) {
                                //names = (String[]) ois.readObject();
                                game.players = game.ois.readInt();
                                players.setText("Players connected: " + game.players);
                                //players.getItems().clear();
                                //players.setItems(playersArr);
                                //System.out.println(p);
                                //players.setItems(new Array<String>(names));
                            } else {
                                if (!game.isServer) {
                                    Gdx.app.log("Client Thread", "Starting the game");
                                    dispose();
                                    game.startGame();
                                }
                                break;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    try {
                        synchronized (bgThread) {
                            bgThread.wait(256);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        bgThread.start();
    }

    private void connectToServer() {
        SocketHints socketHints = new SocketHints();
        // Socket will time out in 4 seconds
        socketHints.connectTimeout = 4000;
        game.serverConnection = Gdx.net.newClientSocket(Net.Protocol.TCP, "localhost", 27015, socketHints);
        //Send name
        try {
            game.oos = new ObjectOutputStream(game.serverConnection.getOutputStream());
            game.oos.writeObject(game.name);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Get id
        try {
            game.ois = new ObjectInputStream(game.serverConnection.getInputStream());
            game.id = game.ois.readInt();
        } catch (IOException e) {
            e.printStackTrace();
        }
        statusLabel.setText("STATUS: CONNECTED");
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}