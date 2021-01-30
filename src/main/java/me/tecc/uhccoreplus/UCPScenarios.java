package me.tecc.uhccoreplus;

import com.gmail.val59000mc.scenarios.Scenario;
import com.gmail.val59000mc.utils.UniversalMaterial;
import me.tecc.uhccoreplus.scenarios.*;

public final class UCPScenarios {
    private UCPScenarios() throws IllegalAccessException {
        throw new IllegalAccessException("You're not supposed to do that!");
    }

    public static final Scenario HEALTH_SWAP = new Scenario("health_swap", UniversalMaterial.APPLE, HealthSwapListener.class);
    public static final Scenario DOG_LOVER = new Scenario("dog_lover", UniversalMaterial.BONE, DogLoverListener.class);
    public static final Scenario MINE_THE_MOST = new Scenario("mine_the_most", UniversalMaterial.IRON_PICKAXE, MineTheMostListener.class);
    // these 2 have very similar names, but they are different
    public static final Scenario DEADWEIGHT = new Scenario("deadweight", UniversalMaterial.ANVIL, DeadweightListener.class);
    public static final Scenario DEAD_WEIGHT = new Scenario("dead_weight", UniversalMaterial.WOODEN_SWORD, DeadWeightListener.class);

    public static final Scenario[] SCENARIOS = new Scenario[]{
            HEALTH_SWAP,
            DOG_LOVER,
            MINE_THE_MOST,
            DEADWEIGHT,
            DEAD_WEIGHT
    };
}
