package si.genlan.nam.utils;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
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

        if (one.size() != two.size()) {
            return false;
        }

        return one.equals(two);
    }
    public static List<String> EnumToStringList(NamingEnumeration<?> enumeration) throws NamingException {
        List<String> list = new ArrayList<>();
        while(enumeration.hasMore())
            list.add(enumeration.next().toString());

        return list;
    }
    public static String[] StringToArray(String input, String splitter)
    {
        String[] returnString = input.split(splitter);
        return returnString;
    }
}
