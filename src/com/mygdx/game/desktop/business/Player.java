
package com.mygdx.game.desktop.business;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mygdx.game.desktop.gui.GameImage;
import java.awt.Point;
import com.mygdx.game.desktop.gui.GameGui;
import com.badlogic.gdx.InputProcessor;
import com.mygdx.game.desktop.gui.GamePlatform;

public class Player extends Sprite {

    private float x, y;
    private GameImage image;
    private float speedX, speedY;
    private boolean jumping;
    private MoveInputProcessor moveInputProc;
    private double gravity;
    private int collisionRange; //higher speed requires higher collisionRange to detect collision


    public Player(float x, float y, GameImage img) {
        this.x = x;
        this.y = y;
        image = img;
    	collisionRange = 5;
        speedX = 0;
        speedY = 0;
        gravity = 0;
        jumping = false;
        moveInputProc = new MoveInputProcessor();
        Gdx.input.setInputProcessor(moveInputProc);
    }

    public void jump(){
        if(speedY <= 0 ){
            jumping = false;
        }
            gravity += 0.018;
            speedY -= gravity;
    }

    public void move(GameGui gui){
        jump();

        if(Gdx.input.isKeyPressed(Input.Keys.D)) {
            speedX += 0.2;
        }
        else if(Gdx.input.isKeyPressed(Input.Keys.A)) {
            speedX -= 0.2;
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            if(ifCollisionY(gui)) {
                speedY = 14f;
                jumping = true;
            }
        }

        if(!ifCollisionX(gui, this.x + speedX))
            this.x += speedX;
        else if(speedX > 0){
            this.x = gui.RIGHT_WALL_BEGIN_X - gui.wallImage.getWidth() - this.image.getWidth();
            speedX= -1.1f * speedX;
        }
        else if(speedX < 0){
            this.x = gui.wallImage.getWidth() + gui.LEFT_WALL_BEGIN_X + (2 * this.image.getWidth());
            speedX = -1.1f * speedX;
        }
        this.y += speedY;

        if(ifCollisionY(gui)) {
            speedY=0;
            jumping = false;
            gravity = 0;
            //test
            for(GamePlatform plat : gui.getPlatforms())
            {
                if((this.y >= plat.getY() + gui.platformImage.getHeight() - collisionRange
                        && this.y <= plat.getY() + gui.platformImage.getHeight() + collisionRange
                        && this.x >= plat.getX() - this.image.getWidth()
                        && this.x <= plat.getX() + plat.getPlatformWidth())
                   // || (this.y <= gui.platformImage.getHeight() + collisionRange
                     /*   && this.y >= gui.platformImage.getHeight() - collisionRange)*/){
                    	this.y = plat.getY() + gui.platformImage.getHeight();
                    	break;
                }
            }

        }

    }

    private boolean ifCollisionY(GameGui gui)
    {
    	setCollisionRange();
        if(!jumping){
            for(GamePlatform plat : gui.getPlatforms())
            {
                if((this.y >= plat.getY() + gui.platformImage.getHeight() - collisionRange
                		&& this.y <= plat.getY() + gui.platformImage.getHeight() + collisionRange
                        && this.x >= plat.getX() - this.image.getWidth()
                        && this.x <= plat.getX() + plat.getPlatformWidth())
                    || (this.y <= gui.platformImage.getHeight() + collisionRange
                        && this.y >= gui.platformImage.getHeight() - collisionRange)){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean ifCollisionX(GameGui gui, float x){
        if ((x <= (gui.wallImage.getWidth() + gui.LEFT_WALL_BEGIN_X + (2 * this.image.getWidth()))) ||
                (x >= (gui.RIGHT_WALL_BEGIN_X - gui.wallImage.getWidth() - this.image.getWidth()))) {
            return true;
        }
        return false;
    }

    public float getX(){
        return this.x;
    }

    public float getY(){
        return this.y;
    }

    public boolean isJumping(){
        return this.jumping;
    }

    public void setX(float x){
        this.x = x;
    }

    public void setY(float y){
        this.y = y;
    }

    public double getGravity()
    {
    	return gravity;
    }

    public GameImage getImage(){
        return image;
    }

    private void setCollisionRange()
    {
    	if(gravity <= 0.5)
    	{
    		collisionRange = 5;
    	}
        else if(gravity <= 0.7) // TU JESt KURWA BLAD  - POPRAWA
    	{
    		collisionRange = 7;
    	}
        else
            collisionRange = 15; // TODO
    }

    class MoveInputProcessor implements InputProcessor {

        @Override
        public boolean keyDown(int keyCode) {
            if(keyCode == Input.Keys.D || keyCode == Input.Keys.A) return true;
            return false;
        }

        @Override
        public boolean keyUp(int keyCode) {
            if (keyCode == Input.Keys.D){
                while(speedX  > 0){
                    if(speedX - 0.01 > 0)
                        speedX -= 0.01;
                    else
                        speedX = 0;
                }
                return true;
            }

            else if (keyCode == Input.Keys.A){
                while(speedX  < 0){
                    if(speedX + 0.01 < 0)
                        speedX += 0.01;
                    else
                        speedX = 0;
                }
                return true;
            }

            return false;
        }


        @Override
        public boolean keyTyped(char keyCode) {
            if(keyCode == Input.Keys.W) return true;
            return false;
        }

        @Override
        public boolean touchDown(int i, int i1, int i2, int i3) {
            return false;
        }

        @Override
        public boolean touchUp(int i, int i1, int i2, int i3) {
            return false;
        }

        @Override
        public boolean touchDragged(int i, int i1, int i2) {
            return false;
        }

        @Override
        public boolean mouseMoved(int i, int i1) {
            return false;
        }

        @Override
        public boolean scrolled(int i) {
            return false;
        }

    }
}
