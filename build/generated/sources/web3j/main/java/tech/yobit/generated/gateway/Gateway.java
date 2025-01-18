package tech.yobit.generated.gateway;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/hyperledger-web3j/web3j/tree/main/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 4.12.3.
 */
@SuppressWarnings("rawtypes")
public class Gateway extends Contract {
    public static final String BINARY = "6080604052348015600e575f5ffd5b503380603357604051631e4fbdf760e01b81525f600482015260240160405180910390fd5b603a81603f565b50608e565b5f80546001600160a01b038381166001600160a01b0319831681178455604051919092169283917f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e09190a35050565b6107618061009b5f395ff3fe608060405234801561000f575f5ffd5b5060043610610055575f3560e01c80631d64760514610059578063715018a61461006e57806389b08f11146100765780638da5cb5b146100ad578063f2fde38b146100c7575b5f5ffd5b61006c61006736600461025a565b6100da565b005b61006c61017d565b610098610084366004610271565b60016020525f908152604090205460ff1681565b60405190151581526020015b60405180910390f35b5f546040516001600160a01b0390911681526020016100a4565b61006c6100d5366004610271565b610190565b6100e26101d2565b5f81336040516100f19061024d565b6001600160a01b0390911681526020018190604051809103905ff590508015801561011e573d5f5f3e3d5ffd5b506001600160a01b0381165f81815260016020818152604092839020805460ff191690921790915590519182529192507f8e4ea796fcc01724fbc5f1860219b107c90d35650f18d63eea7db33505080d85910160405180910390a15050565b6101856101d2565b61018e5f6101fe565b565b6101986101d2565b6001600160a01b0381166101c657604051631e4fbdf760e01b81525f60048201526024015b60405180910390fd5b6101cf816101fe565b50565b5f546001600160a01b0316331461018e5760405163118cdaa760e01b81523360048201526024016101bd565b5f80546001600160a01b038381166001600160a01b0319831681178455604051919092169283917f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e09190a35050565b61048d8061029f83390190565b5f6020828403121561026a575f5ffd5b5035919050565b5f60208284031215610281575f5ffd5b81356001600160a01b0381168114610297575f5ffd5b939250505056fe6080604052348015600e575f5ffd5b5060405161048d38038061048d833981016040819052602b9160b4565b806001600160a01b038116605857604051631e4fbdf760e01b81525f600482015260240160405180910390fd5b605f816065565b505060df565b5f80546001600160a01b038381166001600160a01b0319831681178455604051919092169283917f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e09190a35050565b5f6020828403121560c3575f5ffd5b81516001600160a01b038116811460d8575f5ffd5b9392505050565b6103a1806100ec5f395ff3fe608060405260043610610042575f3560e01c806369328dec146100c8578063715018a6146100e95780638da5cb5b146100fd578063f2fde38b146101275761008a565b3661008a5760405162461bcd60e51b81526020600482015260136024820152720d4c0c48139bdd08125b5c1b195b595b9d1959606a1b60448201526064015b60405180910390fd5b60405162461bcd60e51b81526020600482015260136024820152720d4c0c48139bdd08125b5c1b195b595b9d1959606a1b6044820152606401610081565b3480156100d3575f5ffd5b506100e76100e2366004610312565b610146565b005b3480156100f4575f5ffd5b506100e7610169565b348015610108575f5ffd5b505f54604080516001600160a01b039092168252519081900360200190f35b348015610132575f5ffd5b506100e761014136600461034b565b61017c565b61014e6101b9565b806101636001600160a01b03821685856101e5565b50505050565b6101716101b9565b61017a5f61023c565b565b6101846101b9565b6001600160a01b0381166101ad57604051631e4fbdf760e01b81525f6004820152602401610081565b6101b68161023c565b50565b5f546001600160a01b0316331461017a5760405163118cdaa760e01b8152336004820152602401610081565b604080516001600160a01b038416602482015260448082018490528251808303909101815260649091019091526020810180516001600160e01b031663a9059cbb60e01b17905261023790849061028b565b505050565b5f80546001600160a01b038381166001600160a01b0319831681178455604051919092169283917f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e09190a35050565b5f5f60205f8451602086015f885af1806102aa576040513d5f823e3d81fd5b50505f513d915081156102c15780600114156102ce565b6001600160a01b0384163b155b1561016357604051635274afe760e01b81526001600160a01b0385166004820152602401610081565b80356001600160a01b038116811461030d575f5ffd5b919050565b5f5f5f60608486031215610324575f5ffd5b61032d846102f7565b925060208401359150610342604085016102f7565b90509250925092565b5f6020828403121561035b575f5ffd5b610364826102f7565b939250505056fea2646970667358221220a9350ab36912becbd90a6f81ea02eae631b3b10c2f8c9ab345490ae7a703477064736f6c634300081c0033a26469706673582212208ed88f76bd01113f73bd57857ce29d6f94f2ef51ef2cb77cdca1ad8ce2b808fe64736f6c634300081c0033";

