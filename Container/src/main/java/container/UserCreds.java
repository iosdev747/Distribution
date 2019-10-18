package container;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class UserCreds  implements Serializable {
    //    String userName; can also be used to auth but then this must be distinct
    private String emailId;
    private String passwdHash;

    public UserCreds(String emailId, String passwdHash) {
        this.emailId = emailId;
        this.passwdHash = passwdHash;
    }
}
