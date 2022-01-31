package si.genlan.nam.services;

import si.genlan.nam.idp.Tracer;
import si.genlan.nam.repositories.LdapUserStoreRepository;
import si.genlan.nam.utils.ListUtils;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.ModificationItem;
import java.util.List;

public class UserService {
    Tracer tracer;
    public UserService(boolean trace)
    {
        tracer = Tracer.getInstance(trace);
    }
    public void updateUser(String[] samlValues, String[] multivaluedStoreArray, String gotAttribute, String userPath, Attribute attr, LdapUserStoreRepository ldapUserStoreRepository, String entryDn) throws NamingException {
        ModificationItem[] mods;
        mods = ldapUserStoreRepository.AttributeValuesToAddToUserStore(samlValues, multivaluedStoreArray, gotAttribute);
        ldapUserStoreRepository.updateUser(userPath, mods, entryDn);

        List<String> multivalued = ListUtils.EnumToStringList(attr.getAll());
        multivaluedStoreArray = ldapUserStoreRepository.getAttributeValues(multivalued);
        mods = ldapUserStoreRepository.AttributeValuesToDeleteFromUserStore(samlValues, multivaluedStoreArray, gotAttribute);
        if (mods.length >= 0)
            ldapUserStoreRepository.updateUser(userPath, mods, entryDn);
        else
            tracer.trace("Nothing to delete from User Store");
    }
}
