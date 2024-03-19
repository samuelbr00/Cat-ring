package catering;

import catering.businesslogic.CatERing;
import catering.businesslogic.ShiftBoard.ShiftBoardException;
import catering.businesslogic.ShiftBoard.ShiftBoardInfo;
import catering.businesslogic.ShiftBoard.ShiftException;
import catering.businesslogic.ShiftBoard.ShiftInfo;
import catering.businesslogic.UseCaseLogicException;
import catering.businesslogic.event.EventException;
import catering.businesslogic.event.EventInfo;
import catering.businesslogic.event.ServiceInfo;
import catering.businesslogic.menu.MenuException;
import catering.businesslogic.task.SummarySheet;
import catering.businesslogic.task.SummarySheetException;
import catering.businesslogic.task.TaskInfo;
import catering.businesslogic.user.User;
import catering.businesslogic.user.UserException;
import catering.persistence.PersistenceManager;
import javafx.collections.ObservableList;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;

/*TEST assegna compito
Recuperiamo le info sul primo evento tramite GetEventInfo() di Catering
recuperiamo poi i servizi dell'evento e per il primo servizio dell'evento carichiamo i menù
impostiamo il foglio come modificabile
* aggiungiamo 3 turni al servizio corrente
assegna poi un turno ad un cuoco e  tramite sheet.getTask assegna un compito
L'utente viene caricato attraverso il meotodo LoadUser
Stampa poi il foglio aggiornato
*/

public class TestAssignTask {
    public static void main(String[] args) {
        try {
            System.out.println("TEST DATABASE CONNECTION");
            PersistenceManager.testSQLConnection();
            CatERing.getInstance().getUserManager().fakeLogin("Lidia");
            System.out.println(CatERing.getInstance().getUserManager().getCurrentUser());
            System.out.println("\nTEST ASSIGN TASK\n");
            ObservableList<EventInfo> events = CatERing.getInstance().getEventManager().getEventInfo();
            ObservableList<ServiceInfo> services = events.get(0).getServices();
            ServiceInfo s = services.get(0);
            s.setMenu(s.loadMenu(s.getMenu_id(), s.getConfirmed()));
            SummarySheet sheet = CatERing.getInstance().getEventManager().createEventSheets(events.get(0), s);
            System.out.println(sheet.toString() + ", elements:");
            for (TaskInfo r: sheet.getTasks()) {
                System.out.println(r.toString());
            }
            ArrayList<ShiftInfo> shifts = new ArrayList<>();
            s.setBoard(new ShiftBoardInfo(shifts, s.getId()));
            Date currentDate = new Date();
            Time time = new Time(currentDate.getTime());
            CatERing.getInstance().getEventManager().addShift(s, currentDate, "Manfria", time, time, true, true);
            CatERing.getInstance().getEventManager().addShift(s, currentDate, "Licata", time, time, true, false);
            CatERing.getInstance().getEventManager().addShift(s, currentDate, "Licata", time, time, false, true);
            System.out.println("\n" + s.getShiftBoard().toString());
            System.out.println("\nVoglio assegnare il cuoco Guido al turno:\n" + shifts.get(1) + "Per la task: " + sheet.getTasks().get(3));
            sheet.setModificable(true);
            User guido = User.loadUserById(5);
            CatERing.getInstance().getEventManager().assignTask(s, sheet.getTasks().get(3), shifts.get(1), guido);
            System.out.println("\n" + sheet.toString() + ", elements:");
            for (TaskInfo r: sheet.getTasks()) {
                System.out.println(r.toString());
            }
        } catch (UseCaseLogicException | MenuException | EventException | ShiftBoardException | SummarySheetException |
                 ShiftException | UserException e) {
            System.out.println("Errore di logica nello use case");
        }
    }
}
