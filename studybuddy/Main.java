package studybuddy;

import javax.swing.SwingUtilities;

/**
 * Entry point for StudyBuddy application.
 * Wires all classes together and launches the UI.
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            // Current user (predpogoj: uporabnik je prijavljen)
            final String CURRENT_USER_ID = "u001";

            // Bootstrap – ustvari kontekst in kontrolerje
            StudyBuddyContext       context       = new StudyBuddyContext();
            StudySessionsController sessionCtrl   = new StudySessionsController(context, CURRENT_USER_ID);
            StatsController         statsCtrl     = new StatsController(context, CURRENT_USER_ID);

            // Odpri UI (boundary razred)
            new StudySessionUI(sessionCtrl, statsCtrl, context, CURRENT_USER_ID);
        });
    }
}
