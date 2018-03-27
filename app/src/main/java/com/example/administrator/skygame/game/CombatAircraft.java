package com.example.administrator.skygame.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;

import java.util.List;

/**
 * Fighter class, you can change the location through interaction
 */
public class CombatAircraft extends Sprite {
    private boolean collide = false;// Identify whether the fighter was hit
    private int bombAwardCount = 0;// The number of bombs you can use

    // Two bullets related
    private boolean single = true;// Identifies if a single bullet was issued
    private int doubleTime = 0;// The number of times that the current bullet has been drawn
    private int maxDoubleTime = 140;//Use the maximum number of double bullets to draw
    // Blinking after being hit
    private long beginFlushFrame = 0;// To begin flashing the fighter at the beginning of the beginFlushFrame frame
    private int flushTime = 0;// has flashed the number of times
    private int flushFrequency = 16;// Change the fighter's visibility every 16 frames while blinking
    private int maxFlushTime = 10;// The maximum number of flashes

    public CombatAircraft(Bitmap bitmap){
        super(bitmap);
    }

    @Override
    protected void beforeDraw(Canvas canvas, Paint paint, GameView gameView) {
        if(!isDestroyed()){
            // Make sure the fighter is completely within the Canvas range
            validatePosition(canvas);

            // firing bullets every 7th
            if(getFrame() % 7 == 0){
                fight(gameView);
            }
        }
    }
    // Make sure the fighter is completely within the Canvas range
    private void validatePosition(Canvas canvas){
        if(getX() < 0){
            setX(0);
        }
        if(getY() < 0){
            setY(0);
        }
        RectF rectF = getRectF();
        int canvasWidth = canvas.getWidth();
        if(rectF.right > canvasWidth){
            setX(canvasWidth - getWidth());
        }
        int canvasHeight = canvas.getHeight();
        if(rectF.bottom > canvasHeight){
            setY(canvasHeight - getHeight());
        }
    }

    // launch a bullet
    public void fight(GameView gameView){
        // If the fighter is hit or destroyed, no bullet will be fired
        if(collide || isDestroyed()){
            return;
        }
        float x = getX() + getWidth() / 2;
        float y = getY() - 5;
        if(single){
            //Single shot single yellow bullet firing mode
            Bitmap yellowBulletBitmap = gameView.getYellowBulletBitmap();
            Bullet yellowBullet = new Bullet(yellowBulletBitmap);
            yellowBullet.moveTo(x, y);
            gameView.addSprite(yellowBullet);
        }
        else{
            //Launch two rounds of blue bullets in dual mode
            float offset = getWidth() / 4;
            float leftX = x - offset;
            float rightX = x + offset;
            Bitmap blueBulletBitmap = gameView.getBlueBulletBitmap();

            Bullet leftBlueBullet = new Bullet(blueBulletBitmap);
            leftBlueBullet.moveTo(leftX, y);
            gameView.addSprite(leftBlueBullet);

            Bullet rightBlueBullet = new Bullet(blueBulletBitmap);
            rightBlueBullet.moveTo(rightX, y);
            gameView.addSprite(rightBlueBullet);

            doubleTime++;
            if(doubleTime >= maxDoubleTime){
                single = true;
                doubleTime = 0;
            }
        }
    }

    /*If the fighter is hit, perform an explosion effect Specifically,
    first hide the fighter, and then create an explosion effect,
     the explosion with 28-frame rendering complete*/
    // After the explosion effect is fully rendered, the explosion effect disappears
    // The fighter then goes into blinking mode, the fighter flashes a certain number of times and destroyed
    protected void afterDraw(Canvas canvas, Paint paint, GameView gameView){
        if(isDestroyed()){
            return;
        }

        // When the plane is not currently hit, determine whether it will be hit by an enemy plane
        if(!collide){
            List<EnemyPlane> enemies = gameView.getAliveEnemyPlanes();
            for(EnemyPlane enemyPlane : enemies){
                Point p = getCollidePointWithOther(enemyPlane);
                if(p != null){
                    // p for the fighter and the enemy plane collision point, if p is not null, then the fighter was hit by the enemy plane
                    explode(gameView);
                    break;
                }
            }
        }

        //beginFlushFrame The initial value is 0, that did not enter the flash mode
        // If beginFlushFrame is greater than 0, it means to be in blink mode if the beginFlushFrame frame is in blink
        if(beginFlushFrame > 0){
            long frame = getFrame();
            // If the current number of frames is greater than or equal to beginFlushFrame, it means the fighter enters the blinking state before the destruction
            if(frame >= beginFlushFrame){
                if((frame - beginFlushFrame) % flushFrequency == 0){
                    boolean visible = getVisibility();
                    setVisibility(!visible);
                    flushTime++;
                    if(flushTime >= maxFlushTime){
                        destroy();
                        //Game.gameOver();
                    }
                }
            }
        }

        // Check if the item was acquired without being hit
        if(!collide){
            // Check if bomb props are available
            List<BombAward> bombAwards = gameView.getAliveBombAwards();
            for(BombAward bombAward : bombAwards){
                Point p = getCollidePointWithOther(bombAward);
                if(p != null){
                    bombAwardCount++;
                    bombAward.destroy();
                    //Game.receiveBombAward();
                }
            }

            // Check if bullet props are available
            List<BulletAward> bulletAwards = gameView.getAliveBulletAwards();
            for(BulletAward bulletAward : bulletAwards){
                Point p = getCollidePointWithOther(bulletAward);
                if(p != null){
                    bulletAward.destroy();
                    single = false;
                    doubleTime = 0;
                }
            }
        }
    }

    // Fighter blast
    private void explode(GameView gameView){
        if(collide){
            collide = true;
            setVisibility(true);
            float centerX = getX() + getWidth() / 2;
            float centerY = getY() + getHeight() / 2;
            Explosion explosion = new Explosion(gameView.getExplosionBitmap());
            explosion.centerTo(centerX, centerY);
            gameView.addSprite(explosion);
            beginFlushFrame = getFrame() + explosion.getExplodeDurationFrame();
        }
        else setVisibility(false);
    }

    // Get the number of bombs available
    public int getBombCount(){
        return bombAwardCount;
    }

    //Fighter planes use bombs
    public void bomb(GameView gameView){
        if(collide || isDestroyed()){
            return;
        }

        if(bombAwardCount > 0){
            List<EnemyPlane> enemyPlanes = gameView.getAliveEnemyPlanes();
            for(EnemyPlane enemyPlane : enemyPlanes){
                enemyPlane.explode(gameView);
            }
            bombAwardCount--;
        }
    }

    public boolean isCollide(){
        return collide;
    }

    public void setNotCollide(){
        collide = false;
    }
}