package acsse.csc03a3.miniproject.blockchain;

import acsse.csc03a3.Transaction;

public class ETransaction<T> extends Transaction<T> {

    public ETransaction(String sender, String receiver, T data) {
        super(sender, receiver, data);
    }

    @Override
    public String toString() {
        return this.getSender() + "`" + this.getReceiver() + "`" + this.getData() + "`" + this.getTimestamp();
    }
}
