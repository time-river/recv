import viem, { createPublicClient, createWalletClient, encodeFunctionData, encodePacked, getContract, getContractAddress, http, keccak256, parseAbi, parseAbiItem, parseEther, parseUnits, sha256, toBytes } from "viem";
import { sepolia } from "viem/chains";

import { expect } from "chai";
import { vars } from "hardhat/config";
import { privateKeyToAccount } from "viem/accounts";

import gatewayArtifacts from "../artifacts/contracts/Gateway.sol/Gateway.json";
import walletAtrifacts from "../artifacts/contracts/Wallet.sol/Wallet.json";
import { predictWalletAddress } from "./utils";

const USDTContractAddress: `0x${string}` = "0x7169D38820dfd117C3FA1f22a697dBA58d90BA06";

describe("Sepolia Gateway", function () {
  let gatewayContractAddress: `0x${string}` = '0x0000000000000000000000000000000000000000';

  function getAccounts() {
    const ownerKey = vars.get("SEPOLIA_OWNER_PRIVATE_KEY");
    const anotherKey = vars.get("SEPOLIA_ANOTHER_PRIVATE_KEY");
    if (!ownerKey.startsWith("0x")) {
      throw new Error("ownerKey should start with 0x");
    } else if (!anotherKey.startsWith("0x")) {
      throw new Error("anotherKey should start with 0x");
    }

    const ownerAccount = privateKeyToAccount(ownerKey as `0x{string}`);
    const anotherAccount = privateKeyToAccount(anotherKey as `0x{string}`);

    return {ownerAccount, anotherAccount};
  }

  function createContractClient(account: viem.Account, address: `0x${string}`, abi: viem.Abi | readonly unknown[]) {
    const walletClient = createWalletClient({
      account: account,
      chain: sepolia,
      transport: http(),
    });
    const publicClient = createPublicClient({
      chain: sepolia,
      transport: http(),
    });

    const contract = getContract({
      address: address,
      abi: abi,
      client: {
        public: publicClient,
        wallet: walletClient,
      }
    });

    return contract;
  }

  /* deploy gateway contract */
  before(async () => {
    //gatewayContractAddress = "0x8df56bc055901c23071e5487a9e61794db19b982";
    //return;

    const {ownerAccount, anotherAccount} = getAccounts();

    const walletClient = createWalletClient({
      account: ownerAccount,
      chain: sepolia,
      transport: http(),
    });
    const publicClient = createPublicClient({
      chain: sepolia,
      transport: http(),
    });

    const id = await walletClient.deployContract({
      abi: gatewayArtifacts.abi,
      bytecode: gatewayArtifacts.bytecode as `0x{string}`,
      account: ownerAccount,
      args: [],
    });
    console.log("deploy gateway transaction hash:", id);

    const rc = await publicClient.waitForTransactionReceipt({
      hash: id
    });
    console.log(`gateway contract address: ${rc.contractAddress}, gas used: ${rc.gasUsed}`);

    if (rc.status !== "success" || !rc.contractAddress) {
      throw new Error(`contract deploy failed: ${rc.status}`);
    }

    gatewayContractAddress = rc.contractAddress;
  });

  // security check
  describe("Access", function () {
    it("reject transfer ownership", async () => {
      const {ownerAccount, anotherAccount} = getAccounts();

      const contract = createContractClient(anotherAccount, gatewayContractAddress, gatewayArtifacts.abi);
      try {
        await contract.write.transferOwnership([ownerAccount.address]);
        expect(false).to.be.true;
      } catch(e: any) {
        /*
         * example:
         *  'Error: OwnableUnauthorizedAccount(address account)',
         *  '                                 (0x29a8f35834372aE51ad9CcFd3dC4080B3B9682Ac)',
         *  ' ',
         *  'Contract Call:',
         *  '  address:   0xf37e22316971dd1c056d00d1a0fd873532aa9dc7\n' +
         *  '  function:  transferOwnership(address newOwner)\n' +
         *  '  args:                       (0x03034947Fb6f497246e53Bd977ff1C2Cb47f743b)\n' +
         *  '  sender:    0x29a8f35834372aE51ad9CcFd3dC4080B3B9682Ac'
         */
        expect(e.metaMessages[0]).to.include("OwnableUnauthorizedAccount");
      }
    });

    it("reject renounce ownership", async () => {
      const {ownerAccount, anotherAccount} = getAccounts();
      const contract = createContractClient(anotherAccount, gatewayContractAddress, gatewayArtifacts.abi);

      try {
        await contract.write.renounceOwnership([]);
        expect(false).to.be.true;
      } catch(e: any) {
        expect(e.metaMessages[0]).to.include("OwnableUnauthorizedAccount");
      }
    });
  });

  describe("Wallet", async () => {
    let walletContractAddress: `0x${string}` = "0x0000000000000000000000000000000000000000";

    it("create wallet", async () => {
      const {ownerAccount, anotherAccount} = getAccounts();
      const contract = createContractClient(ownerAccount, gatewayContractAddress, gatewayArtifacts.abi);
      const salt = sha256(toBytes("user1" + new Date().getTime().toString()));
      let generate: `0x${string}` | undefined = undefined;
      let wait = 30;

      // calculate create2 address
      const predict = predictWalletAddress(contract.address, ownerAccount.address, salt);

      const publicClient = createPublicClient({
        chain: sepolia,
        transport: http(),
      });
      const unwatch = publicClient.watchEvent({
        address: contract.address,
        event: parseAbiItem("event CreateWallet(address)"),
        onLogs: (logs) => {
          generate = logs[0].args[0];
          wait = 0;
        }
      });

      let exist = await publicClient.readContract({
        address: contract.address,
        abi: contract.abi,
        functionName: "wallets",
        args: [predict],
      });
      expect(exist).to.equal(false);

      const tx = await contract.write.createWallet([salt]);
      console.log("createWallet transaction id: ", tx);
      const rc = await publicClient.waitForTransactionReceipt({
        hash: tx,
      });
      expect(rc.status).to.equal("success");

      while (wait > 0) {
        await new Promise((resolve) => setTimeout(resolve, 1000));
        wait -= 1;
      }

      unwatch();
      expect(generate).to.equal(predict);

      exist = await publicClient.readContract({
        address: contract.address,
        abi: contract.abi,
        functionName: "wallets",
        args: [predict],
      });
      expect(exist).to.equal(true);

      walletContractAddress = predict;
      console.log("wallet contract address: ", predict);
    });

    it("reject create wallet", async () => {
      const {ownerAccount, anotherAccount} = getAccounts();
      const contract = createContractClient(anotherAccount, gatewayContractAddress, gatewayArtifacts.abi);
      const salt = sha256(toBytes("user1" + new Date().getTime().toString()));

      try {
        await contract.write.createWallet([salt]);
        expect(false).to.be.true;
      } catch(e: any) {
        expect(e.metaMessages[0]).to.include("OwnableUnauthorizedAccount");
      }
    });

    it("reject transfer native coin", async () => {
      if (walletContractAddress === "0x0000000000000000000000000000000000000000") {
        throw new Error("wallet contract address is not created")
      }

      const { ownerAccount } = getAccounts();
      const walletClient = createWalletClient({
        account: ownerAccount,
        chain: sepolia,
        transport: http(),
      });
  
      try {
        await walletClient.sendTransaction({
          to: walletContractAddress,
          value: 1n
        });
        expect(false).to.be.true;
      } catch(e: any) {
        expect(e.shortMessage).to.include("501 Not Implemented");
      }
    });

    async function getUSDTBalance(address: `0x${string}`): Promise<bigint> {
      const publicClient = createPublicClient({
        chain: sepolia,
        transport: http(),
      });

      const amount = await publicClient.readContract({
        address: USDTContractAddress,
        abi: parseAbi(["function balanceOf(address owner) view returns (uint256)"]),
        functionName: "balanceOf",
        args: [address],
      });

      return amount;
    }

    it("allow withdraw USDT", async () => {
      if (walletContractAddress === "0x0000000000000000000000000000000000000000") {
        throw new Error("wallet contract address is not created")
      }

      const publicClient = createPublicClient({
        chain: sepolia,
        transport: http(),
      });
      const {ownerAccount, anotherAccount} = getAccounts();
      const ownerWalletClient = createWalletClient({
        account: ownerAccount,
        chain: sepolia,
        transport: http(),
      });
      const amount = parseUnits("1", 6);

      // transfer
      const transferData = encodeFunctionData({
        abi: parseAbi(["function transfer(address to, uint256 value) external returns (bool)"]),
        functionName: "transfer",
        args: [walletContractAddress, amount], // 1 USDT
      });
      const tx1 = await ownerWalletClient.sendTransaction({
        to: USDTContractAddress,
        data: transferData,
        value: 0n,
        from: ownerWalletClient.account.address,
      })
      console.log("USDT transfer transaction id: ", tx1);
      const rc1 = await publicClient.waitForTransactionReceipt({
        hash: tx1,
      });
      expect(rc1.status).to.equal("success");

      // withdraw
      const contract = createContractClient(ownerAccount, walletContractAddress, walletAtrifacts.abi);
      console.log("contract owner: ", await contract.read.owner([]));
      console.log("contract: ", contract.address, " , gateway: ", gatewayContractAddress);
      console.log("account: ", ownerAccount.address);
      try {
        const tx2 = await contract.write.withdraw(
          [ownerAccount.address, amount, USDTContractAddress]
        );
        console.log("withdraw transaction id: ", tx2);
        const rc2 = await publicClient.waitForTransactionReceipt({
          hash: tx2,
        });
        expect(rc2.status).to.equal("success");
      } catch(e) {
        console.log(e);
      }
    });

    it("reject withdraw USDT", async () => {
      if (!walletContractAddress) {
        throw new Error("wallet contract address is not created")
      }

      const {ownerAccount, anotherAccount} = getAccounts();
      const contract = createContractClient(anotherAccount, walletContractAddress, walletAtrifacts.abi);

      try {
        await contract.write.withdraw(
          [ownerAccount.address, parseUnits("1", 6), USDTContractAddress]
        );
        expect(false).to.be.true;
      } catch(e: any) {
        expect(e.metaMessages[0]).to.include("OwnableUnauthorizedAccount");
      }
    });
  });
});