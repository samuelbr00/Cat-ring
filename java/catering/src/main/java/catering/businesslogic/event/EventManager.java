package catering.businesslogic.event;


import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import catering.businesslogic.CatERing;
import catering.businesslogic.ShiftBoard.ShiftBoardException;
import catering.businesslogic.ShiftBoard.ShiftBoardInfo;
import catering.businesslogic.ShiftBoard.ShiftException;
import catering.businesslogic.ShiftBoard.ShiftInfo;
import catering.businesslogic.UseCaseLogicException;
import catering.businesslogic.menu.Menu;
import catering.businesslogic.menu.MenuException;
import catering.businesslogic.recipe.Recipe;
import catering.businesslogic.task.SummarySheet;
import catering.businesslogic.task.SummarySheetException;
import catering.businesslogic.task.TaskInfo;
import catering.businesslogic.user.User;
import catering.businesslogic.user.UserException;
import javafx.collections.ObservableList;

public class EventManager {
    private EventInfo currentEvent;
    private ArrayList<EventEventReceiver> receivers;

    public EventManager() {
        receivers = new ArrayList<>();
    }

    public ObservableList<EventInfo> getEventInfo() {
        return EventInfo.loadAllEventInfo();
    }

    public void addReceiver(EventEventReceiver er) {this.receivers.add(er);}

    public void removeReceiver(EventEventReceiver er) {this.receivers.remove(er);}
    private void setCurrentEvent(EventInfo event) {
        this.currentEvent = event;
    }


    /* metodo che aggiunge una mansione di cucina
    * controlla che il foglio riepilogativo non sia null ,fatto ciò  crea una nuova task (un nuovo compito che
    * equivale quindi ad una ricetta essitente
    * e lo aggiunge al foglio riepilogativo)*/
    public void addKitchenTask(ServiceInfo service, Recipe kitchenTask) throws SummarySheetException {
        SummarySheet sheet = service.getSummarySheet();
        if (sheet == null) {
            throw new SummarySheetException();
        }
        TaskInfo t = sheet.createTask(kitchenTask);
        this.notifyAddKitchenTask(sheet, t);
    }


    /* metodo che aggiunge un turno
     * controlla che il tabellone dei turni non sia null ,fatto ciò  crea una nuovo turno
     *  con i relativi attributi e lo aggiunge al tabellone
     * e lo aggiunge al foglio riepilogativo)*/

    public void addShift(ServiceInfo service, Date date, String place, Time start, Time end, boolean preparationShift, boolean serviceShift) throws ShiftBoardException {
        ShiftBoardInfo board = service.getShiftBoard();
        if (board == null) {
            throw new ShiftBoardException();
        }
        ShiftInfo newShift = board.createShift(date, place, start, end, preparationShift, serviceShift);
        this.notifyAddShift(board, newShift);
    }


    /* metodo che assegna un compito
     * controlla che il foglio riepilogativo non sia null ,
     * che non sia vuoto
     * che il foglio riepilogativo sia modificabile
     * che il turno non sia null e che l'user  cui assegnare
     * il compito sia un cuoco (quindi checkcook == true)
     * fatto ciò assegna il compito ad un cuoco e ad un turno*/
    public void assignTask(ServiceInfo service, TaskInfo task, ShiftInfo shift, User cook) throws SummarySheetException, ShiftException, UserException {
        SummarySheet sheet = service.getSummarySheet();
        if (sheet == null) {
            throw new SummarySheetException();
        }
        if (!sheet.contains(task)) {
            throw new SummarySheetException();
        }
        if (!sheet.modificable()) {
            throw new SummarySheetException();
        }
        if (shift == null) {
            throw new ShiftException();
        }
        if (sheet.checkCook(cook)) {
            task.setShift(shift);
            task.assignCook(cook);
        }
        this.notifyAssignTask(task, sheet);
    }

    /* metodo che aggiorna un compito
     * controlla che il foglio riepilogativo non sia null ,
     * che non contenga altri compiti
     * che il foglio riepilogativo sia modificabile
     * che il turno non sia null e che sia assegnato ad un cuoco
     * fatto ciò posso modificare il cuoco ed il turno*/
    public void updateTask(ServiceInfo service, TaskInfo task, ShiftInfo shift, User cook) throws SummarySheetException, UserException {
        SummarySheet sheet = service.getSummarySheet();
        if (sheet == null) {
            throw new SummarySheetException();
        }
        if (!sheet.contains(task)) {
            throw new SummarySheetException();
        }
        if (!sheet.modificable()) {
            throw new SummarySheetException();
        }
        if(cook != null) {
            if (sheet.checkCook(cook)) {
                task.assignCook(cook);
            } else {
                throw new UserException();
            }
        }
        if(shift != null) {
            task.setShift(shift);
        }
        this.notifyUpdateTask(task);
    }

