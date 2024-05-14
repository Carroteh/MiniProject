package acsse.csc03a3.miniproject.blockchain;

import acsse.csc03a3.Blockchain;
import acsse.csc03a3.Transaction;
import acsse.csc03a3.miniproject.payloads.AdminAssociationPayload;
import acsse.csc03a3.miniproject.payloads.ClientAssociationPayload;
import acsse.csc03a3.miniproject.payloads.ClientRegistrationPayload;
import acsse.csc03a3.miniproject.payloads.Payload;

import java.util.ArrayList;
import java.util.List;

public class BlockchainManager {

    private Blockchain<Payload> blockchain;
    private List<Transaction<Payload>> currentTSet;
    private BlockchainSearcher bcSearcher;

    public BlockchainManager(Blockchain<Payload> blockchain) {
        this.bcSearcher = new BlockchainSearcher(blockchain);
        this.blockchain = blockchain;
        currentTSet = new ArrayList<>();
    }

    public synchronized void addTransaction(Transaction<Payload> transaction) {
        currentTSet.add(transaction);
        if (currentTSet.size() >= 2) {
            blockchain.addBlock(currentTSet);
            currentTSet = new ArrayList<>();
        }
    }

    public synchronized void registerStake(String nodeAddress, int stake) {
        blockchain.registerStake(nodeAddress, stake);
    }

    public synchronized boolean checkAdminExistance() {
        for (Transaction<Payload> transaction : currentTSet) {
            if(transaction.getData() instanceof AdminAssociationPayload) {
                return true;
            }
        }
        return bcSearcher.checkAdminExistance();
    }

    public synchronized boolean checkUserAssociation(String publicKey) {
        for (Transaction<Payload> transaction : currentTSet) {
            if(transaction.getData() instanceof ClientAssociationPayload client) {
                if(client.getPublicKey().equals(publicKey)) {
                    return true;
                }
            }
        }
        return bcSearcher.checkUserAssociation(publicKey);
    }

    public synchronized boolean checkUserRegistered(String id) {
        for (Transaction<Payload> transaction : currentTSet) {
            if(transaction.getData() instanceof ClientRegistrationPayload client) {
                if(client.getId().equals(id)) {
                    return true;
                }
            }
        }
        return bcSearcher.checkUserRegistered(id);
    }

    public synchronized String getAdminPK() {
        for (Transaction<Payload> transaction : currentTSet) {
            if(transaction.getData() instanceof AdminAssociationPayload) {
                return (transaction.getData()).getPublicKey();
            }
        }
        return bcSearcher.getAdminPK();
    }

    public synchronized boolean checkUsernameRegistered(String username) {
        for (Transaction<Payload> transaction : currentTSet) {
            if(transaction.getData() instanceof ClientRegistrationPayload client) {
                if(client.getUsername().equals(username)) {
                    return true;
                }
            }
        }
        return bcSearcher.checkUsernameRegistered(username);
    }

    public Blockchain<Payload> getBlockchain() {
        return blockchain;
    }
}