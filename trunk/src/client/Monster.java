package client;

public class Monster extends WUnit {

    public Monster(long id, String nick, int maxHitPoints, double speed, int x, int y, Direction d, String set) {
        super(id, nick, maxHitPoints, speed, x, y, d, set);
        hitPoints = maxHitPoints;
        deathAnim = new MonsterDeathAnimation(set);
    }

    // <editor-fold defaultstate="collapsed" desc="HP works">
    @Override
    public void doHit(int dmg) {
        hitPoints -= dmg;
        if (hitPoints < 0) {
            hitPoints = 0;
        }
    }

    public void restoreHitPoints() {
        hitPoints = maxHitPoints;
    }
    // </editor-fold>

    @Override
    public void kill() {
        Direction d;

        if ((d = moveAnim.getDirection()) == null
                && (d = standAnim.getDirection()) == null) {
            d = Direction.SOUTH;
        }
        deathAnim.run(d, System.currentTimeMillis() - ServerInteraction.serverStartTime);
        mv.stop();
    }

    @Override
    public boolean dead() {
        return hitPoints <= 0;
    }
}
