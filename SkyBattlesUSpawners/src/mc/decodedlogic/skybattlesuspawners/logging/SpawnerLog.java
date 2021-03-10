package mc.decodedlogic.skybattlesuspawners.logging;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class SpawnerLog {
    
    protected ModificationRecord createRecord;
    protected ModificationRecord deleteRecord;
    protected List<Record> records;
    
    public SpawnerLog() {
        this.records = new ArrayList<Record>();
        this.createRecord = null;
        this.deleteRecord = null;
    }
    
    public SpawnerLog(List<Record> parseRecords, ModificationRecord createRecord, ModificationRecord deleteRecord) {
        super();
        this.createRecord = createRecord;
        this.deleteRecord = deleteRecord;
    }
    
    public void setCreationRecord(@Nonnull ModificationRecord newRecord) {
        this.createRecord = newRecord;
    }
    
    public ModificationRecord getCreationRecord() {
        return this.createRecord;
    }
    
    public void setDeletionRecord(@Nonnull ModificationRecord newRecord) {
        this.deleteRecord = newRecord;
    }
    
    public ModificationRecord getDeletionRecord() {
        return this.deleteRecord;
    }
    
    public List<Record> getRecords() {
        return this.records;
    }
    
}
