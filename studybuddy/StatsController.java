package studybuddy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Control class: StatsController
 * Stereotype: <<control>>
 */
public class StatsController {

    private StudyBuddyContext context;
    private String            currentUserId;

    public StatsController(StudyBuddyContext context, String currentUserId) {
        this.context       = context;
        this.currentUserId = currentUserId;
    }

    /**
     * Calculates and returns learning statistics.
     * (ponovnoZracunajStatistiko – zadnji korak osnovnega toka)
     */
    public StatsResult getStats() {
        List<StudySession> sessions = new java.util.ArrayList<>();
        for (StudySession s : context.getStudySessions())
            if (s.getUserId().equals(currentUserId))
                sessions.add(s);

        int totalMinutes = 0;
        for (StudySession s : sessions)
            totalMinutes += s.getDurationMinutes();

        int sessionCount = sessions.size();
        int avgMinutes   = sessionCount > 0 ? totalMinutes / sessionCount : 0;

        // Group by subject name
        Map<String, Integer> bySubject = new LinkedHashMap<>();
        for (StudySession s : sessions) {
            Subject sub  = context.findSubject(s.getSubjectId());
            String  name = (sub != null) ? sub.getName() : "—";
            bySubject.merge(name, s.getDurationMinutes(), Integer::sum);
        }

        // Find best subject
        String bestSubject = "—";
        int    bestMinutes = 0;
        for (Map.Entry<String, Integer> e : bySubject.entrySet()) {
            if (e.getValue() > bestMinutes) {
                bestMinutes = e.getValue();
                bestSubject = e.getKey();
            }
        }

        return new StatsResult(totalMinutes, avgMinutes, sessionCount, bySubject, bestSubject);
    }
}
