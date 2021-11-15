package si.genlan.nam.utils;

public class ArrayUtils {

    public static String[] addToStringArray(String[] oldArray, String value) {
        String[] newArray = new String[oldArray.length + 1];
        for(int i=0; i< oldArray.length; i++)
            newArray[i] = oldArray[i];
        newArray[oldArray.length] = value;
        return newArray;
    }
}
