package acsse.csc03a3.miniproject.blockchain;

import acsse.csc03a3.Block;

import java.util.ArrayList;
import java.util.List;

public class EBlock<T> extends Block<T> {
    public EBlock(String previousHash, List<ETransaction<T>> list) {
        super(previousHash, new ArrayList<>(list));
    }

    @Override
    public String toString() {
        return this.getHash() + "^" + this.getTransactions() + "^" + this.getHash() + "^" + this.getHash() + "^" + this.getHash() + "^" + this.getNonce();
    }

}
