package container;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class Packet  implements Serializable {
    public String call;
    public String data;
}