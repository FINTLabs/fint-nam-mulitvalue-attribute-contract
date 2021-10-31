package si.genlan.nam.idp

import spock.lang.Specification

class AttributeRepositorySpec extends Specification {

    private AttributeRepository attributeRepository

    void setup() {
        attributeRepository = new AttributeRepository([new MyAttribute("name1", "val1")])
    }

    void cleanup() {
        attributeRepository.clear()
    }

    def "Added a non-exiting attribute should result in a new attribute in the list"() {
        when:
        attributeRepository.add("name2", "val2")

        then:
        attributeRepository.getAttributes().size() == 2
    }

    def "Added a exiting attribute should result in a updated value on the attribute"() {
        when:
        attributeRepository.add("name1", "updated value")

        then:
        attributeRepository.getAttributes().size() == 1
        attributeRepository.getAttributes().get(0).getValues().contains("updated value")
    }

    def "Get concatenate value list"() {
        given:
        attributeRepository.add("name1", "val2")

        when:
        def value = attributeRepository.getListValue("name1")

        then:
        value == "val1; val2; "
    }

    def "getSentAttributes should return a list of attribute names"() {

        when:
        def attributes = attributeRepository.getSentAttributes()

        then:
        attributes.size() == 1
        attributes[0] == "name1"
    }

    def "getSentAttributesArray should return a list of values for the given attribute name"() {
        given:
        attributeRepository.add("name1", "val2")

        when:
        def values = attributeRepository.getSentAttributesArray("name1")

        then:
        values.size() == 2
        values[0] == "val1"
    }
}
