package com.fyp.autologistics.blockchain;

import com.fyp.autologistics.config.BlockchainProperties;
import com.fyp.autologistics.enums.BlockchainHealthStatus;
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
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthChainId;
import org.web3j.protocol.core.methods.response.EthGetCode;
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

    @Override
    public BlockchainHealthSnapshot getHealthSnapshot() {
        boolean enabled = properties.isEnabled();
        boolean contractConfigured = StringUtils.hasText(properties.getContractAddress());
        boolean signerConfigured = hasValidSignerConfiguration();
        String contractAddress = contractConfigured ? properties.getContractAddress() : null;

        if (!enabled) {
            return new BlockchainHealthSnapshot(
                    BlockchainHealthStatus.DISABLED,
                    false,
                    false,
                    contractConfigured,
                    false,
                    signerConfigured,
                    contractAddress,
                    null,
                    null,
                    "Blockchain integration is disabled."
            );
        }

        if (!StringUtils.hasText(properties.getRpcUrl())) {
            return new BlockchainHealthSnapshot(
                    BlockchainHealthStatus.DEGRADED,
                    true,
                    false,
                    contractConfigured,
                    false,
                    signerConfigured,
                    contractAddress,
                    null,
                    null,
                    "Blockchain RPC URL is not configured."
            );
        }

        try (Web3j web3j = buildWeb3j()) {
            EthChainId chainIdResponse = web3j.ethChainId().send();
            EthBlockNumber blockNumberResponse = web3j.ethBlockNumber().send();

            if (chainIdResponse.hasError()) {
                throw new BlockchainLedgerException(
                        "Chain ID request failed: " + chainIdResponse.getError().getMessage()
                );
            }

            if (blockNumberResponse.hasError()) {
                throw new BlockchainLedgerException(
                        "Latest block request failed: "
                                + blockNumberResponse.getError().getMessage()
                );
            }

            Long chainId = chainIdResponse.getChainId().longValue();
            Long latestBlockNumber = blockNumberResponse.getBlockNumber().longValue();
            boolean contractDeployed = contractConfigured
                    && isContractDeployed(web3j, properties.getContractAddress());

            if (!contractConfigured) {
                return connectedButDegraded(
                        signerConfigured,
                        contractAddress,
                        chainId,
                        latestBlockNumber,
                        false,
                        "Blockchain node is reachable, but the contract address is not configured."
                );
            }

            if (!contractDeployed) {
                return connectedButDegraded(
                        signerConfigured,
                        contractAddress,
                        chainId,
                        latestBlockNumber,
                        false,
                        "Blockchain node is reachable, but no contract code exists at the configured address."
                );
            }

            if (!signerConfigured) {
                return connectedButDegraded(
                        false,
                        contractAddress,
                        chainId,
                        latestBlockNumber,
                        true,
                        "Blockchain node and contract are available, but the signer key is missing or invalid."
                );
            }

            return new BlockchainHealthSnapshot(
                    BlockchainHealthStatus.HEALTHY,
                    true,
                    true,
                    true,
                    true,
                    true,
                    contractAddress,
                    chainId,
                    latestBlockNumber,
                    "Blockchain node, deployed contract, and signer configuration are ready."
            );
        } catch (Exception exception) {
            return new BlockchainHealthSnapshot(
                    BlockchainHealthStatus.UNAVAILABLE,
                    true,
                    false,
                    contractConfigured,
                    false,
                    signerConfigured,
                    contractAddress,
                    null,
                    null,
                    "Blockchain RPC health check failed: " + getFailureMessage(exception)
            );
        }
    }

    private BlockchainHealthSnapshot connectedButDegraded(
            boolean signerConfigured,
            String contractAddress,
            Long chainId,
            Long latestBlockNumber,
            boolean contractDeployed,
            String message
    ) {
        return new BlockchainHealthSnapshot(
                BlockchainHealthStatus.DEGRADED,
                true,
                true,
                StringUtils.hasText(contractAddress),
                contractDeployed,
                signerConfigured,
                contractAddress,
                chainId,
                latestBlockNumber,
                message
        );
    }

    private boolean isContractDeployed(Web3j web3j, String contractAddress) throws Exception {
        EthGetCode codeResponse = web3j.ethGetCode(
                contractAddress,
                DefaultBlockParameterName.LATEST
        ).send();

        if (codeResponse.hasError()) {
            throw new BlockchainLedgerException(
                    "Contract code request failed: " + codeResponse.getError().getMessage()
            );
        }

        String contractCode = codeResponse.getCode();
        return StringUtils.hasText(contractCode)
                && !"0x".equalsIgnoreCase(contractCode)
                && !"0x0".equalsIgnoreCase(contractCode)
                && !"0x00".equalsIgnoreCase(contractCode);
    }

    private boolean hasValidSignerConfiguration() {
        if (!StringUtils.hasText(properties.getPrivateKey())) {
            return false;
        }

        try {
            String normalizedPrivateKey = normalizePrivateKey(properties.getPrivateKey());
            if (!normalizedPrivateKey.matches("[0-9a-fA-F]{64}")) {
                return false;
            }

            Credentials.create(normalizedPrivateKey);
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private String getFailureMessage(Exception exception) {
        return StringUtils.hasText(exception.getMessage())
                ? exception.getMessage()
                : exception.getClass().getSimpleName();
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
