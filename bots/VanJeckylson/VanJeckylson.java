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
        double distance = distanceTo(e.getX(), e.getY());

        // Fire harder when closer
        double firePower;
        if (distance < 150) {
            firePower = 3;
        } else if (distance < 350) {
            firePower = 2;
        } else {
            firePower = 1;
        }

        // Save energy if low
        if (getEnergy() < 10) {
            firePower = 0.5;
        }

        // Predict where the enemy will be
        double bulletSpeed = 20 - 3 * firePower;
        double time = distance / bulletSpeed;
        double futureX = e.getX() + Math.sin(Math.toRadians(e.getDirection())) * e.getSpeed() * time;
        double futureY = e.getY() + Math.cos(Math.toRadians(e.getDirection())) * e.getSpeed() * time;

        // Turn gun toward predicted position
        double gunBearing = gunBearingTo(futureX, futureY);
        turnGunRight(gunBearing);

        // Move closer if far away
        if (distance > 250) {
            double bearing = calcBearing(directionTo(e.getX(), e.getY()));
            turnRight(bearing);
            forward(100);
        } else {
            // move offline if close to enemy
            double bearing = calcBearing(directionTo(e.getX(), e.getY()));
            turnRight(bearing + 90);
            forward(75);
        }

        // Fire when the gun is ready and lined up
        if (getGunHeat() == 0 && Math.abs(gunBearing) <= 5 && getEnergy() > 1) {
            fire(firePower);
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
