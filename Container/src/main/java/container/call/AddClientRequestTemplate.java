package container.call;

import container.Seeder;

import java.io.Serializable;

public class AddClientRequestTemplate  implements Serializable {
    public Seeder seeder;
    public String chunkHash;
}
