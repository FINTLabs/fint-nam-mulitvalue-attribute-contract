package si.genlan.nam.idp

import spock.lang.Specification

class SamlResponseAttributeRepositorySpec extends Specification {

    private SamlResponseAttributeRepository samlResponseAttributeRepository

    void setup() {
        samlResponseAttributeRepository = new SamlResponseAttributeRepository(
                [new SamlResponseAttribute("name1", "val1")]
        )
    }

    void cleanup() {
        samlResponseAttributeRepository.clear()
    }

    def "Added a non-exiting attribute should result in a new attribute in the list"() {
        when:
        samlResponseAttributeRepository.add("name2", "val2")

        then:
        samlResponseAttributeRepository.getAttributes().size() == 2
    }

    def "Added a exiting attribute should result in a updated value on the attribute"() {
        when:
        samlResponseAttributeRepository.add("name1", "updated value")

        then:
        samlResponseAttributeRepository.getAttributes().size() == 1
        samlResponseAttributeRepository.getAttributes().get(0).getValues().contains("updated value")
    }

    def "Get concatenate value list"() {
        given:
        samlResponseAttributeRepository.add("name1", "val2")

        when:
        def value = samlResponseAttributeRepository.getJoinedValueListByName("name1")

        then:
        value == "val1; val2"
    }

    def "getArrayOfAttributeNames should return a list of attribute names"() {
        given:
        samlResponseAttributeRepository.add("name2", "val2")

        when:
        def attributes = samlResponseAttributeRepository.getArrayOfAttributeNames()

        then:
        attributes.size() == 2
        attributes[0] == "name1"
        attributes[1] == "name2"

    }

    def "getValuesAsArrayByAttributeName should return a list of values for the given attribute name"() {
        given:
        samlResponseAttributeRepository.add("name1", "val2")

        when:
        def values = samlResponseAttributeRepository.getValuesAsArrayByAttributeName("name1")

        then:
        values.size() == 2
        values[0] == "val1"
    }
}
