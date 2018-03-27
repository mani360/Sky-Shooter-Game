package com.example.administrator.skygame.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import java.util.List;

/**
 * Enemy aircraft, from top to bottom along a straight line
 */
public class EnemyPlane extends AutoSprite {

    private int power = 1;// Enemy anti-strike capability
    private int value = 0;// Hit an enemy aircraft score

    public EnemyPlane(Bitmap bitmap){
        super(bitmap);
    }

    public void setPower(int power){
        this.power = power;
    }

    public int getPower(){
        return power;
    }

    public void setValue(int value){
        this.value = value;
    }

    public int getValue(){
        return value;
    }

    @Override
    protected void afterDraw(Canvas canvas, Paint paint, GameView gameView) {
        super.afterDraw(canvas, paint, gameView);

        // After the drawing is completed to check whether the bullet hit
        if(!isDestroyed()){

            List<Bullet> bullets = gameView.getAliveBullets();
            for(Bullet bullet : bullets){
                // Determine whether the enemy plane meets the bullet
                Point p = getCollidePointWithOther(bullet);
                if(p != null){
                    //If there is a cross point, bullets hit the plane
                    bullet.destroy();
                    power--;
                    if(power <= 0){
                        // Enemy aircraft have no energy, the implementation of the explosion effect
                        explode(gameView);
                        return;
                    }
                }
            }
        }
    }

    //After the explosion is created, the enemy plane will be destroyed
    public void explode(GameView gameView){
        //Create an explosion effect
        float centerX = getX() + getWidth() / 2;
        float centerY = getY() + getHeight() / 2;
        Bitmap bitmap = gameView.getExplosionBitmap();
        Explosion explosion = new Explosion(bitmap);
        explosion.centerTo(centerX, centerY);
        gameView.addSprite(explosion);
        //After creating the explosion effect, add a score to GameView and destroy the enemy plane
        gameView.addScore(value);
        destroy();
    }
}