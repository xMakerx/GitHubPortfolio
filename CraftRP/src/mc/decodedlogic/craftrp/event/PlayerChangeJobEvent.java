package mc.decodedlogic.craftrp.event;

import mc.decodedlogic.craftrp.job.Job;
import mc.decodedlogic.craftrp.player.CRPPlayer;

public class PlayerChangeJobEvent extends CRPPlayerEvent {
    
    protected Job newJob;
    protected final Job LAST_JOB;
    
    public PlayerChangeJobEvent(CRPPlayer p, Job newJob2, Job lastJob) {
        super(p);
        this.newJob = newJob2;
        this.LAST_JOB = lastJob;
    }
    
    public void setNewJob(Job job) {
        if(job != newJob) {
            this.newJob = job;
            this.PLAYER.setJob(job);
        }
    }
    
    public Job getNewJob() {
        return this.newJob;
    }
    
    public Job getLastJob() {
        return this.LAST_JOB;
    }

}
