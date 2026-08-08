package com.fyp.autologistics.blockchain;

import com.fyp.autologistics.config.BlockchainProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;
import org.web3j.tx.response.TransactionReceiptProcessor;

import java.math.BigInteger;
import java.util.HexFormat;
import java.util.List;

@Component
public class Web3jBlockchainLedgerClient implements BlockchainLedgerClient {

    private final BlockchainProperties properties;

    public Web3jBlockchainLedgerClient(BlockchainProperties properties) {
        this.properties = properties;
    }

    @Override
    public BlockchainTransactionReceipt recordShipmentEvent(
            Long shipmentId,
            Long shipmentEventId,
            String eventHash
    ) {
        ensureBlockchainIsConfigured();

        try (Web3j web3j = buildWeb3j()) {
            Credentials credentials = Credentials.create(normalizePrivateKey(properties.getPrivateKey()));
            TransactionManager transactionManager = new RawTransactionManager(web3j, credentials);

            Function function = new Function(
                    "recordShipmentEvent",
                    List.of(
                            new Uint256(BigInteger.valueOf(shipmentId)),
                            new Uint256(BigInteger.valueOf(shipmentEventId)),
                            new Bytes32(toBytes32(eventHash))
                    ),
                    List.of()
            );

            String encodedFunction = FunctionEncoder.encode(function);
            EthSendTransaction sentTransaction = transactionManager.sendTransaction(
                    properties.getGasPriceWei(),
                    properties.getGasLimit(),
                    properties.getContractAddress(),
                    encodedFunction,
                    BigInteger.ZERO
            );

            if (sentTransaction.hasError()) {
                throw new BlockchainLedgerException(
                        "Blockchain transaction rejected: "
                                + sentTransaction.getError().getMessage()
                );
            }

            TransactionReceiptProcessor receiptProcessor = new PollingTransactionReceiptProcessor(
                    web3j,
                    properties.getReceiptPollingIntervalMs(),
                    properties.getReceiptPollingAttempts()
            );

            TransactionReceipt receipt = receiptProcessor.waitForTransactionReceipt(
                    sentTransaction.getTransactionHash()
            );

            if (!receipt.isStatusOK()) {
                throw new BlockchainLedgerException(
                        "Blockchain transaction failed with status: " + receipt.getStatus()
                );
            }

            return new BlockchainTransactionReceipt(
                    receipt.getTransactionHash(),
                    receipt.getBlockNumber().longValue()
            );
        } catch (BlockchainLedgerException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BlockchainLedgerException(
                    "Failed to record shipment event on blockchain.",
                    exception
            );
        }
    }

    @Override
    public String getRecordedEventHash(Long shipmentEventId) {
        ensureBlockchainIsConfigured();

        try (Web3j web3j = buildWeb3j()) {
            Function function = new Function(
                    "getShipmentEventHash",
                    List.of(new Uint256(BigInteger.valueOf(shipmentEventId))),
                    List.of(new TypeReference<Bytes32>() {
                    })
            );

            String encodedFunction = FunctionEncoder.encode(function);
            EthCall response = web3j.ethCall(
                    Transaction.createEthCallTransaction(
                            null,
                            properties.getContractAddress(),
                            encodedFunction
                    ),
                    DefaultBlockParameterName.LATEST
            ).send();

            if (response.hasError()) {
                throw new BlockchainLedgerException(
                        "Blockchain read rejected: " + response.getError().getMessage()
                );
            }

            List<Type> decodedValues = FunctionReturnDecoder.decode(
                    response.getValue(),
                    function.getOutputParameters()
            );

            if (decodedValues.isEmpty()) {
                throw new BlockchainLedgerException(
                        "Blockchain record was not found for shipment event #"
                                + shipmentEventId
                );
            }

            Bytes32 hash = (Bytes32) decodedValues.get(0);
            return HexFormat.of().formatHex(hash.getValue());
        } catch (BlockchainLedgerException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BlockchainLedgerException(
                    "Failed to read shipment event hash from blockchain.",
                    exception
            );
        }
    }

    private Web3j buildWeb3j() {
        return Web3j.build(new HttpService(properties.getRpcUrl()));
    }

    private void ensureBlockchainIsConfigured() {
        if (!properties.isEnabled()) {
            throw new BlockchainLedgerException("Blockchain integration is disabled.");
        }

        if (!StringUtils.hasText(properties.getRpcUrl())) {
            throw new BlockchainLedgerException("Blockchain RPC URL is not configured.");
        }

        if (!StringUtils.hasText(properties.getContractAddress())) {
            throw new BlockchainLedgerException("Blockchain contract address is not configured.");
        }

        if (!StringUtils.hasText(properties.getPrivateKey())) {
            throw new BlockchainLedgerException("Blockchain signer private key is not configured.");
        }
    }

    private String normalizePrivateKey(String privateKey) {
        return privateKey.startsWith("0x") ? privateKey.substring(2) : privateKey;
    }

    private byte[] toBytes32(String eventHash) {
        String normalizedHash = eventHash.startsWith("0x")
                ? eventHash.substring(2)
                : eventHash;

        byte[] hashBytes = HexFormat.of().parseHex(normalizedHash);

        if (hashBytes.length != 32) {
            throw new BlockchainLedgerException("Event hash must be exactly 32 bytes.");
        }

        return hashBytes;
    }
}
