package com.mygdx.game.desktop.server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.ServerSocketHints;
import com.badlogic.gdx.net.Socket;
import com.mygdx.game.desktop.business.Position;
import com.mygdx.game.desktop.gui.GameMain;

import java.io.IOException;

public class ServerThread implements Runnable {

    private ClientThread[] clientList = new ClientThread[4];
    private Socket[] socketList = new Socket[4];
    private ServerSocket server;
    public int players;
    private String[] names = new String[4];
    public Position[] positions = new Position[4];
    public GameMain game;

    public ServerThread(GameMain game) {
        this.game = game;
    }

    public void run() {
        players = 0;
        for (int i = 0; i < 4; ++i)
            positions[i] = new Position(0, 1);
        ServerSocketHints hints = new ServerSocketHints();
        hints.acceptTimeout = 0;
        server = Gdx.net.newServerSocket(Net.Protocol.TCP, "localhost", 27015, hints);
        while (true) {
            if (players < 4) {
                socketList[players] = server.accept(null);
                clientList[players] = new ClientThread(socketList[players], players, this);
                new Thread(clientList[players]).start();
                players++;
                Gdx.app.log("Server Thread", "Client connected");
            } else {
                Socket client = server.accept(null);
                try {
                    client.getOutputStream().write("Could not connect. The game is full.".getBytes());
                    client.dispose();
                } catch (IOException e) {
                    Gdx.app.log("GameServer", "an error occurred while saying the server is full", e);
                }
            }
        }
    }

    public void startGame() {
        for (int i = 0; i < players; ++i)
            clientList[i].inGame = true;
    }

    public synchronized void setPosition(Position pos, int i) {
        positions[i].x = pos.x;
        positions[i].y = pos.y;
        //Gdx.app.log("Server (server thread)", "Set position of id " + i + ": " + positions[i].x + " " + positions[i].y);
    }

    public synchronized void setName(String name, int i) {
        names[i] = name;
    }

    public synchronized Position[] getPositions() {
        return positions;
    }

    public String[] getNames() {
        return names;
    }

    public int getNumber() {
        return players;
    }
}