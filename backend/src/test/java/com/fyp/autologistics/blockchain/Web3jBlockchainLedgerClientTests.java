package com.fyp.autologistics.blockchain;

import com.fyp.autologistics.config.BlockchainProperties;
import org.junit.jupiter.api.Test;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthChainId;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.io.IOException;
import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class Web3jBlockchainLedgerClientTests {

    @Test
    void resolvesChainIdFromConnectedNetwork() throws Exception {
        BlockchainProperties properties = new BlockchainProperties();
        Web3jBlockchainLedgerClient client = new Web3jBlockchainLedgerClient(properties);
        Web3j web3j = mock(Web3j.class);
        @SuppressWarnings("unchecked")
        Request<?, EthChainId> request = mock(Request.class);
        EthChainId response = new EthChainId();
        response.setResult("0xaa36a7");

        doReturn(request).when(web3j).ethChainId();
        when(request.send()).thenReturn(response);

        assertThat(client.resolveChainId(web3j)).isEqualTo(11_155_111L);
    }

    @Test
    void usesNetworkGasPriceWhenConfiguredValueIsZero() throws Exception {
        BlockchainProperties properties = new BlockchainProperties();
        properties.setGasPriceWei(BigInteger.ZERO);
        Web3jBlockchainLedgerClient client = new Web3jBlockchainLedgerClient(properties);
        Web3j web3j = mock(Web3j.class);
        @SuppressWarnings("unchecked")
        Request<?, EthGasPrice> request = mock(Request.class);
        EthGasPrice response = new EthGasPrice();
        response.setResult("0x3b9aca00");

        doReturn(request).when(web3j).ethGasPrice();
        when(request.send()).thenReturn(response);

        assertThat(client.resolveGasPriceWei(web3j))
                .isEqualTo(BigInteger.valueOf(1_000_000_000L));
    }

    @Test
    void positiveConfiguredGasPriceOverridesNetworkLookup() throws Exception {
        BlockchainProperties properties = new BlockchainProperties();
        properties.setGasPriceWei(BigInteger.valueOf(2_000_000_000L));
        Web3jBlockchainLedgerClient client = new Web3jBlockchainLedgerClient(properties);
        Web3j web3j = mock(Web3j.class);

        assertThat(client.resolveGasPriceWei(web3j))
                .isEqualTo(BigInteger.valueOf(2_000_000_000L));
        verify(web3j, never()).ethGasPrice();
    }

    @Test
    void receiptPollingContinuesAfterTemporaryRpcFailure() throws Exception {
        BlockchainProperties properties = new BlockchainProperties();
        properties.setReceiptPollingAttempts(2);
        properties.setReceiptPollingIntervalMs(1L);
        Web3jBlockchainLedgerClient client = new Web3jBlockchainLedgerClient(properties);
        Web3j web3j = mock(Web3j.class);
        @SuppressWarnings("unchecked")
        Request<?, EthGetTransactionReceipt> request = mock(Request.class);
        EthGetTransactionReceipt response = new EthGetTransactionReceipt();
        TransactionReceipt expectedReceipt = new TransactionReceipt();
        expectedReceipt.setTransactionHash("0xconfirmed");
        expectedReceipt.setBlockNumber("0x2a");
        expectedReceipt.setStatus("0x1");
        response.setResult(expectedReceipt);

        doReturn(request).when(web3j).ethGetTransactionReceipt("0xsubmitted");
        when(request.send())
                .thenThrow(new IOException("Temporary RPC failure."))
                .thenReturn(response);

        TransactionReceipt receipt = client.waitForTransactionReceipt(web3j, "0xsubmitted");

        assertThat(receipt.getTransactionHash()).isEqualTo("0xconfirmed");
        assertThat(receipt.getBlockNumber()).isEqualTo(42L);
    }

    @Test
    void receiptPollingPreservesSubmittedHashWhenConfirmationTimesOut() throws Exception {
        BlockchainProperties properties = new BlockchainProperties();
        properties.setReceiptPollingAttempts(1);
        Web3jBlockchainLedgerClient client = new Web3jBlockchainLedgerClient(properties);
        Web3j web3j = mock(Web3j.class);
        @SuppressWarnings("unchecked")
        Request<?, EthGetTransactionReceipt> request = mock(Request.class);

        doReturn(request).when(web3j).ethGetTransactionReceipt("0xsubmitted");
        when(request.send()).thenThrow(new IOException("Temporary RPC failure."));

        assertThatThrownBy(() -> client.waitForTransactionReceipt(web3j, "0xsubmitted"))
                .isInstanceOf(BlockchainSubmissionPendingException.class)
                .satisfies(exception -> assertThat(
                        ((BlockchainSubmissionPendingException) exception).getTransactionHash()
                ).isEqualTo("0xsubmitted"));
    }
}
