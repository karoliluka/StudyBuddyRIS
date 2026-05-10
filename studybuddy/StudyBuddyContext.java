package studybuddy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity class: StudyBuddyContext
 * Stereotype: <<entity>>
 * Simulates database persistence with hardcoded seed data.
 */
public class StudyBuddyContext {

    private List<User>         users;
    private List<Subject>      subjects;
    private List<StudySession> studySessions;

    private int nextSessionId = 5;
    private int nextSubjectId = 4;

    public StudyBuddyContext() {
        // ---- Hardcoded seed data (2. del seminarske, točka 4) ----
        users = new ArrayList<>();
        users.add(new User("u001", "Luka",  "Karoli", "luka@example.com"));
        users.add(new User("u002", "Aljaz", "Smole",  "aljaz@example.com"));

        subjects = new ArrayList<>();
        subjects.add(new Subject(1, "Matematika",    "u001"));
        subjects.add(new Subject(2, "Fizika",        "u001"));
        subjects.add(new Subject(3, "Programiranje", "u001"));

        studySessions = new ArrayList<>();
        studySessions.add(new StudySession(1, LocalDateTime.now().minusDays(3),  60, "u001", 1));
        studySessions.add(new StudySession(2, LocalDateTime.now().minusDays(2),  45, "u001", 2));
        studySessions.add(new StudySession(3, LocalDateTime.now().minusHours(5), 90, "u001", 3));
        studySessions.add(new StudySession(4, LocalDateTime.now().minusDays(1),  30, "u001", 1));
    }

    // ---- Sessions ----
    public List<StudySession> getStudySessions() { return studySessions; }

    public StudySession findSession(int id) {
        for (StudySession s : studySessions)
            if (s.getStudySessionId() == id) return s;
        return null;
    }

    public void addSession(StudySession s) { studySessions.add(s); }

    public boolean removeSession(int id) {
        return studySessions.removeIf(s -> s.getStudySessionId() == id);
    }

    // ---- Subjects ----
    public List<Subject> getSubjects() { return subjects; }

    public Subject findSubject(int id) {
        for (Subject s : subjects)
            if (s.getSubjectId() == id) return s;
        return null;
    }

    public Subject findSubjectByName(String name, String userId) {
        for (Subject s : subjects)
            if (s.getName().equals(name) && s.getUserId().equals(userId)) return s;
        return null;
    }

    public void addSubject(Subject s) { subjects.add(s); }

    // ---- Users ----
    public List<User> getUsers() { return users; }

    public User findUser(String userId) {
        for (User u : users)
            if (u.getUserId().equals(userId)) return u;
        return null;
    }

    // ---- ID generators ----
    public int nextSessionId() { return nextSessionId++; }
    public int nextSubjectId() { return nextSubjectId++; }

    public void saveChanges() { /* In real app: persist to DB */ }
}