    /* metodo che rimuove un compito
     * controlla che il foglio riepilogativo non sia null ,
     * che il foglio riepilogativo sia modificabile
     * fatto ciò posso rimuovere il compito*/
    public void removeTask(ServiceInfo service, TaskInfo task) throws SummarySheetException {
        SummarySheet sheet = service.getSummarySheet();
        if (sheet == null) {
            throw new SummarySheetException();
        }
        if (sheet.lenght() < 2) {  // perchè foglio riepilogativo conteine da 1 a n compiti
            throw new SummarySheetException();
        }
        if (!sheet.modificable()) {
            throw new SummarySheetException();
        }
        sheet.removeTask(task);
        this.notifyRemoveTask(task);
    }


    /* metodo che rimuove un turno
     * controlla che il tabellone dei turni non sia null ,fatto ciò rimuove
     * il turno dal tabellone*/
    public void removeShift(ShiftInfo shift, ServiceInfo service) throws ShiftBoardException {
        ShiftBoardInfo board = service.getShiftBoard();
        if(board == null) {
            throw new ShiftBoardException();
        }
        board.removeShift(shift);
        this.notifyRemoveShift(shift);
    }


    /* metodo che aggiunge info al foglio riepilogativo
     * controlla che il foglio riepilogativo non sia null ,
     * che contenga compiti
     * che il foglio riepilogativo sia modificabile
     * fatto ciò posso aggiungere informazioni (es tempo stimato , porzioni )*/

    public void addInfo(ServiceInfo service, TaskInfo task, int stimedTime, int portions) throws SummarySheetException {
        SummarySheet sheet = service.getSummarySheet();
        if (sheet == null) {
            throw new SummarySheetException();
        }
        if (!sheet.contains(task)) {
            throw new SummarySheetException();
        }
        if (!sheet.modificable()) {
            throw new SummarySheetException();
        }
        task.setPortions(portions);
        task.setStimedTime(stimedTime);
        this.notifyAddInfo(task);
    }


    /*
* Questo metodo crea un nuovo foglio riepilogativo
* solo se l'utente è uno chef , il menù è approvato
* e l'evento è in corso
* per ogni ricetta del menù previsto in una mansione
* crea un compito
*
* */
    public SummarySheet createEventSheets(EventInfo event, ServiceInfo service) throws UseCaseLogicException, MenuException, EventException {
        User user = CatERing.getInstance().getUserManager().getCurrentUser();
        Menu menu = service.getMenu();
        if (!user.isChef()) {
            throw new UseCaseLogicException("user non è uno chef");
        }
        if (!menu.isApproved()) {
            throw new MenuException("menu non approvato");
        }
        if (!event.isInProgress()) {
            throw new EventException("evento non in corso");
        }
        setCurrentEvent(event);
        
        service.createSummarySheet();

        ArrayList<Recipe> kitchenTasks = menu.getKitchenTask();
        for (Recipe recipe : kitchenTasks) {
            service.getSummarySheet().createTask(recipe);
        }
        this.notifySummarySheetCreated(service.getSummarySheet());
        return service.getSummarySheet();
    }

    private void notifySummarySheetCreated(SummarySheet s) {
        for (EventEventReceiver er : this.receivers) {
            er.updateSummarySheetCreated(this.currentEvent, s);
        }
    }

    private void notifyAddKitchenTask(SummarySheet s, TaskInfo t) {
        for (EventEventReceiver er : this.receivers) {
            er.updateAddKitchenTask(s, t);
        }
    }

    private void notifyAddShift(ShiftBoardInfo sb, ShiftInfo s) {
        for (EventEventReceiver er : this.receivers) {
            er.updateAddShift(sb, s);
        }
    }

    private void notifyRemoveShift(ShiftInfo s) {
        for (EventEventReceiver er : this.receivers) {
            er.updateRemoveShift(s);
        }
    }

    private void notifyAssignTask(TaskInfo t, SummarySheet s) {
        for (EventEventReceiver er : this.receivers) {
            er.updateAssignTask(t, s);
        }
    }

    private void notifyRemoveTask(TaskInfo t) {
        for (EventEventReceiver er : this.receivers) {
            er.updateRemoveTask(t);
        }
    }

    private void notifyUpdateTask(TaskInfo t) {
        for (EventEventReceiver er : this.receivers) {
            er.updateUpdateTask(t);
        }
    }

    private void notifyAddInfo(TaskInfo t) {
        for (EventEventReceiver er : this.receivers) {
            er.updateAddInfo(t);
        }
    }
}
