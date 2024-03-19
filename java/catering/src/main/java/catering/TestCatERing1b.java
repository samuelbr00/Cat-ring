package catering;
import catering.businesslogic.CatERing;
import catering.businesslogic.UseCaseLogicException;
import catering.businesslogic.event.EventInfo;
import catering.businesslogic.event.ServiceInfo;
import catering.businesslogic.menu.Menu;
import catering.businesslogic.menu.MenuException;
import catering.businesslogic.menu.Section;
import catering.businesslogic.recipe.Recipe;
import catering.persistence.PersistenceManager;
import javafx.collections.ObservableList;

import java.util.Arrays;
import java.util.Map;



/*

Viene effettuato un test di connessione al database.
Si simula il login di un utente chiamato "Lidia".
Viene creato un nuovo menu chiamato "Menu da Cancellare".
Vengono definiti tre diversi tipi di sezione per il menu: "Antipasti", "Primi", e "Secondi".
Viene recuperata una lista di ricette.
Le ricette vengono inserite nelle sezioni appropriate del menu.
Due ricette vengono inserite nel menu senza specificare la sezione, quindi presumibilmente vengono inserite come "altro".
Il menu viene pubblicato.
Viene stampato il menu creato.
Viene eseguito un test di cancellazione del menu creato.

* */
public class TestCatERing1b {
    public static void main(String[] args) {
        try {
            System.out.println("TEST DATABASE CONNECTION");
            PersistenceManager.testSQLConnection();
            CatERing.getInstance().getUserManager().fakeLogin("Lidia");
            System.out.println(CatERing.getInstance().getUserManager().getCurrentUser());

            Menu m = CatERing.getInstance().getMenuManager().createMenu("Menu da Cancellare");

            Section antipasti = CatERing.getInstance().getMenuManager().defineSection("Antipasti");
            Section primi = CatERing.getInstance().getMenuManager().defineSection("Primi");
            Section secondi = CatERing.getInstance().getMenuManager().defineSection("Secondi");

            ObservableList<Recipe> recipes = CatERing.getInstance().getRecipeManager().getRecipes();
            CatERing.getInstance().getMenuManager().insertItem(recipes.get(0), antipasti);
            CatERing.getInstance().getMenuManager().insertItem(recipes.get(1), antipasti);
            CatERing.getInstance().getMenuManager().insertItem(recipes.get(2), antipasti);
            CatERing.getInstance().getMenuManager().insertItem(recipes.get(6), secondi);
            CatERing.getInstance().getMenuManager().insertItem(recipes.get(7), secondi);
            CatERing.getInstance().getMenuManager().insertItem(recipes.get(3));
            CatERing.getInstance().getMenuManager().insertItem(recipes.get(4));

            CatERing.getInstance().getMenuManager().publish();
            System.out.println("\nMENU CREATO");
            System.out.println(m.testString());

            System.out.println("\nTEST DELETE");
            CatERing.getInstance().getMenuManager().deleteMenu(m);

        } catch (UseCaseLogicException | MenuException e) {
            System.out.println("Errore di logica nello use case");
        }
    }
}
