package labelingStudy.nctu.minuku.manager;

import java.util.HashMap;
import java.util.Map;

public class SituationManager {

    Map<String,String> Situmap =  new HashMap<>();
    boolean locationSituTAG = false;
    public static String locationSitu = "NA";

    public SituationManager() {
    }

    public Map<String, String> getMap() {
        return Situmap;
    }

    public void setMap() {

        if (locationSituTAG) {
            Situmap.put("Location", "0");
        }
    }
}
