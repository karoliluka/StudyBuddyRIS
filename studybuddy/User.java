package studybuddy;

/**
 * Entity class: User
 * Stereotype: <<entity>>
 */
public class User {

    private String userId;
    private String firstName;
    private String lastName;
    private String email;

    public User(String userId, String firstName, String lastName, String email) {
        this.userId    = userId;
        this.firstName = firstName;
        this.lastName  = lastName;
        this.email     = email;
    }

    public String getUserId()    { return userId; }
    public String getFirstName() { return firstName; }
    public String getLastName()  { return lastName; }
    public String getEmail()     { return email; }

    @Override
    public String toString() {
        return firstName + " " + lastName;
    }
}
