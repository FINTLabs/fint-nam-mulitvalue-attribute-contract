package si.genlan.nam.idp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ListUtils {

    public static boolean isListsEqual(String[] l1, String[] l2) {
        List<String> one = Arrays.asList(l1);
        List<String> two = Arrays.asList(l2);

        Collections.sort(one);
        Collections.sort(two);

        if(one.size() != two.size()){
            return false;
        }

        return one.equals(two);
    }
}
