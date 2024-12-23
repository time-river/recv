import { createHash} from "crypto";

import { BytesLike } from "ethers";
import hre from "hardhat";
import { expect } from "chai";
import { loadFixture } from "@nomicfoundation/hardhat-toolbox/network-helpers";

import artifacts from "../artifacts/contracts/Gateway.sol/Account.json";

function sleep(callback: (args: void) => void, ms: number) {
  return new Promise((callback) => setTimeout(callback, ms));
}

describe("Gateway", function () {

  function buildCreate2Address(from: string, to: string, salt: BytesLike): string {
      // calculate address
      const argsBytes = new hre.ethers.AbiCoder().encode(["address"], [to]).slice(2);
      const initCode = [artifacts.bytecode, argsBytes].join('');
      const initCodeHash = hre.ethers.keccak256(initCode);
      const predict = hre.ethers.getCreate2Address(
        from,
        salt,
        initCodeHash
      );

      return predict;
  }

  before(() => {

  });

  async function depoly() {
    const [owner, otherAccount] = await hre.ethers.getSigners();

    const Gateway = await hre.ethers.getContractFactory("Gateway", { signer: owner });
    const gateway =  await Gateway.deploy();

    return { gateway, owner, otherAccount}
  }

  describe("deploy", function () {
    it("Should deploy", async function () {
      const { gateway, owner } = await loadFixture(depoly);

      console.log("owner: ", owner.address);
      console.log("contract address: ", await gateway.getAddress());

      expect(await gateway.owner()).to.equal(owner.address);
    });
  });

  describe("Sub-contract",  function(){
    it("Create sub-contract", async function() {
      const salt = createHash("sha256").update("user1").digest();
      const { gateway, owner, otherAccount } = await loadFixture(depoly);
      const to = otherAccount.address;
      let stopCycle = 10;

      // calculate address
      const predict = buildCreate2Address(await gateway.getAddress(), to, salt);

      let exist = await gateway.accounts(predict);
      console.log("predict: ", predict, ", exist: ", exist);
      expect(exist).to.equal(false);

      gateway.addListener("Create", (msg) => {
        console.log(`recv Create event msg: ${msg}`);
        stopCycle = 0;
      });

      const tx = await gateway.create(to, salt);
      console.log("transaction Id:", tx.hash);
      const rc = await tx.wait();

      expect(rc && rc.status == 1, "transaction failed").to.equal(true);
      if (!rc || rc.status != 1) {
        return;
      }

      exist = await gateway.accounts(predict);
      console.log("predict: ", predict, ", exist: ", exist);
      expect(exist).to.equal(true);

      while (stopCycle > 0) {
        await sleep(() => {stopCycle -= 1;}, 1000);
        stopCycle -= 1;
      }
    });

    it("Create repeat sub-contract", async function() {
      const salt = createHash("sha256").update("user12").digest();
      const { gateway, owner, otherAccount } = await loadFixture(depoly);
      const to = otherAccount.address;

      const predict = buildCreate2Address(await gateway.getAddress(), to, salt);
      let exist = await gateway.accounts(predict);
      console.log("predict: ", predict, ", exist: ", exist);
      expect(exist).to.equal(false);

      const tx = await gateway.create(to, salt);
      console.log("transaction Id:", tx.hash);
      const rc = await tx.wait();

      expect(rc && rc.status == 1, "transaction failed").to.equal(true);
      if (!rc || rc.status != 1) {
        return;
      }

      exist = await gateway.accounts(predict);
      console.log("predict: ", predict, ", exist: ", exist);
      expect(exist).to.equal(true);

      // repeat create contract
      let right = false;
      try {
        await gateway.create(to, salt);
      } catch (e: any) {
        console.log("Error msg:", e.message);
        right = true;
      } finally {
        expect(right).to.equal(true);
      }
    });
  });
});