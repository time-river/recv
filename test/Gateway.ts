import { createHash} from "crypto";

import hre from "hardhat";
import { expect } from "chai";
import { loadFixture } from "@nomicfoundation/hardhat-toolbox/network-helpers";

import artifacts from "../artifacts/contracts/Gateway.sol/Gateway.json";

describe("Gateway", function () {

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
      const { gateway, owner, otherAccount } = await loadFixture(depoly);

      console.log("owner: ", owner.address);
      console.log("contract address: ", await gateway.getAddress());

      expect(await gateway.owner()).to.equal(owner.address);
    });
  });

  describe("Create sub-contract",  function(){
    it("Create sub-contract", async function() {
      const salt = createHash("sha256").update("user1").digest();
      const { gateway, owner, otherAccount } = await loadFixture(depoly);
      const to = otherAccount.address.toLocaleLowerCase();

      gateway.addListener("Create", (event) => console.log(">>> Create event:", event));

      // TODO: calculate address
      const argsBytes = new hre.ethers.AbiCoder().encode(["address"], [to]).slice(2);
      const initCode = [artifacts.bytecode, argsBytes].join('');
      const initCodeHash = hre.ethers.keccak256(initCode);
      const predict = hre.ethers.getCreate2Address(to, salt, initCodeHash).toLocaleLowerCase();
      console.log("predict: ", predict);

      const exist = await gateway.accounts(predict);
      expect(exist).to.equal(false);

      const tx = await gateway.create(otherAccount.address, salt);
      console.log("transaction Id:", tx.hash);
      const rc = await tx.wait();

      expect(rc && rc.status == 1, "transaction failed").to.equal(true);
      if (!rc || rc.status != 1) {
        return;
      }

      console.log("result:", rc.toJSON());
    });
  });
});