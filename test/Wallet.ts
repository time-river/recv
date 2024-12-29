import {
  loadFixture,
} from "@nomicfoundation/hardhat-toolbox-viem/network-helpers";
import hre from "hardhat";
import { expect } from "chai";
import { bigint } from "hardhat/internal/core/params/argumentTypes";

describe("Wallet", () => {
  async function deploy() {
    const [owner, otherAccount] = await hre.viem.getWalletClients();

    const wallet = await hre.viem.deployContract("Wallet", []);
    const publicClient = await hre.viem.getPublicClient();

    return {wallet, owner, otherAccount, publicClient};
  }

  describe("Deploy", () => {
    it("deploy contract", async () => {
      await loadFixture(deploy);
    });
  });

  describe("Access", () => {
    it("get owner", async () => {
      const { wallet, owner } = await loadFixture(deploy);

      const addr1 = owner.account.address;
      const addr2 = await wallet.read.owner();
      expect(addr1.toLocaleLowerCase()).to.equal(addr2.toLocaleLowerCase());
    });

    it("allow transfer ownership", async () => {
      const { wallet, owner, otherAccount } = await loadFixture(deploy);

      const addr1 = await wallet.read.owner();
      const addr2 = otherAccount.account.address;
      expect(addr1.toLocaleLowerCase()).to.not.equal(addr2.toLocaleLowerCase());

      await wallet.write.transferOwnership([addr2]);
      const addr3 = await wallet.read.owner();
      expect(addr3.toLocaleLowerCase()).to.equal(addr2.toLocaleLowerCase());
    });

    it("reject transfer ownership", async () => {
      const { wallet, owner, otherAccount } = await loadFixture(deploy);

      try {
        await otherAccount.writeContract({
          address: wallet.address,
          abi: wallet.abi,
          functionName: "transferOwnership",
          args: [otherAccount.account.address]
        });
        expect(false).to.be.true;
      } catch(e: any) {
        expect(e.details).to.include("OwnableUnauthorizedAccount");
      }
    });

    it("renounce ownership", async () => {
      const { wallet } = await loadFixture(deploy);

      await wallet.write.renounceOwnership();
      const addr = await wallet.read.owner();

      expect(addr).to.equal("0x0000000000000000000000000000000000000000");
    });

    it("reject renounce ownership", async () => {
      const { wallet, owner, otherAccount } = await loadFixture(deploy);

      const addr = wallet.address;
      try {
        await otherAccount.writeContract({
          address: addr,
          abi: wallet.abi,
          functionName: "renounceOwnership",
          args: []
        });
        expect(false).to.be.true;
      } catch(e: any) {
        expect(e.details).to.include("OwnableUnauthorizedAccount");
      }
    });
  });

  describe("Withdraw", () => {
    // ignore: local network can't execute it
    // it("allow withdaw", async () => {
    //  
    // });

    it("reject withdraw", async () => {
      const { wallet, owner, otherAccount } = await loadFixture(deploy);
      const to = otherAccount.account.address;
      const amount = BigInt(1);

      try {
        await otherAccount.writeContract({
          address: wallet.address,
          abi: wallet.abi,
          functionName: "withdraw",
          args: [to, amount, to]
        });
        expect(false).to.be.true;
      } catch(e: any) {
        expect(e.details).to.include("OwnableUnauthorizedAccount");
      }
    });
  });

  describe("Transfer", () => {
    it("reject transfer native coin", async () => {
      const { wallet, owner, otherAccount } = await loadFixture(deploy);
      const to = otherAccount.account.address;
      const amount = BigInt(1);

      try {
        await owner.sendTransaction({
          to: wallet.address,
          value: amount
        });
        expect(false).to.be.true;
      } catch(e: any) {
        expect(e.details).to.include("501 Not Implemented");
      }
    });
  });
});