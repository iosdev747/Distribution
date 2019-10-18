package container.call;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class GetStrings implements Serializable {
    public String[] strings;
}
