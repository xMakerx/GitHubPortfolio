package mc.decodedlogic.skybattlesuspawners.event;

import javax.annotation.Nonnull;

import org.bukkit.entity.Player;

import mc.decodedlogic.skybattlesuspawners.logging.Transaction;
import mc.decodedlogic.skybattlesuspawners.spawner.MobSpawner;

public class MobSpawnerTransactionEvent extends MobSpawnerEvent {
    
    private final Player P;
    private final Transaction T;
    
    public MobSpawnerTransactionEvent(@Nonnull MobSpawner spawner, @Nonnull Player player, @Nonnull Transaction trans) {
        super(spawner);
        this.P = player;
        this.T = trans;
    }
    
    /**
     * Fetches the {@link Player} who completed the transaction.
     * @return
     */
    
    public Player getPlayer() {
        return this.P;
    }
    
    /**
     * Fetches the details of the {@link Transaction} completed.
     * @return
     */
    
    public Transaction getTransaction() {
        return this.T;
    }

}
