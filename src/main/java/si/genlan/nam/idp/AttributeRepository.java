package si.genlan.nam.idp;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Getter
public class AttributeRepository {

    private final List<MyAttribute> attributes;

    public AttributeRepository(List<MyAttribute> attributes) {
        this.attributes = attributes;
    }

    public AttributeRepository() {
        attributes = new ArrayList<>();
    }

    public void add(String name, String value) {

        MyAttribute attribute = attributes.stream()
                .filter(attributeExists(name))
                .peek(addValueToExistingAttribute(value))
                .findAny()
                .orElse(new MyAttribute(name, value));

        attributes.removeIf(attributeExists(name));
        attributes.add(attribute);
    }

    private Consumer<MyAttribute> addValueToExistingAttribute(String value) {
        return a -> a.addValue(value);
    }

    private Predicate<MyAttribute> attributeExists(String name) {
        return myAttribute -> myAttribute.getName().equals(name);
    }

    public void clear() {
        attributes.clear();
    }

    public String getListValue(String name) {
        return attributes.stream()
                .filter(attributeExists(name))
                .map(MyAttribute::getAttributeValues)
                .collect(Collectors.joining(";"));
    }

    public String[] getSentAttributes() {
        String[] sentAttributes = new String[]{};
        for (MyAttribute attr : attributes) {
            if (Arrays.stream(sentAttributes).noneMatch(attr.getName()::equals)) {
                sentAttributes = Arrays.copyOf(sentAttributes, sentAttributes.length + 1);
                sentAttributes[sentAttributes.length - 1] = attr.getName();
            }
        }
        return sentAttributes;
    }

    public String[] getSentAttributesArray(String name) {
        String[] sentAttributes;
        for (MyAttribute attr : attributes) {
            if (attr.getName().equals(name)) {
                sentAttributes = attr.getAttributeValuesArray();
                return sentAttributes;
            }
        }
        return null;
    }
}
