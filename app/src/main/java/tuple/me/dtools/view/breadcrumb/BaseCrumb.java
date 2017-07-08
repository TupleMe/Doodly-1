package tuple.me.dtools.view.breadcrumb;

/**
 * Created by gokul-4192 on 0011 11-Mar-17.
 */
public class BaseCrumb {
    public int scrollPox;
    private String title;

    public BaseCrumb(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public boolean isValidCrumb() {
        return true;
    }
}
