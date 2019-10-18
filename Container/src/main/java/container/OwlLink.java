package container;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class OwlLink implements Serializable {
    private String ip;
    private int port;
}
