package studybuddy;

/**
 * Entity class: Subject
 * Stereotype: <<entity>>
 */
public class Subject {

    private int    subjectId;
    private String name;
    private String userId;

    public Subject(int subjectId, String name, String userId) {
        this.subjectId = subjectId;
        this.name      = name;
        this.userId    = userId;
    }

    public int    getSubjectId() { return subjectId; }
    public String getName()      { return name; }
    public String getUserId()    { return userId; }

    @Override
    public String toString() { return name; }
}
