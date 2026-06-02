import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;
import dev.robocode.tankroyale.botapi.graphics.Color;

import java.util.Random;

public class BudBot extends Bot {

    // ---------------- CONSTANTS ----------------
    private static final double WALL_FORCE = 5000;

    private static final double ANTIGRAV_WEIGHT_1V1 = 0.2;
    private static final double ANTIGRAV_WEIGHT_MELEE = 0.7;

    private static final double ENEMY_FORCE_1V1 = 2000;
    private static final double ENEMY_FORCE_MELEE = 4000;

    private static final double CLOSE_DISTANCE = 160;
    private static final double FAST_SPEED = 4;

    private static final int MAX_ENEMIES_TRACKED = 10;
    private static final int NEAREST_COUNT = 5;

    // ---------------- STATE ----------------
    private int moveDirection = 1;
    private int hitCount = 0;
    private boolean defensiveMode = false;

    private final Random random = new Random();

    // ---------------- ENEMY TRACKING ----------------
    private static class Enemy {
        double x, y, distSq;
    }

    private final Enemy[] enemies = new Enemy[MAX_ENEMIES_TRACKED];
    private int enemyCountTracked = 0;

    // ---------------- MAIN ----------------
    public static void main(String[] args) {
        new BudBot().start();
    }

    @Override
    public void run() {

        setBodyColor(Color.BLACK);
        setTurretColor(Color.fromRgb(0x00, 0x96, 0x32));
        setRadarColor(Color.BLACK);

        setAdjustGunForBodyTurn(true);

        for (int i = 0; i < MAX_ENEMIES_TRACKED; i++) {
            enemies[i] = new Enemy();
        }

        while (isRunning()) {
            turnGunLeft(20);
        }
    }

    // ---------------- SCAN ----------------
    @Override
    public void onScannedBot(ScannedBotEvent e) {

        double ex = e.getX();
        double ey = e.getY();

        double dx = getX() - ex;
        double dy = getY() - ey;
        double distSq = dx * dx + dy * dy;

        // store enemy (cyclic)
        Enemy en = enemies[enemyCountTracked % MAX_ENEMIES_TRACKED];
        en.x = ex;
        en.y = ey;
        en.distSq = distSq;
        enemyCountTracked++;

        double distance = Math.sqrt(distSq);
        double speed = Math.abs(e.getSpeed());

        // -------- TARGETING --------
        double firePower = pickFirePower(distance, speed);
        double bulletSpeed = 20 - 3 * firePower;
        double time = Math.min(distance / bulletSpeed, 10);

        double futureX = ex + Math.sin(Math.toRadians(e.getDirection())) * e.getSpeed() * time;
        double futureY = ey + Math.cos(Math.toRadians(e.getDirection())) * e.getSpeed() * time;

        double gunBearing = gunBearingTo(futureX, futureY);
        turnGunLeft(gunBearing);

        if (getGunHeat() == 0 && Math.abs(gunBearing) < 4 && getEnergy() > 1) {
            fire(firePower);
        }

        // -------- MOVEMENT --------
        double bearing = calcBearing(directionTo(ex, ey));

        double[] vjMove = computeVJMovement(bearing, distance, speed);

        double weight = getEnemyCount() > 2 ? ANTIGRAV_WEIGHT_MELEE : ANTIGRAV_WEIGHT_1V1;
        double enemyForce = getEnemyCount() > 2 ? ENEMY_FORCE_MELEE : ENEMY_FORCE_1V1;

        double[] agMove = computeMultiEnemyAntiGrav(enemyForce);

        double finalTurn = vjMove[0] + agMove[0] * weight;
        double finalMove = vjMove[1] + agMove[1] * weight;

        setTurnRight(finalTurn);
        setForward(finalMove);

        if (random.nextDouble() < 0.1) {
            moveDirection *= -1;
        }

        if (distance < 400) {
            rescan();
        }
    }

