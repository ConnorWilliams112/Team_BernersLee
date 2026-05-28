import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;
import robocode.util.Utils;

// ------------------------------------------------------------------
// BudBot
// ------------------------------------------------------------------
public class BudBot extends Bot {

    public static void main(String[] args) {
        new BudBot().start();
    }

    //To implement:
        // Power level selection tree -> Aggregate score with weights
        // Targeting/accuracy logic
        // Movement logic -> Random walk, wall avoidance, etc.
        // Enemy tracking -> Keep track of scanned bots and their positions
        // Event handling -> React to being hit, hitting others
        
    //-----------------------------------------------------------------//
    //--------------------PRECOMPUTE/STATIC STUFF----------------------//
    //-----------------------------------------------------------------//
    private static final double[] SIN_TABLE = new double[360];
    private static final double[] COS_TABLE = new double[360];

    static {
        for (int i = 0; i < 360; i++) {
            double radians = Math.toRadians(i);
            SIN_TABLE[i] = Math.sin(radians);
            COS_TABLE[i] = Math.cos(radians);
        }
    }
    public double fastSin(double degrees) {
        return SIN_TABLE[(int) degrees % 360];
    }
    public double fastCos(double degrees) {
        return COS_TABLE[(int) degrees % 360];
    }
    //-----------------------------------------------------------------//
    //--------------------BOT MAIN LOOP--------------------------------//
    //-----------------------------------------------------------------//
    @Override
    public void run() {
        // Repeat while the bot is running
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        setTurnRadarRight(Double.POSITIVE_INFINITY);

        while (isRunning()) {
            scan();
            if (getEnemyCount() > 2) {
                // Implement melee tactics
                meleeTactics();
            } else {
                // Implement 1v1 tactics
                oneVsOneTactics();
            }
        }
    }
    //-----------------------------------------------------------------//
    //--------------------METHODS--------------------------------------//
    //-----------------------------------------------------------------//
    public void meleeTactics() {
        // TO DO: Implement melee tactics here -> Avoid damage, survive, opportunistic firing
        
    }

    public void oneVsOneTactics() {
        // TO DO: Implement 1v1 tactics here -> Maximize damage

    }

    //-----------------------------------------------------------------//
    //--------------------INTERRUPT HANDLERS---------------------------//
    //-----------------------------------------------------------------//

    // Other bot IDed
    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        setTurnRadarRight(2.0 * Utils.normalRelativeAngleDegrees(getHeading() + e.getBearing() - getRadarHeading()));
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
