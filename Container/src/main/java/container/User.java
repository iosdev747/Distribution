package container;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


@Getter
@Setter
public class User  implements Serializable {

    private String name;
    private String passHash;
    private String email;
    private String usrname;
    private String  DOB;
    private String[] fileHash;

    public String toString(){
        return name;
    }
}