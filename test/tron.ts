import crypto from 'crypto';
import { expect } from "chai";
import { TronWeb } from 'tronweb';

import artifacts from '../artifacts/contracts/usdt.sol/Gateway.json';
import { clearInterval } from 'timers';

if (!process.env.TRON_PRIVATE_KEY_DEV) {
  throw new Error('TRON_PRIVATE_KEY_DEV environment variable is not set');
}

const private_key = process.env.TRON_PRIVATE_KEY_DEV; // 32 bytes
const port = process.env.TRON_PORT || 9090;
const full_node = "http://127.0.0.1:" + port;
const solidity_node = full_node;
const event_server = full_node;

describe("TRON-USDT", function() {
  let tronWeb: TronWeb;
  let contract: any;

  function digest(ctx: string): Buffer {
    const bytes32 = crypto.createHash('sha256').update(ctx).digest();
    return bytes32;
  }

  function sleep(callback: (args: void) => void, ms: number) {
    return new Promise((callback) => setTimeout(callback, ms));
  }

  function buildCreate2Address(creatorAddress: string, saltHex: string, byteCode: string) {
    //crypto.createHash
  }

  before(async function() {
    tronWeb = new TronWeb(
      full_node,
      solidity_node,
      event_server,
      private_key,
    );

    contract = await tronWeb.contract().new({
      abi: artifacts.abi,
      bytecode: artifacts.bytecode
    });
  });

  describe("Deployment Contract", function() {
    it("deploy contract and check the owner", async function() {
      // ref: https://github.com/tronprotocol/tronweb/blob/v5.3.2/test/utils/accounts.test.js#L21
      const address = TronWeb.address.fromPrivateKey(private_key);
      const hex = TronWeb.address.toHex(address as string);

      const ownerAddress = await contract.owner().call();
      expect(ownerAddress).to.equal(hex);
    });
  });

  async function createAccountContract(initAddress: string, to: string, salt: string): Promise<string> {
    let accountAddress = initAddress;
    let intervalStoped = false;
    const saltBytes = digest(salt);
    const txID = await contract.create(to, saltBytes).send();

    console.log("txID: ", txID);
    const intervalId = setInterval(async () => {
      await tronWeb.event.getEventsByTransactionID(txID)
        .then((event) => {
          console.log(event);
          if (event.data) {
            const data = event.data[0];
            accountAddress = data.result['0'];

            clearInterval(intervalId);
            intervalStoped = true;
          }
        });
    }, 1000);

    // wait 10 seconds
    let count = 10;
    while (count > 0) {
      await sleep(() => {}, 1000);

      if (intervalStoped) {
        break;
      }
      count -= 1;
    }

    return accountAddress;
  }

  describe("Create Account", function() {
    it("create one account", async function() {
      const initAddress = "";
      const to = "TXuegbzruFz4HGbSoAGdWJFfFGpH7SXEZL";
      const accountAddress = await createAccountContract(initAddress, to, "user1");

      console.log("addr: ", accountAddress);
      expect(accountAddress).to.not.equal(initAddress);
    });

    it("create repeat account", async function() {
      const initAddress = "";
      const to = "TXuegbzruFz4HGbSoAGdWJFfFGpH7SXEZL";
      // salt is the same as the previous
      const accountAddress = await createAccountContract(initAddress, to, "user1");

      expect(accountAddress).to.equal("");
    });

    it("create two account with different salt", async function() {
      const initAddress = "";
      const to = "TXuegbzruFz4HGbSoAGdWJFfFGpH7SXEZL";
      const accountAddress1 = await createAccountContract(initAddress, to, "user21");
      const accountAddress2 = await createAccountContract(initAddress, to, "user22");

      console.log("addr1: ", accountAddress1);
      console.log("addr2: ", accountAddress2);

      expect(accountAddress1).to.not.equal(initAddress);
      expect(accountAddress2).to.not.equal(initAddress);
      expect(accountAddress1).to.not.equal(accountAddress2);
    });

    it("be rejected", async function() {

    });

  });

  describe("Deposit & Withdraw", function() {

  });
})