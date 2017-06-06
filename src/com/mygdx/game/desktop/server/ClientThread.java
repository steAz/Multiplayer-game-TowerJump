package com.mygdx.game.desktop.server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.net.Socket;
import com.mygdx.game.desktop.business.Position;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class ClientThread implements Runnable {

    public String name;
    public int id;
    public boolean inGame = false;
    public boolean starting = true;
    private Socket client;
    private ServerThread server;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    public ClientThread(Socket client, int id, ServerThread server) {
        this.client = client;
        this.id = id;
        this.server = server;
        ois = null;
        oos = null;
    }

    public void run(){
        try {
            oos = new ObjectOutputStream(client.getOutputStream());
            ois = new ObjectInputStream(client.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        loadName();
        server.setName(name, id);
        sendId();
        //Sending position to main server thread
        while(true)
        {
            if (!inGame) {
                //String[] names = server.getNames();
                int number = server.getNumber();
                try {
                    oos.writeObject("Number");
                    //Gdx.app.log("Client Thread", "Sending names");
                    oos.writeInt(number);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    synchronized (this) {
                        this.wait(256);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            else if (starting && (id != 0)) {
                Gdx.app.log("Server (client thread)", "Sending client start game message.");
                try {
                    synchronized (oos) {
                        oos.writeObject("START");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                starting = false;
            }
            else {
                server.setPosition(getPosition(), id);
                sendPosition();
                try {
                    synchronized (this) {
                        this.wait(10);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void loadName() {
        try {
            synchronized (ois) {
                name = (String) ois.readObject();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void sendId() {
        try {
            synchronized (oos) {
                oos.writeInt(id);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Position getPosition() {
        Position pos = new Position(0, 0);
        try {
            synchronized (ois) {
                pos = (Position) ois.readObject();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Gdx.app.log("Server (client thread)", "Received position from id " + id + " : " + pos.x + " " + pos.y);
        return new Position(pos);
    }

    private void sendPosition() {
//        Position[] poss = server.positions;
//        for (int i = 0; i < server.players; ++i) {
//            Gdx.app.log("Server (client thread)", "Position " + i + " on server: " + poss[i].x + " " + poss[i].y);
//        }
        try {
            synchronized (oos) {
                //Gdx.app.log("Server (client thread)", "Sending position of id " + id + " : " + poss[id].x + " " + poss[id].y);
                oos.writeObject(server.getPositions());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