    // ---------------- VJ LOGIC ----------------
    private double[] computeVJMovement(double bearing, double distance, double speed) {

        if (distance < CLOSE_DISTANCE) {
            return new double[]{bearing + 180, 120};
        }

        if (getEnemyCount() > 2) {
            return new double[]{bearing + 90 + (defensiveMode ? 20 : 0), 100 * moveDirection};
        } else {
            if (defensiveMode && speed > FAST_SPEED) {
                return new double[]{bearing + 110, 120 * moveDirection};
            } else {
                return new double[]{bearing + 90, 90 * moveDirection};
            }
        }
    }

    // ---------------- MULTI ENEMY ANTIGRAV ----------------
    private double[] computeMultiEnemyAntiGrav(double enemyForceWeight) {

        double x = getX();
        double y = getY();

        double xForce = 0;
        double yForce = 0;

        double width = getArenaWidth();
        double height = getArenaHeight();

        // ----- WALL FORCES -----
        xForce += WALL_FORCE / Math.pow(width - x, 2);
        xForce -= WALL_FORCE / Math.pow(x, 2);

        yForce += WALL_FORCE / Math.pow(height - y, 2);
        yForce -= WALL_FORCE / Math.pow(y, 2);

        // ----- NEAREST 5 ENEMIES -----
        int count = Math.min(enemyCountTracked, MAX_ENEMIES_TRACKED);

        for (int i = 0; i < count; i++) {

            Enemy e = enemies[i];
            if (e.distSq == 0) continue;

            int closerCount = 0;
            for (int j = 0; j < count; j++) {
                if (enemies[j].distSq < e.distSq) closerCount++;
                if (closerCount >= NEAREST_COUNT) break;
            }

            if (closerCount >= NEAREST_COUNT) continue;

            double dx = x - e.x;
            double dy = y - e.y;
            double distSq = e.distSq;

            if (distSq < 1) continue;

            double weight = enemyForceWeight * Math.min(1.0, 90000.0 / distSq);

            xForce += (dx / distSq) * weight;
            yForce += (dy / distSq) * weight;
        }

        // ----- CONVERT TO MOVEMENT -----
        double angle = Math.atan2(yForce, xForce);

        double turn = Math.toDegrees(angle) - getDirection();
        turn = normalizeBearing(turn);

        double magnitude = Math.min(80, Math.hypot(xForce, yForce) * 200);

        if (Math.abs(turn) > 90) {
            turn += (turn > 0) ? -180 : 180;
            magnitude *= -1;
        }

        return new double[]{turn, magnitude};
    }

    // ---------------- HELPERS ----------------
    private double normalizeBearing(double angle) {
        while (angle > 180) angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
    }

    // ---------------- FIREPOWER ----------------
    private double pickFirePower(double distance, double speed) {
        if (speed > FAST_SPEED && distance < 250) return 3;
        if (distance < 150) return 2;
        if (distance < 350) return 1.5;
        return 1;
    }

    // ---------------- EVENTS ----------------
    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        hitCount++;
        if (hitCount >= 3) defensiveMode = true;

        moveDirection *= -1;

        var bearing = calcBearing(e.getBullet().getDirection());
        turnRight(90 - bearing + 25 * moveDirection);
        forward(110 * moveDirection);
    }

    @Override
    public void onHitWall(HitWallEvent e) {
        moveDirection *= -1;
        back(100);
        turnRight(60 + random.nextInt(60));
    }

    @Override
    public void onHitBot(HitBotEvent e) {
        double gunBearing = gunBearingTo(e.getX(), e.getY());
        turnGunLeft(gunBearing);

        if (getGunHeat() == 0 && getEnergy() > 2) {
            fire(3);
        }

        moveDirection *= -1;
        back(100);
    }

    @Override
    public void onDeath(DeathEvent e) {
        reset();
    }

    @Override
    public void onWonRound(WonRoundEvent e) {
        reset();
    }

    private void reset() {
        hitCount = 0;
        defensiveMode = false;
        enemyCountTracked = 0;
    }
}