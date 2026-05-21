import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;

public class VanJeckylson extends Bot {

    // // Appearance
    // setBodyColor(Color.BLACK);
    // setTurretColor(Color.fromRgb(0x00, 0x96, 0x32));
    // setRadarColor(Color.BLACK);
    // setBulletColor(Color.BLACK);
    // setScanColor(Color.BLACK);

    public static void main(String[] args) {
        new VanJeckylson().start();
    }

    @Override
    public void run() {
        while (isRunning()) {
            // your movement loop
        }
    }

    @Override
    public void onScannedBot(ScannedBotEvent e) {
        fire(1);
    }

    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        // TODO
    }

    @Override
    public void onHitWall(HitWallEvent e) {
        // TODO
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