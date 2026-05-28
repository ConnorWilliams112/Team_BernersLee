import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;
import dev.robocode.tankroyale.botapi.graphics.Color;

public class VanJeckylson extends Bot {

    public static void main(String[] args) {
        new VanJeckylson().start();
    }

    @Override
    public void run() {
        setBodyColor(Color.BLACK);
        setTurretColor(Color.fromRgb(0x00, 0x96, 0x32));
        setRadarColor(Color.BLACK);
        setBulletColor(Color.BLACK);
        setScanColor(Color.BLACK);

        // movement loop
        while (isRunning()) {
            forward(150);
            turnGunLeft(360);
            back(100);
            turnRight(45);
            turnGunLeft(360);
        }
    }

    @Override
    public void onScannedBot(ScannedBotEvent e) {
        // use stronger bullets when closer
        if (distanceTo(e.getX(), e.getY()) < 150) {
            fire(3);
        } else {
            fire(1);
        }
    }

    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        var bearing = calcBearing(e.getBullet().getDirection());
        turnRight(90 - bearing);
        forward(100);
    }

    @Override
    public void onHitWall(HitWallEvent e) {
        // back up and turn away
        back(100);
        turnRight(90);
    }

    @Override
    public void onDeath(DeathEvent e) {
        // TODO
    }

    @Override
    public void onWonRound(WonRoundEvent e) {
        // TODO
    }

}
