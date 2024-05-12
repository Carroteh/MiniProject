package acsse.csc03a3.miniproject.blockchain;

import acsse.csc03a3.Blockchain;
import acsse.csc03a3.Transaction;
import acsse.csc03a3.miniproject.payloads.AdminAssociationPayload;
import acsse.csc03a3.miniproject.payloads.ClientAssociationPayload;
import acsse.csc03a3.miniproject.payloads.ClientRegistrationPayload;
import acsse.csc03a3.miniproject.payloads.Payload;

import java.util.ArrayList;

public class BlockchainManager {

    private final Blockchain<Payload> blockchain;
    private final ArrayList<Transaction<Payload>> currentTSet;
    private BlockchainSearcher bcSearcher;

    public BlockchainManager(Blockchain<Payload> blockchain) {
        this.bcSearcher = new BlockchainSearcher(blockchain);
        this.blockchain = blockchain;
        currentTSet = new ArrayList<>();
    }

    public void addTransaction(Transaction<Payload> transaction) {
        if (currentTSet.size() < 6) {
            currentTSet.add(transaction);
        } else {
            blockchain.addBlock(currentTSet);
            currentTSet.clear();
        }
    }

    public void registerStake(String nodeAddress, int stake) {
        blockchain.registerStake(nodeAddress, stake);
    }

    public boolean checkAdminExistance() {
        for (Transaction<Payload> transaction : currentTSet) {
            if(transaction.getData() instanceof AdminAssociationPayload) {
                return true;
            }
        }
        return bcSearcher.checkAdminExistance();
    }

    public boolean checkUserAssociation(String publicKey) {
        for (Transaction<Payload> transaction : currentTSet) {
            if(transaction.getData() instanceof ClientAssociationPayload client) {
                if(client.getPublicKey().equals(publicKey)) {
                    return true;
                }
            }
        }
        return bcSearcher.checkUserAssociation(publicKey);
    }

    public boolean checkUserRegistered(String id) {
        for (Transaction<Payload> transaction : currentTSet) {
            if(transaction.getData() instanceof ClientRegistrationPayload client) {
                if(client.getId().equals(id)) {
                    return true;
                }
            }
        }
        return bcSearcher.checkUserAssociation(id);
    }

    public String getAdminPK() {
        for (Transaction<Payload> transaction : currentTSet) {
            if(transaction.getData() instanceof AdminAssociationPayload) {
                return (transaction.getData()).getPublicKey();
            }
        }
        return bcSearcher.getAdminPK();
    }

}