import { viem } from "hardhat";
import { expect } from "chai";
import { loadFixture } from "@nomicfoundation/hardhat-toolbox/network-helpers";

describe("Wallet", () => {
  async function deploy() {
    const client = await viem.getTestClient();

    return { client };
  }

  describe("Deploy", () => {
    it("deploy contract", async () => {
      const { client } = await loadFixture(deploy);
      expect(client).to.be.ok;
    });
  });

  describe("Access", () => {
    it("get owner", async () => {

    });

    it("allow transfer ownership", async () => {

    });

    it("reject transfer ownership", async () => {

    });
  });

  describe("Receiver", () => {
    it("get receiver", async () => {

    });
  
    it("set receiver", async () => {

    });

    it("reject set receiver", async () => {

    });
  });

  describe("Withdraw", () => {
    it("allow withdaw", async () => {
      
    });

    it("reject withdraw", async () => {

    });
  });
});