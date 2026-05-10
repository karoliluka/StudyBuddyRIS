package studybuddy;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Entity class: StudySession
 * Stereotype: <<entity>>
 */
public class StudySession {

    private int           studySessionId;
    private LocalDateTime startTime;
    private int           durationMinutes;
    private String        userId;
    private int           subjectId;

    public StudySession(int studySessionId, LocalDateTime startTime,
                        int durationMinutes, String userId, int subjectId) {
        this.studySessionId  = studySessionId;
        this.startTime       = startTime;
        this.durationMinutes = durationMinutes;
        this.userId          = userId;
        this.subjectId       = subjectId;
    }

    public int           getStudySessionId()  { return studySessionId; }
    public LocalDateTime getStartTime()       { return startTime; }
    public int           getDurationMinutes() { return durationMinutes; }
    public String        getUserId()          { return userId; }
    public int           getSubjectId()       { return subjectId; }

    public String getFormattedDate() {
        return startTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }
}
