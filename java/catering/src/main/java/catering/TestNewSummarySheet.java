package catering;

import catering.businesslogic.CatERing;
import catering.businesslogic.UseCaseLogicException;
import catering.businesslogic.event.EventException;
import catering.businesslogic.event.EventInfo;
import catering.businesslogic.event.ServiceInfo;
import catering.businesslogic.menu.MenuException;
import catering.businesslogic.task.SummarySheet;
import catering.businesslogic.task.TaskInfo;
import catering.persistence.PersistenceManager;
import javafx.collections.ObservableList;

/*Recuperiamo le info sul primo evento tramite GetEventInfo() di Catering
recuperiamo poi i servizi dell'evento
carica il menù associato e lo imposta nel servizio
Creiamo un foglio riepilogativo per l'evento selezionato
e stampiamo l'oggetto appena creato
*
* */
public class TestNewSummarySheet {
    public static void main(String[] args) {
        try {
            System.out.println("TEST DATABASE CONNECTION");
            PersistenceManager.testSQLConnection();
            CatERing.getInstance().getUserManager().fakeLogin("Lidia");
            System.out.println(CatERing.getInstance().getUserManager().getCurrentUser());
            System.out.println("\nTEST CREATE SUMMARY SHEET");
            ObservableList<EventInfo> events = CatERing.getInstance().getEventManager().getEventInfo();
            ObservableList<ServiceInfo> services = events.get(0).getServices();
            ServiceInfo s = services.get(0);
            s.setMenu(s.loadMenu(s.getMenu_id(), s.getConfirmed()));
            SummarySheet sheet = CatERing.getInstance().getEventManager().createEventSheets(events.get(0), s);
            System.out.println(s.testSheetCreated());
            System.out.println(sheet.toString() + ", elements:\n");
            for (TaskInfo r: sheet.getTasks()) {
                System.out.println(r.toString());
            }
        } catch (UseCaseLogicException | MenuException | EventException e) {
            System.out.println("Errore di logica nello use case");
        }
    }
}
