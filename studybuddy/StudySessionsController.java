package studybuddy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Control class: StudySessionsController
 * Stereotype: <<control>>
 */
public class StudySessionsController {

    private StudyBuddyContext context;
    private String            currentUserId;

    public StudySessionsController(StudyBuddyContext context, String currentUserId) {
        this.context       = context;
        this.currentUserId = currentUserId;
    }

    /** Returns all sessions for the current user. */
    public List<StudySession> index() {
        List<StudySession> result = new ArrayList<>();
        for (StudySession s : context.getStudySessions())
            if (s.getUserId().equals(currentUserId))
                result.add(s);
        return result;
    }

    /** Returns a single session by id. */
    public StudySession details(int id) {
        return context.findSession(id);
    }

    /** Creates a session from timer result (subjectName + durationMinutes). */
    public boolean createFromTimer(String subjectName, int durationMinutes) {
        if (durationMinutes <= 0) return false;

        Subject subject = context.findSubjectByName(subjectName, currentUserId);
        if (subject == null) return false;

        StudySession session = new StudySession(
            context.nextSessionId(),
            LocalDateTime.now(),
            durationMinutes,
            currentUserId,
            subject.getSubjectId()
        );
        context.addSession(session);
        context.saveChanges();
        return true;
    }

    /** Deletes a session by id. */
    public boolean deleteConfirmed(int id) {
        boolean removed = context.removeSession(id);
        if (removed) context.saveChanges();
        return removed;
    }
}
