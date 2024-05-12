package acsse.csc03a3.miniproject.blockchain;

import acsse.csc03a3.Transaction;

import java.io.Serializable;
import java.util.*;

public class SerializableBlockchain<T>  implements Serializable {
    private List<SerializableBlock<T>> chain = new ArrayList();
    private Map<String, Integer> stakes = new HashMap();

    public SerializableBlockchain() {
        this.chain.add(this.createGenesisBlock());
    }

    private SerializableBlock<T> createGenesisBlock() {
        return new SerializableBlock("0", new ArrayList());
    }

    public void addBlock(List<Transaction<T>> transactions) {
        SerializableBlock<T> previousBlock = (SerializableBlock)this.chain.get(this.chain.size() - 1);
        SerializableBlock<T> newBlock = new SerializableBlock(previousBlock.getHash(), transactions);
        newBlock.setNonce(this.calculatePoSNonce(newBlock));
        this.chain.add(newBlock);
    }

    public void registerStake(String nodeAddress, int stake) {
        this.stakes.put(nodeAddress, stake);
    }

    private long calculatePoSNonce(SerializableBlock<T> block) {
        if (this.stakes.isEmpty()) {
            throw new RuntimeException("No stakeholders registered with stakes for Proof of Stake");
        } else {
            String selectedValidator = this.selectValidator();
            return (long)selectedValidator.hashCode();
        }
    }

    private String selectValidator() {
        int totalStake = this.stakes.values().stream().mapToInt(Integer::intValue).sum();
        int stakeThreshold = (new Random()).nextInt(totalStake);
        int currentSum = 0;
        Iterator var5 = this.stakes.entrySet().iterator();

        while(var5.hasNext()) {
            Map.Entry<String, Integer> entry = (Map.Entry)var5.next();
            currentSum += (Integer)entry.getValue();
            if (currentSum > stakeThreshold) {
                return (String)entry.getKey();
            }
        }

        throw new RuntimeException("Failed to select a validator");
    }

    public boolean isChainValid() {
        for(int i = 1; i < this.chain.size(); ++i) {
            SerializableBlock<T> currentBlock = this.chain.get(i); //REMOVED CASTING
            SerializableBlock<T> previousBlock = this.chain.get(i - 1);
            if (!currentBlock.getHash().equals(currentBlock.calculateHash()) || !currentBlock.getPreviousHash().equals(previousBlock.getHash())) {
                return false;
            }
        }

        return true;
    }

    public String toString() {
        StringBuilder blockchainString = new StringBuilder();
        Iterator var3 = this.chain.iterator();

        while(var3.hasNext()) {
            SerializableBlock<T> block = (SerializableBlock)var3.next();
            blockchainString.append(block.toString()).append("\n");
        }

        return blockchainString.toString();
    }
}