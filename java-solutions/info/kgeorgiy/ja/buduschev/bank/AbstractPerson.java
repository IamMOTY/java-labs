package info.kgeorgiy.ja.buduschev.bank;

import java.io.Serializable;

public abstract class AbstractPerson implements Serializable {
    private final String firstName;
    private final String lastName;
    private final String passportId;

    public AbstractPerson(String firstName, String lastName, String passportId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.passportId = passportId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPassportId() {
        return passportId;
    }


    public String getAccountId(final String subId) {
        return getPassportId() + ":" + subId;
    }
}
