package si.genlan.nam.idp;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Data
public class SamlResponseAttribute {
    private String name;
    // TODO: 31/10/2021 Should this one always only contain one value? Maybe we should use a Set instead?
    // TODO: 02/11/2021 Name only contains one value, but values can contain many strings, because of multivalue attributes.
    private List<String> values = new ArrayList<>();

    public SamlResponseAttribute(String name, String val) {
        this.name = name;
        values.add(val);
    }

    public void addValue(@NotNull String value) {
        if (values.stream().noneMatch(value::equalsIgnoreCase)) {
            values.add(value);
        }
    }
    public void addValue(List<String> valuesGot)
    {
        valuesGot
                .stream()
                .forEach(value->
                    {
                        if(values.stream().noneMatch(value::equalsIgnoreCase))
                        {
                            values.add(value);
                            System.out.println(String.format("Adding value %s to %s",value, name));
                        }
                    });

    }

    public String getAttributeValues() {
        String str = String.join("; ", values);
        System.out.println("Joined String: " + str);
        return str;
    }

    public String[] getValuesAsArray() {
        return values.toArray(new String[0]);
    }
}
