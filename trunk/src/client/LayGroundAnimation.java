package client;

import java.util.Random;

/**
 * Lay ground animation. Used for layed down items.
 * @author sorc
 */
class LayGroundAnimation {

    /**
     * Directional sprite set.
     */
    protected final DirectionalSpriteSet dsSet;

    private Random r = new Random();

    public LayGroundAnimation(String dsSet) {
        this.dsSet = DirectionalSpriteSet.load(dsSet);
    }

    public Sprite getSprite(int x, int y) {
        Sprite s = dsSet.getSprite(Direction.SOUTH_EAST, 0);
        s.x = x;
        s.y = y;
        return s;
    }
}
