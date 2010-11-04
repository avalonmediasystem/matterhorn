package ch.ethz.replay.ui.scheduler.external.common;

import ch.ethz.replay.core.api.common.vcard.EmailAddress;
import ch.ethz.replay.ui.common.util.dao.NotFoundException;
import ch.ethz.replay.ui.common.util.hibernate.PostLoadAware;
import ch.ethz.replay.ui.scheduler.Person;
import ch.ethz.replay.ui.scheduler.impl.AbstractPerson;
import org.hibernate.annotations.NaturalId;
import org.springframework.util.StringUtils;

import javax.persistence.Entity;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Christoph E. Driessen <ced@neopoly.de>
 */
@Entity(name = "LdapPerson")
public class LdapPerson extends AbstractPerson
        implements PostLoadAware {

    @NaturalId
    private String ldapId;

    @Transient
    private boolean modifiable = false;

    @Transient
    private String familyName;

    @Transient
    private String givenName;

    @Transient
    private String honorificPrefixes;

    @Transient
    private List emailAddresses = new ArrayList();

    //

    public LdapPerson() {
    }

    public LdapPerson(String ldapId) {
        this.ldapId = ldapId;
    }

    public LdapPerson(String ldapId, boolean modifiable) {
        this.ldapId = ldapId;
        this.modifiable = modifiable;
    }

    /**
     * Returns the LDAP ID.
     */
    public String getLdapId() {
        return ldapId;
    }

    public String getFormattedName() {
        StringBuilder b = new StringBuilder();
        if (StringUtils.hasText(honorificPrefixes)) b.append(honorificPrefixes).append(" ");
        if (StringUtils.hasText(givenName)) b.append(givenName).append(" ");
        if (StringUtils.hasText(familyName)) b.append(familyName);
        return b.toString();
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getHonorificPrefixes() {
        return honorificPrefixes;
    }

    public void setHonorificPrefixes(String honorificPrefixes) {
        this.honorificPrefixes = honorificPrefixes;
    }

    public List<EmailAddress> getEmailAddresses() {
        return emailAddresses;
    }

    public EmailAddress getPreferredEmailAddress() {
        EmailAddress e = null;
        for (Object o : emailAddresses) {
            e = (EmailAddress) o;
            if (e.isPreferred()) return e;
        }
        return e;
    }

    public void setEmailAddresses(List emailAddresses) {
        this.emailAddresses = emailAddresses;
    }

    public void addEmailAddress(EmailAddress address) {
        emailAddresses.add(address);
    }

    public void onPostLoad() {
        Person dto = LdapPersonDaoProvider.get().get(ldapId);
        if (dto == null) {
            throw new NotFoundException("Cannot find entity " + ldapId + " in LDAP directory");
        }
        copyFrom(dto);
    }

    public boolean isModifiable() {
        return modifiable;
    }

    protected void copyFrom(Person template) {
        familyName = template.getFamilyName();
        givenName = template.getGivenName();
        honorificPrefixes = template.getHonorificPrefixes();
        emailAddresses = template.getEmailAddresses();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LdapPerson)) return false;

        LdapPerson that = (LdapPerson) o;
        if (ldapId == null)
            return super.equals(o);
        return ldapId.equals(that.getLdapId());
    }

    public int hashCode() {
        if (ldapId == null)
            return super.hashCode();
        return ldapId.hashCode();
    }
}