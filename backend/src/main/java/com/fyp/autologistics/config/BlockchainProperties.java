package com.fyp.autologistics.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

@Component
@ConfigurationProperties(prefix = "app.blockchain")
public class BlockchainProperties {

    private boolean enabled;

    private String rpcUrl;

    private String contractAddress;

    private String privateKey;

    private BigInteger gasPriceWei = BigInteger.valueOf(20_000_000_000L);

    private BigInteger gasLimit = BigInteger.valueOf(250_000L);

    private long receiptPollingIntervalMs = 1_000L;

    private int receiptPollingAttempts = 40;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getRpcUrl() {
        return rpcUrl;
    }

    public void setRpcUrl(String rpcUrl) {
        this.rpcUrl = rpcUrl;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public BigInteger getGasPriceWei() {
        return gasPriceWei;
    }

    public void setGasPriceWei(BigInteger gasPriceWei) {
        this.gasPriceWei = gasPriceWei;
    }

    public BigInteger getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(BigInteger gasLimit) {
        this.gasLimit = gasLimit;
    }

    public long getReceiptPollingIntervalMs() {
        return receiptPollingIntervalMs;
    }

    public void setReceiptPollingIntervalMs(long receiptPollingIntervalMs) {
        this.receiptPollingIntervalMs = receiptPollingIntervalMs;
    }

    public int getReceiptPollingAttempts() {
        return receiptPollingAttempts;
    }

    public void setReceiptPollingAttempts(int receiptPollingAttempts) {
        this.receiptPollingAttempts = receiptPollingAttempts;
    }
}
