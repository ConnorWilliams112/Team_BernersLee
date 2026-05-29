import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;
import dev.robocode.tankroyale.botapi.graphics.Color;
import java.util.Random;

public class VanJeckylson extends Bot {

    int moveDirection = 1;
    int scansSinceDirectionChange = 0;
    int scansBeforeDirectionChange = 4;
    int hitCount = 0;
    boolean defensiveMode = false;
    Random random = new Random();

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

        // scan loop
        while (isRunning()) {
            turnGunLeft(45);
        }
    }

    @Override
    public void onScannedBot(ScannedBotEvent e) {
        double distance = distanceTo(e.getX(), e.getY());
        double speed = Math.abs(e.getSpeed());

        // Change directions sometimes
        scansSinceDirectionChange++;
        if (scansSinceDirectionChange >= scansBeforeDirectionChange) {
            moveDirection *= -1;
            scansSinceDirectionChange = 0;
            scansBeforeDirectionChange = 3 + random.nextInt(4);
        }

        // Pick fire power
        double firePower;
        if (speed > 4 && distance < 250) {
            firePower = 3;
        } else if (speed > 4) {
            firePower = 1.5;
        } else if (speed < 1 && distance < 300) {
            firePower = 3;
        } else if (distance < 150) {
            firePower = 2;
        } else if (distance < 350) {
            firePower = 1.5;
        } else {
            firePower = 1;
        }

        // Save energy if low
        if (getEnergy() < 10) {
            firePower = 0.5;
        }

        // Predict where the enemy will be
        double bulletSpeed = 20 - 3 * firePower;
        double time;
        if (speed > 4) {
            time = Math.min(distance / bulletSpeed, 5);
        } else {
            time = Math.min(distance / bulletSpeed, 12);
        }
        double futureX = e.getX();
        double futureY = e.getY();

        // Lead moving enemies, but aim straight at slow enemies
        if (speed >= 1) {
            futureX += Math.sin(Math.toRadians(e.getDirection())) * e.getSpeed() * time;
            futureY += Math.cos(Math.toRadians(e.getDirection())) * e.getSpeed() * time;
        }

        // Turn gun toward predicted position
        double gunBearing = gunBearingTo(futureX, futureY);
        turnGunLeft(gunBearing);

        // Fire when the gun is ready and lined up
        double aimError = gunBearingTo(futureX, futureY);
        if (getGunHeat() == 0 && Math.abs(aimError) <= 5 && getEnergy() > 1) {
            fire(firePower);
        }

        // Circle around the enemy
        double bearing = calcBearing(directionTo(e.getX(), e.getY()));
        if (defensiveMode && speed > 4 && distance < 400) {
            // Keep distance if fast enemies keep hitting us
            turnRight(bearing + 170);
            forward(180 * moveDirection);
        } else if (defensiveMode && speed > 4) {
            // Strafe fast enemies once we have space
            turnRight(bearing + 100);
            forward(140 * moveDirection);
        } else if (speed > 4 && distance < 250) {
            // Back away from fast enemies that get too close
            turnRight(bearing + 160);
            forward(160 * moveDirection);
        } else if (distance > 500) {
            turnRight(bearing + 45);
            forward(90 * moveDirection);
        } else if (distance > 180) {
            turnRight(bearing + 90);
            forward(90 * moveDirection);
        } else {
            turnRight(bearing + 110);
            forward(60 * moveDirection);
        }

        rescan();
    }

    @Override
    public void onHitBot(HitBotEvent e) {
        // aim and fire hard when touching enemy
        double gunBearing = gunBearingTo(e.getX(), e.getY());
        turnGunLeft(gunBearing);

        if (getGunHeat() == 0 && getEnergy() > 3) {
            fire(3);
        }

        // back up and switch direction
        moveDirection *= -1;
        back(50);
    }

    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        // Count hits so we can adapt our movement
        hitCount++;
        if (hitCount >= 3) {
            defensiveMode = true;
        }

        // switch directions when hit
        moveDirection *= -1;
        var bearing = calcBearing(e.getBullet().getDirection());
        turnRight(90 - bearing + 25 * moveDirection);
        forward(110 * moveDirection);
    }

    @Override
    public void onHitWall(HitWallEvent e) {
        // back up and turn away
        moveDirection *= -1;
        back(100);
        turnRight(90);
    }

    @Override
    public void onDeath(DeathEvent e) {
        // reset adaptive movement for next round
        hitCount = 0;
        defensiveMode = false;
    }

    @Override
    public void onWonRound(WonRoundEvent e) {
        // reset adaptive movement for next round
        hitCount = 0;
        defensiveMode = false;
    }

}
