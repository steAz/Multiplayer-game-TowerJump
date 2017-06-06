package com.mygdx.game.desktop.gui;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.net.Socket;
import com.mygdx.game.desktop.server.ServerThread;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class GameMain extends Game {

    public SpriteBatch batch;
    public BitmapFont font;
    public String[] args;
    public Socket serverConnection;
    public ServerThread server;
    public String name;
    public ObjectOutputStream oos;
    public ObjectInputStream ois;
    public int players;
    public int id;
    public boolean isServer = false;

    public GameMain(String[] args) {
        this.args = args;
    }

    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        parseArguments();
        this.setScreen(new GameMainMenu(this));
    }

    public void render() {
        super.render(); //important!
    }

    public void dispose() {
        batch.dispose();
        font.dispose();
    }

    private void parseArguments() {
        for (int i = 0; i < args.length; ++i)
        {
            Gdx.app.log("GameMain", "Argument " + i + " : " + args[i]);
            if (args[i].equals("-server")) {
                isServer = true;
                server = new ServerThread(this);
                new Thread(server).start();
            }
            if (args[i].equals("-name"))
                name = args[i+1];
        }
        if (name == null)
            name = "Player";
    }

    public void startGame() {
        //dispose();
        final GameMain t = this;
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                t.setScreen(new GameGui(t));
            }
        });

    }

}
