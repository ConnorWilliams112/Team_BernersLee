import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;

// ------------------------------------------------------------------
// BudBot
// ------------------------------------------------------------------
// A sample bot originally made for Robocode by Mathew Nelson.
//
// Probably the first bot you will learn about.
// Moves in a seesaw motion and spins the gun around at each end.
// ------------------------------------------------------------------
public class BudBot extends Bot {

    // The main method starts our bot
    public static void main(String[] args) {
        new BudBot().start();
    }

    //To implement:
        //Melee vs Single checker -> Scan, add ID to table
            //If > 1, melee tactics
            //If 1, 1v1 tactics
        // Power level selection tree -> Aggregate score with weights
        // Targeting/accuracy logic
        // Movement logic -> Random walk, wall avoidance, etc.
        // Enemy tracking -> Keep track of scanned bots and their positions
        // Event handling -> React to being hit, hitting others
        
    // Pre-compute common angles
    private static final double[] SIN_TABLE = new double[360];
    private static final double[] COS_TABLE = new double[360];

    static {
        for (int i = 0; i < 360; i++) {
            double radians = Math.toRadians(i);
            SIN_TABLE[i] = Math.sin(radians);
            COS_TABLE[i] = Math.cos(radians);
        }
    }

    // Fast lookups
    public double fastSin(double degrees) {
        return SIN_TABLE[(int) degrees % 360];
    }
    public double fastCos(double degrees) {
        return COS_TABLE[(int) degrees % 360];
    }

    
    @Override
    public void run() {
        // Repeat while the bot is running
        while (isRunning()) {
            //TO DO: Implement movement and scanning loops here
        }
    }

    // Other bot IDed
    @Override
    public void onScannedBot(ScannedBotEvent e) {
        fire(1);
    }

    // We were hit by a bullet -> turn perpendicular to the bullet
    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        // Calculate the bearing to the direction of the bullet
        var bearing = calcBearing(e.getBullet().getDirection());

        // Turn 90 degrees to the bullet direction based on the bearing
        turnRight(90 - bearing);
    }

    @Override
    public void onHitWall(HitWallEvent e) {}

    @Override
    public void onHitBot(HitBotEvent e) {}

    @Override
}
