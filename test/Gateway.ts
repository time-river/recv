import { createHash} from "crypto";

import { BytesLike, Contract, Provider } from "ethers";
import hre from "hardhat";
import { expect } from "chai";
import { loadFixture } from "@nomicfoundation/hardhat-toolbox/network-helpers";

import artifacts from "../artifacts/contracts/Gateway.sol/Account.json";

function sleep(callback: (args: void) => void, ms: number) {
  return new Promise((callback) => setTimeout(callback, ms));
}


async function scanBlocks(provider: Provider, from: string) {
  let lastBlockNumber = await provider.getBlockNumber();

  setInterval(async () => {
    const currentBlockNumber = await provider.getBlockNumber();

    if (currentBlockNumber > lastBlockNumber) {
        for (let i = lastBlockNumber + 1; i <= currentBlockNumber; i++) {
            const block = await provider.getBlock(i, true);

            block?.prefetchedTransactions.forEach(tx => {
                // 检查交易中的发送或接收地址

                console.log(tx.toJSON());
                //if (tx.from === from) {
                //    console.log(`检测到相关交易: ${tx.hash}`);
                //    console.log(tx.data);
                    // 进一步处理（解析交易等）
                //}
            });
        }
        lastBlockNumber = currentBlockNumber;
    }
  }, 1000); // 每 1 秒检查一次
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

  async function getBalance(address: string) {
    const wei = await hre.ethers.provider.getBalance(address);
    const eth = hre.ethers.formatEther(wei);
 
    return { wei, eth };
  }

  async function depoly() {
    const [owner, otherAccount] = await hre.ethers.getSigners();

    const Gateway = await hre.ethers.getContractFactory("Gateway", { signer: owner });
    const gateway =  await Gateway.deploy();
    await gateway.waitForDeployment();

    return { gateway, owner, otherAccount}
  }

  describe("Deploy", function () {
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

      gateway.once("Create", (msg) => {
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
      gateway.removeAllListeners("Create");
    });

    it("Create repeat sub-contract", async function() {
      const { gateway, owner, otherAccount } = await loadFixture(depoly);
      const salt = createHash("sha256").update("user12").digest();
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

  describe("Balance & Transfer", function() {
    it("Wallet of balance & transfer", async function() {
      const [owner, otherAccount] = await hre.ethers.getSigners();

      const balance1 = await getBalance(owner.address);
      console.log(`wallet ${owner.address} balance: ${balance1.eth}`);

      const balance2 = await getBalance(otherAccount.address); 
      console.log(`${otherAccount.address} balance: ${balance2.eth}`);

      const amount = hre.ethers.parseEther("1");
      const tx = {
        to: otherAccount.address,
        value: amount,
      };
      await owner.sendTransaction(tx);

      const balance3 = await getBalance(otherAccount.address);
      console.log(`${otherAccount.address} balance: ${balance3.eth}`);
      expect(balance3.wei).to.equal(balance2.wei+amount);
    });

    it("Balance of existing contract balance", async function() {
      const { gateway, owner } = await loadFixture(depoly);

      const address = await gateway.getAddress();
      const { eth } = await getBalance(address);
      console.log(`contract ${address} balance: ${eth}`);
    });

    it("Contract balance & transfer", async function() {
      const { gateway, owner, otherAccount } = await loadFixture(depoly);
      const to = otherAccount.address;
      const salt = createHash("sha256").update("user1223").digest();

      const predict = buildCreate2Address(await gateway.getAddress(), to, salt);
      let exist = await gateway.accounts(predict);
      expect(exist).to.equal(false);

      // 1st check
      const balance1 = await getBalance(predict);
      console.log(`1st, contract ${predict} balance: ${balance1.eth}`);
      expect(balance1.wei).to.equal(0);

      // 1st transfer
      const amount = hre.ethers.parseEther("1");
      const msg = {
        to: predict,
        value: amount,
      };
      await owner.sendTransaction(msg);

      // 2nd check
      const balance2 = await getBalance(predict);
      console.log(`2nd, contract ${predict} balance: ${balance2.eth}`);
      expect(balance2.wei).to.equal(amount);

      // create sub-contract
      const tx = await gateway.create(to, salt);
      const rc = await tx.wait();
      expect(rc && rc.status == 1, "transaction failed").to.equal(true);
      if (!rc || rc.status != 1) {
        return;
      }

      exist = await gateway.accounts(predict);
      expect(exist).to.equal(true);

      // 3rd check
      const balance3 = await getBalance(predict);
      console.log(`3nd, contract ${predict} balance: ${balance3.eth}`);
      expect(balance3.wei).to.equal(amount);

      // 2nd transfer
      await owner.sendTransaction(msg);

      // 4th check
      const balance4 = await getBalance(predict);
      console.log(`4th, contract ${predict} balance: ${balance4.eth}`);
      expect(balance4.wei).to.equal(amount+amount);
    });

    it("Balance of ERC20", async function() {

    });
  });
});