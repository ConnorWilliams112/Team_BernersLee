import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;
import dev.robocode.tankroyale.botapi.graphics.Color;
import java.util.Random;

public class VanJeckylson extends Bot {

    int moveDirection = 1;
    int scansSinceDirectionChange = 0;
    int scansBeforeDirectionChange = 4;
    int scansSinceHitDecay = 0;
    int scansSinceLastSeen = 0;
    int hitCount = 0;
    int wallHitCount = 0;
    boolean defensiveMode = false;
    double lastEnemyX = 0;
    double lastEnemyY = 0;
    boolean lastEnemySeen = false;
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
            // Forget old target info if scans go cold
            scansSinceLastSeen++;
            if (scansSinceLastSeen > 12) {
                lastEnemySeen = false;
            }

            if (lastEnemySeen) {
                // Sweep near the last enemy before searching again
                double gunBearing = gunBearingTo(lastEnemyX, lastEnemyY);
                if (Math.abs(gunBearing) > 20) {
                    turnGunLeft(Math.copySign(20, gunBearing));
                } else {
                    turnGunLeft(20 * moveDirection);
                }
            } else {
                turnGunLeft(20);
            }
        }
    }

    @Override
    public void onScannedBot(ScannedBotEvent e) {
        double distance = distanceTo(e.getX(), e.getY());
        double speed = Math.abs(e.getSpeed());

        // Remember last scanned enemy
        lastEnemyX = e.getX();
        lastEnemyY = e.getY();
        lastEnemySeen = true;
        scansSinceLastSeen = 0;

        // Slowly leave defensive mode if hits stop coming
        scansSinceHitDecay++;
        if (scansSinceHitDecay >= 6 && hitCount > 0) {
            hitCount--;
            scansSinceHitDecay = 0;
            defensiveMode = hitCount >= 3;
        }

        // Pick fire power
        double firePower = pickFirePower(distance, speed);

        // Predict where the enemy will be
        double bulletSpeed = 20 - 3 * firePower;
        double time = getPredictionTime(distance, bulletSpeed, speed);
        double futureX = e.getX();
        double futureY = e.getY();

        // Use less lead against fast close enemies
        if (speed >= 1 && !(speed > 4 && distance < 300)) {
            futureX += Math.sin(Math.toRadians(e.getDirection())) * e.getSpeed() * time;
            futureY += Math.cos(Math.toRadians(e.getDirection())) * e.getSpeed() * time;
        }

        // Turn gun toward predicted position
        double gunBearing = gunBearingTo(futureX, futureY);
        turnGunLeft(gunBearing);

        // Fire when the gun is ready and lined up
        double aimLimit;
        if (getEnemyCount() > 2) {
            aimLimit = 5;
        } else {
            aimLimit = 3;
        }

        if (getGunHeat() == 0 && Math.abs(gunBearing) <= aimLimit && getEnergy() > 1) {
            fire(firePower);
        }

        // Only move every few scans so movement commands actually complete
        scansSinceDirectionChange++;
        if (scansSinceDirectionChange >= scansBeforeDirectionChange) {
            moveDirection *= -1;
            scansSinceDirectionChange = 0;
            scansBeforeDirectionChange = 3 + random.nextInt(4);
            double bearing = calcBearing(directionTo(e.getX(), e.getY()));
            handleMovement(bearing, distance, speed);
        }

        // Rescan more often when enemies are close or lined up
        if (distance < 400 || Math.abs(gunBearing) <= 10) {
            rescan();
        }
    }

    private double pickFirePower(double distance, double speed) {
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

        return firePower;
    }

    private double getPredictionTime(double distance, double bulletSpeed, double speed) {
        if (speed > 4) {
            return Math.min(distance / bulletSpeed, 5);
        }

        return Math.min(distance / bulletSpeed, 12);
    }

    private void handleMovement(double bearing, double distance, double speed) {
        // called by onScannedBot() to determine movement branching
        if (isNearWall()) {
            // Turn toward the center before hitting a wall
            avoidWall();
        } else if (distance < 160) {
            // Turn away if an enemy gets too close
            turnRight(bearing + 180);
            forward(130);
        } else if (defensiveMode && speed > 4 && distance < 400) {
            // Keep distance if fast enemies keep hitting us
            turnRight(bearing + 170);
            forward(180);
        } else if (defensiveMode && speed > 4) {
            // Strafe fast enemies once we have space
            turnRight(bearing + 100);
            forward(140 * moveDirection);
        } else if (speed > 4 && distance < 250) {
            // Back away from fast enemies that get too close
            turnRight(bearing + 160);
            forward(160);
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
    }

    private boolean isNearWall() {
        return getX() < 80 || getY() < 80 || getX() > getArenaWidth() - 80 || getY() > getArenaHeight() - 80;
    }

    private void avoidWall() {
        double centerX = getArenaWidth() / 2.0;
        double centerY = getArenaHeight() / 2.0;
        double bearingToCenter = calcBearing(directionTo(centerX, centerY));

        turnRight(bearingToCenter);
        forward(120);
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
        back(120);
    }

    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        // Count hits so we can adapt our movement
        hitCount++;
        scansSinceHitDecay = 0;
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
        // Use our heading to turn away from the wall
        wallHitCount++;
        moveDirection *= -1;

        back(120);
        turnRight(100 * moveDirection);

        // Add a bigger turn if we are stuck near a wall
        if (wallHitCount >= 2) {
            turnRight(60 + random.nextInt(60));
            wallHitCount = 0;
        }
    }

    @Override
    public void onDeath(DeathEvent e) {
        // reset adaptive movement for next round
        resetRoundState();
    }

    @Override
    public void onWonRound(WonRoundEvent e) {
        // reset adaptive movement for next round
        resetRoundState();
    }

    private void resetRoundState() {
        hitCount = 0;
        wallHitCount = 0;
        scansSinceHitDecay = 0;
        scansSinceLastSeen = 0;
        defensiveMode = false;
        lastEnemySeen = false;
    }

}
