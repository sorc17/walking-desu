package client;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class WanderingLocks {

    private static final Lock monstersLock = new ReentrantLock();
    private static final Lock towersLock = new ReentrantLock();
    private static final Lock playersLock = new ReentrantLock();
    private static final Lock unitsLock = new ReentrantLock();
    private static final Lock nukesLock = new ReentrantLock();
    private static final Lock hitsLock = new ReentrantLock();
    private static final Lock packetLock = new ReentrantLock();
    private static final int debugLevel = 0;

    public static void lockAll() {
        if (debugLevel == 2) {
            System.out.println(Thread.currentThread().getId() + " thread try to lock monsters.");
        }
        monstersLock.lock();
        if (debugLevel == 2) {
            System.out.println(Thread.currentThread().getId() + " thread try to lock towers.");
        }
        towersLock.lock();
        if (debugLevel == 2) {
            System.out.println(Thread.currentThread().getId() + " thread try to lock players.");
        }
        playersLock.lock();
        if (debugLevel == 2) {
            System.out.println(Thread.currentThread().getId() + " thread try to lock units.");
        }
        unitsLock.lock();
        if (debugLevel == 1) {
            System.out.println(Thread.currentThread().getId() + " thread successfully lock all resources.");
        }
    }

    public static void unlockAll() {
        if (debugLevel == 2) {
            System.out.println(Thread.currentThread().getId() + " thread try to unlock units.");
        }
        unitsLock.unlock();
        if (debugLevel == 2) {
            System.out.println(Thread.currentThread().getId() + " thread try to unlock players.");
        }
        playersLock.unlock();
        if (debugLevel == 2) {
            System.out.println(Thread.currentThread().getId() + " thread try to unlock towers.");
        }
        towersLock.unlock();
        if (debugLevel == 2) {
            System.out.println(Thread.currentThread().getId() + " thread try to lock monsters.");
        }
        monstersLock.unlock();
        if (debugLevel == 1) {
            System.out.println(Thread.currentThread().getId() + " thread successfully unlock all resources.");
        }
    }

    public static void lockPlayers() {
        monstersLock.lock();
        towersLock.lock();
        playersLock.lock();
        if (debugLevel == 1) {
            System.out.println(Thread.currentThread().getId() + " thread successfully lock players.");
        }
    }

    public static void unlockPlayers() {
        playersLock.unlock();
        towersLock.unlock();
        monstersLock.unlock();
        if (debugLevel == 1) {
            System.out.println(Thread.currentThread().getId() + " thread successfully unlock players.");
        }
    }

    public static void lockTowers() {
        monstersLock.lock();
        towersLock.lock();
        if (debugLevel == 1) {
            System.out.println(Thread.currentThread().getId() + " thread successfully lock towers.");
        }
    }

    public static void unlockTowers() {
        towersLock.unlock();
        monstersLock.unlock();
        if (debugLevel == 1) {
            System.out.println(Thread.currentThread().getId() + " thread successfully unlock towers.");
        }
    }

    public static void lockMonsters() {
        monstersLock.lock();
        if (debugLevel == 1) {
            System.out.println(Thread.currentThread().getId() + " thread successfully lock monsters.");
        }
    }

    public static void unlockMonsters() {
        monstersLock.unlock();
        if (debugLevel == 1) {
            System.out.println(Thread.currentThread().getId() + " thread successfully unlock monsters.");
        }
    }

    public static void lockPacket() {
        packetLock.lock();
        if (debugLevel == 1) {
            System.out.println(Thread.currentThread().getId() + " thread successfully lock packet.");
        }
    }

    public static void unlockPacket() {
        packetLock.unlock();
        if (debugLevel == 1) {
            System.out.println(Thread.currentThread().getId() + " thread successfully unlock packet.");
        }
    }

    public static void lockNukes() {
        nukesLock.lock();
        if (debugLevel == 1) {
            System.out.println(Thread.currentThread().getId() + " thread successfully lock nukes.");
        }
    }

    public static void unlockNukes() {
        nukesLock.unlock();
        if (debugLevel == 1) {
            System.out.println(Thread.currentThread().getId() + " thread successfully unlock nukes.");
        }
    }

    public static void lockHits() {
        hitsLock.lock();
        if (debugLevel == 1) {
            System.out.println(Thread.currentThread().getId() + " thread successfully lock hits.");
        }
    }

    public static void unlockHits() {
        hitsLock.unlock();
        if (debugLevel == 1) {
            System.out.println(Thread.currentThread().getId() + " thread successfully unlock hits.");
        }
    }
}
