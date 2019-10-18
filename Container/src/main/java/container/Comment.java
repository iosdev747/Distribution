package container;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Comment {
    boolean isAnonymous;
    String userName;
    String description;
}