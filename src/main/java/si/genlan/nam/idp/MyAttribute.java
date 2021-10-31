package si.genlan.nam.idp;

import lombok.Data;

import java.util.ArrayList;

@Data
public class MyAttribute {
    private String name;
    // TODO: 31/10/2021 Should this one always only contain one value?
    private ArrayList<String> values = new ArrayList<>();

    public MyAttribute(String name, String val) {
        this.name = name;
        values.add(val);
    }

    public int length() {
        return values.size();
    }

    public void addValue(String value) {
        if (values.stream().noneMatch(value::equalsIgnoreCase)) {
            values.add(value);
        }
    }

    public String getAttributeValues() {
        String val = "";
        for (String str : values) {
            val += str;
            if (length() > 1)
                val += "; ";
        }

        return val;
    }

    public String[] getAttributeValuesArray() {
        String[] val = new String[length()];
        int i = 0;
        for (String str : values) {
            val[i] = str;
            i++;
        }
        return val;
    }

    public boolean isInside(String val) {
        for (String val1 : values) {
            if (val1.equals(val))
                return true;
        }
        return false;
    }

}
