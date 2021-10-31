package si.genlan.nam.idp;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Getter
public class SamlResponseAttributeRepository {

    private final List<SamlResponseAttribute> attributes;

    public SamlResponseAttributeRepository(List<SamlResponseAttribute> attributes) {
        this.attributes = attributes;
    }

    public SamlResponseAttributeRepository() {
        attributes = new ArrayList<>();
    }

    public void add(String name, String value) {

        SamlResponseAttribute attribute = attributes.stream()
                .filter(attributeExists(name))
                .peek(addValueToExistingAttribute(value))
                .findAny()
                .orElse(new SamlResponseAttribute(name, value));

        attributes.removeIf(attributeExists(name));
        attributes.add(attribute);
    }

    private Consumer<SamlResponseAttribute> addValueToExistingAttribute(String value) {
        return a -> a.addValue(value);
    }

    private Predicate<SamlResponseAttribute> attributeExists(String name) {
        return samlResponseAttribute -> samlResponseAttribute.getName().equals(name);
    }

    public void clear() {
        attributes.clear();
    }

    public String getJoinedValueListByName(String name) {
        return attributes.stream()
                .filter(attributeExists(name))
                .findAny()
                .map(SamlResponseAttribute::getAttributeValues)
                .orElse("");
    }

    public String[] getArrayOfAttributeNames() {

        return attributes
                .stream()
                .map(SamlResponseAttribute::getName)
                .distinct()
                .toArray(String[]::new);
    }

    public String[] getValuesAsArrayByAttributeName(String name) {
        return attributes
                .stream()
                .filter(attributeExists(name))
                .findAny()
                .map(SamlResponseAttribute::getValuesAsArray)
                .orElse(null);
    }
}
