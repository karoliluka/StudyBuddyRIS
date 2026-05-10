package studybuddy;

import java.util.Map;

/**
 * Helper class returned by StatsController.getStats()
 */
public class StatsResult {

    private int               totalMinutes;
    private int               avgMinutes;
    private int               sessionCount;
    private Map<String, Integer> bySubject;
    private String            bestSubject;

    public StatsResult(int totalMinutes, int avgMinutes, int sessionCount,
                       Map<String, Integer> bySubject, String bestSubject) {
        this.totalMinutes = totalMinutes;
        this.avgMinutes   = avgMinutes;
        this.sessionCount = sessionCount;
        this.bySubject    = bySubject;
        this.bestSubject  = bestSubject;
    }

    public int                  getTotalMinutes()  { return totalMinutes; }
    public int                  getAvgMinutes()    { return avgMinutes; }
    public int                  getSessionCount()  { return sessionCount; }
    public Map<String, Integer> getBySubject()     { return bySubject; }
    public String               getBestSubject()   { return bestSubject; }
}
