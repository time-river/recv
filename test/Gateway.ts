import {
  loadFixture,
} from "@nomicfoundation/hardhat-toolbox-viem/network-helpers";
import hre from "hardhat";
import { expect } from "chai";
import { encodeAbiParameters, encodePacked, getContractAddress, keccak256, parseAbiItem, sha256, toBytes } from "viem";

import artifacts from "../artifacts/contracts/Wallet.sol/Wallet.json";

function sleep(ms: number) {
  const cb = () => {};
  return new Promise((cb) => setTimeout(cb, ms));
}

describe("Gateway", () => {
  async function deploy() {
    // Contracts are deployed using the first signer/account by default
    const [owner, otherAccount] = await hre.viem.getWalletClients();

    const gateway = await hre.viem.deployContract("Gateway", []);
    const publicClient = await hre.viem.getPublicClient();

    return {gateway, owner, otherAccount, publicClient};
  }

  describe("Deploy", () => {
    it("deploy contract", async () => {
      await loadFixture(deploy);
    });
  });

  describe("Access", () => {
    it("get owner", async () => {
      const { gateway, owner } = await loadFixture(deploy);

      const addr1 = owner.account.address;
      const addr2 = await gateway.read.owner();
      expect(addr1.toLocaleLowerCase()).to.equal(addr2.toLocaleLowerCase());
    });

    it("allow transfer ownership", async () => {
      const { gateway, owner, otherAccount } = await loadFixture(deploy);

      const addr1 = await gateway.read.owner();
      const addr2 = otherAccount.account.address;
      expect(addr1.toLocaleLowerCase()).to.not.equal(addr2.toLocaleLowerCase());

      await gateway.write.transferOwnership([addr2]);
      const addr3 = await gateway.read.owner();
      expect(addr3.toLocaleLowerCase()).to.equal(addr2.toLocaleLowerCase());
    });

    it("reject transfer ownership", async () => {
      const { gateway, owner, otherAccount } = await loadFixture(deploy);

      try {
        await otherAccount.writeContract({
          address: gateway.address,
          abi: gateway.abi,
          functionName: "transferOwnership",
          args: [otherAccount.account.address]
        });
        expect(false).to.be.true;
      } catch(e: any) {
        expect(e.details).to.include("OwnableUnauthorizedAccount");
      }
    });

    it("renounce ownership", async () => {
      const { gateway } = await loadFixture(deploy);

      await gateway.write.renounceOwnership();
      const addr = await gateway.read.owner();

      expect(addr).to.equal("0x0000000000000000000000000000000000000000");
    });

    it("reject renounce ownership", async () => {
      const { gateway, owner, otherAccount } = await loadFixture(deploy);

      const addr = gateway.address;
      try {
        await otherAccount.writeContract({
          address: addr,
          abi: gateway.abi,
          functionName: "renounceOwnership",
          args: []
        });
        expect(false).to.be.true;
      } catch(e: any) {
        expect(e.details).to.include("OwnableUnauthorizedAccount");
      }
    });
  });

  /* 
   * 1. test the generation rule of CREATE2
   * 2. test `createWallet` function
   * 3. test `wallets` public variable
   */ 
  describe("Create sub-contract", () => {
    it("create & predict wallet", async () => {
      const salt = sha256(toBytes("user1"));
      const { gateway, owner, otherAccount, publicClient } = await loadFixture(deploy);
      let generate: `0x${string}` | undefined = undefined;
      let wait = 10;

      const unwatch = publicClient.watchEvent({
        address: gateway.address,
        event: parseAbiItem("event CreateWallet(address)"),
        onLogs: (logs) => {
          generate = logs[0].args[0];
          wait = 0;
        }
      });

      // calculate create2 address
      const initCode = encodePacked(
        ["bytes"],
        [artifacts.bytecode as `0x${string}`]
      );
      const predict = getContractAddress({
        bytecodeHash: keccak256(initCode),
        from: gateway.address,
        opcode: "CREATE2",
        salt: salt,
      });

      let exist = await publicClient.readContract({
        address: gateway.address,
        abi: gateway.abi,
        functionName: "wallets",
        args: [predict],
      });
      expect(exist).to.equal(false);

      await gateway.write.createWallet([salt]);

      while (wait > 0) {
        await sleep(1000);
        wait -= 1;
      }

      unwatch();
      expect(wait).to.lessThanOrEqual(0);

      expect(generate).to.equal(predict);

      exist = await publicClient.readContract({
        address: gateway.address,
        abi: gateway.abi,
        functionName: "wallets",
        args: [predict],
      });
      expect(exist).to.equal(true);
    });

    it("reject create wallet", async () => {
      const salt = sha256(toBytes("user1"));
      const { gateway, owner, otherAccount, publicClient } = await loadFixture(deploy);

      try {
        await otherAccount.writeContract({
          address: gateway.address,
          abi: gateway.abi,
          functionName: "createWallet",
          args: [salt]
        });
        expect(false).to.be.true;
      } catch(e: any) {
        expect(e.details).to.include("OwnableUnauthorizedAccount");
      }
    });
  });  
});