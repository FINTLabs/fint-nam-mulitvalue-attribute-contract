package si.genlan.nam.idp

import si.genlan.nam.utils.ListUtils
import spock.lang.Specification

class ListUtilsSpec extends Specification {

    def "The to lists are equal"() {
        given:
        def list1 = ["Hello", "World"]
        def list2 = ["World", "Hello"]

        when:
        def listsAreEqual = ListUtils.isListsEqual(list1 as String[], list2 as String[])

        then:
        listsAreEqual
    }
}
