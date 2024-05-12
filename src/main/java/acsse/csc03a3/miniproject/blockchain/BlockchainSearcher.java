package acsse.csc03a3.miniproject.blockchain;

import acsse.csc03a3.Block;
import acsse.csc03a3.Blockchain;
import acsse.csc03a3.Transaction;
import acsse.csc03a3.miniproject.payloads.AdminAssociationPayload;
import acsse.csc03a3.miniproject.payloads.ClientAssociationPayload;
import acsse.csc03a3.miniproject.payloads.ClientRegistrationPayload;
import acsse.csc03a3.miniproject.payloads.Payload;

import java.util.ArrayList;
import java.util.List;

public class BlockchainSearcher {
    private BlockchainParser parser;

    public BlockchainSearcher(Blockchain<Payload> blockchain) {
        this.parser = new BlockchainParser(blockchain);
    }

    /**
     * Function that checks if an admin is already associated with the payload
     * @return true of the admin is associated, false otherwise
     */
    public boolean checkAdminExistance() {
        ArrayList<Block<Payload>> blocks = parser.getBlocks();
        for (Block<Payload> block : blocks) {
            List<Transaction<Payload>> transactions = block.getTransactions();
            for (Transaction<Payload> transaction : transactions) {
                if(transaction.getData() instanceof AdminAssociationPayload) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean checkUserAssociation(String publicKey) {
        ArrayList<Block<Payload>> blocks = parser.getBlocks();
        for (Block<Payload> block : blocks) {
            List<Transaction<Payload>> transactions = block.getTransactions();
            for (Transaction<Payload> transaction : transactions) {
                if(transaction.getData() instanceof ClientAssociationPayload client) {
                    if(client.getPublicKey().equals(publicKey)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean checkUserRegistered(String id) {
        ArrayList<Block<Payload>> blocks = parser.getBlocks();
        for (Block<Payload> block : blocks) {
            List<Transaction<Payload>> transactions = block.getTransactions();
            for (Transaction<Payload> transaction : transactions) {
                if(transaction.getData() instanceof ClientRegistrationPayload client) {
                    if(client.getId().equals(id)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public String getAdminPK() {
        ArrayList<Block<Payload>> blocks = parser.getBlocks();
        for (Block<Payload> block : blocks) {
            List<Transaction<Payload>> transactions = block.getTransactions();
            for (Transaction<Payload> transaction : transactions) {
                if(transaction.getData() instanceof AdminAssociationPayload) {
                    return transaction.getData().getPublicKey();
                }
            }
        }
        return null;
    }
}
