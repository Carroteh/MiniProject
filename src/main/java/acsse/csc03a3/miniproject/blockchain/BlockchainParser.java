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
import java.util.StringTokenizer;


public class BlockchainParser {
    private final Blockchain<Payload> blockchain;

    public BlockchainParser(Blockchain<Payload> blockchain) {
        this.blockchain = blockchain;
    }

    /**
     * Function that parses a blockchain string into an arraylist of blocks
     * @return ArrayList of blocks
     */
    public ArrayList<Block<Payload>> getBlocks() {
        ArrayList<String> strBlocks = getStrBlocks();
        ArrayList<Block<Payload>> blocks =  new ArrayList<>();
        for (String strBlock : strBlocks) {
            Block<Payload> block = getBlock(strBlock);
            if(block != null) {
                blocks.add(block);
            }
        }
        return blocks;
    }

    private ArrayList<String> getStrBlocks() {
        String strBlockchain = blockchain.toString();
        StringTokenizer st = new StringTokenizer(strBlockchain, "\n");
        ArrayList<String> blocks = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            blocks.add(st.nextToken());
        }
        return blocks;
    }

    private Transaction<Payload> getTransaction(String badTransaction) {
        String[] payloadStr = badTransaction.split("`");
        String sender = payloadStr[0].trim();
        String receiver = payloadStr[1].trim();
        String type = payloadStr[2].trim();

        Payload payload = null;
        if(type.equals("1")) {
            String id = payloadStr[3].trim();
            String PK = payloadStr[4].trim();
            payload = new AdminAssociationPayload(id, PK);
        }
        else if(type.equals("3")) {
            String PK = payloadStr[3].trim();
            payload = new ClientAssociationPayload(PK);
        }
        else if (type.equals("4")) {
            String id = payloadStr[3].trim();
            String username = payloadStr[4].trim();
            String hash = payloadStr[5].trim();
            String PK = payloadStr[7].trim();
            payload = new ClientRegistrationPayload(id,username, hash, new byte[0], PK);
        }
        return new Transaction(sender, receiver, payload);
    }

    private Block<Payload> getBlock(String badBlock) {
        StringTokenizer st = new StringTokenizer(badBlock, "=");
        st.nextToken();
        String prevHashBad = st.nextToken();
        String prevHashGood = prevHashBad.substring(1, prevHashBad.length() - 15);
        if(prevHashGood.equals("0")) { return null; }
        String transactionsBad = st.nextToken();
        String transactionsGood = transactionsBad.substring(1, transactionsBad.length() - 7);
        String hashBad = st.nextToken();
        String hashGood = hashBad.substring(1, hashBad.length() - 8);
        String nonceBad = st.nextToken();
        String nonceGood = nonceBad.substring(0, nonceBad.length() - 1);

        String[] transactionsStr = transactionsGood.split(",");
        List<Transaction<Payload>> transactions = new ArrayList<>();
        for (String s : transactionsStr) {
            transactions.add(getTransaction(s));
        }
        return new Block<>(prevHashGood, transactions);
    }



    public Blockchain<Payload> getBlockchain() {return blockchain;}
    public void setBlockchain(Blockchain<Payload> blockchain) {}
}