    private static String librariesLinkedBinary;

    public static final String FUNC_CREATEWALLET = "createWallet";

    public static final String FUNC_OWNER = "owner";

    public static final String FUNC_RENOUNCEOWNERSHIP = "renounceOwnership";

    public static final String FUNC_TRANSFEROWNERSHIP = "transferOwnership";

    public static final String FUNC_WALLETS = "wallets";

    public static final Event CREATEWALLET_EVENT = new Event("CreateWallet", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
    ;

    public static final Event OWNERSHIPTRANSFERRED_EVENT = new Event("OwnershipTransferred", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}));
    ;

    @Deprecated
    protected Gateway(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected Gateway(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected Gateway(String contractAddress, Web3j web3j, TransactionManager transactionManager,
            BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected Gateway(String contractAddress, Web3j web3j, TransactionManager transactionManager,
            ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static List<CreateWalletEventResponse> getCreateWalletEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(CREATEWALLET_EVENT, transactionReceipt);
        ArrayList<CreateWalletEventResponse> responses = new ArrayList<CreateWalletEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            CreateWalletEventResponse typedResponse = new CreateWalletEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.param0 = (String) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static CreateWalletEventResponse getCreateWalletEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(CREATEWALLET_EVENT, log);
        CreateWalletEventResponse typedResponse = new CreateWalletEventResponse();
        typedResponse.log = log;
        typedResponse.param0 = (String) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<CreateWalletEventResponse> createWalletEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getCreateWalletEventFromLog(log));
    }

    public Flowable<CreateWalletEventResponse> createWalletEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(CREATEWALLET_EVENT));
        return createWalletEventFlowable(filter);
    }

    public static List<OwnershipTransferredEventResponse> getOwnershipTransferredEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(OWNERSHIPTRANSFERRED_EVENT, transactionReceipt);
        ArrayList<OwnershipTransferredEventResponse> responses = new ArrayList<OwnershipTransferredEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.previousOwner = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.newOwner = (String) eventValues.getIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static OwnershipTransferredEventResponse getOwnershipTransferredEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(OWNERSHIPTRANSFERRED_EVENT, log);
        OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
        typedResponse.log = log;
        typedResponse.previousOwner = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.newOwner = (String) eventValues.getIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<OwnershipTransferredEventResponse> ownershipTransferredEventFlowable(
            EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getOwnershipTransferredEventFromLog(log));
    }

    public Flowable<OwnershipTransferredEventResponse> ownershipTransferredEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(OWNERSHIPTRANSFERRED_EVENT));
        return ownershipTransferredEventFlowable(filter);
    }

    public RemoteFunctionCall<TransactionReceipt> createWallet(byte[] salt) {
        final Function function = new Function(
                FUNC_CREATEWALLET, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(salt)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<String> owner() {
        final Function function = new Function(FUNC_OWNER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> renounceOwnership() {
        final Function function = new Function(
                FUNC_RENOUNCEOWNERSHIP, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> transferOwnership(String newOwner) {
        final Function function = new Function(
                FUNC_TRANSFEROWNERSHIP, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(newOwner)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Boolean> wallets(String param0) {
        final Function function = new Function(FUNC_WALLETS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    @Deprecated
    public static Gateway load(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        return new Gateway(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static Gateway load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new Gateway(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static Gateway load(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        return new Gateway(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static Gateway load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new Gateway(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<Gateway> deploy(Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        return deployRemoteCall(Gateway.class, web3j, credentials, contractGasProvider, getDeploymentBinary(), "");
    }

    public static RemoteCall<Gateway> deploy(Web3j web3j, TransactionManager transactionManager,
            ContractGasProvider contractGasProvider) {
        return deployRemoteCall(Gateway.class, web3j, transactionManager, contractGasProvider, getDeploymentBinary(), "");
    }

    @Deprecated
    public static RemoteCall<Gateway> deploy(Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(Gateway.class, web3j, credentials, gasPrice, gasLimit, getDeploymentBinary(), "");
    }

    @Deprecated
    public static RemoteCall<Gateway> deploy(Web3j web3j, TransactionManager transactionManager,
            BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(Gateway.class, web3j, transactionManager, gasPrice, gasLimit, getDeploymentBinary(), "");
    }

    public static void linkLibraries(List<Contract.LinkReference> references) {
        librariesLinkedBinary = linkBinaryWithReferences(BINARY, references);
    }

    private static String getDeploymentBinary() {
        if (librariesLinkedBinary != null) {
            return librariesLinkedBinary;
        } else {
            return BINARY;
        }
    }

    public static class CreateWalletEventResponse extends BaseEventResponse {
        public String param0;
    }

    public static class OwnershipTransferredEventResponse extends BaseEventResponse {
        public String previousOwner;

        public String newOwner;
    }
}
