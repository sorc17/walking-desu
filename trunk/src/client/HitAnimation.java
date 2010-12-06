package client;

import java.awt.Point;

public abstract class HitAnimation implements WDrawable {

    protected DirectionalSpriteSet set;
    protected Direction direction;
    protected boolean done = false;
    protected Point curPos;

    protected HitAnimation(String set, Point curPos, Direction direction) {
        this.set = DirectionalSpriteSet.load(set + "_hit");
        this.curPos = curPos;
        this.direction = direction;
    }

    @Override
    public abstract Sprite getSprite();

    public boolean isDone() {
        return done;
    }
}
