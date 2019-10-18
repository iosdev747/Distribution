package container.call;

import container.Torrent;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CallAddComment {
    String comment;
    Torrent torrent;
    String userName;
}
