package container.call;

import java.io.Serializable;

public class InsertMyFileRequestTemplate implements Serializable {
    public String emailId;
    public String fileHash;
    public String fileName;
}
