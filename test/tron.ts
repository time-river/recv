import crypto from 'crypto';
import { clearInterval } from 'timers';
import { expect } from "chai";
import { TronWeb } from 'tronweb';
import { BytesLike } from 'tronweb/lib/esm/types';
import * as web3Utils from 'web3-utils';

import artifacts from '../artifacts/contracts/usdt.sol/Gateway.json';
import accountArtifacts from '../artifacts/contracts/usdt.sol/Account.json';

if (!process.env.TRON_PRIVATE_KEY_DEV || !process.env.TRON_PRIVATE_KEY_DEV1) {
  throw new Error('TRON_PRIVATE_KEY_DEV environment variable is not set');
}

// wallets: http://172.17.0.2:9090/admin/accounts?format=all
const private_key = process.env.TRON_PRIVATE_KEY_DEV; // 32 bytes
const private_key1 = process.env.TRON_PRIVATE_KEY_DEV1;
const port = process.env.TRON_PORT || 9090;
const full_node = "http://127.0.0.1:" + port;
const solidity_node = full_node;
const event_server = full_node;

describe("TRON-LOCAL", function() {
  let tronWeb: TronWeb;
  let contract: any;

  function digest(ctx: string): Buffer {
    const bytes32 = crypto.createHash('sha256').update(ctx).digest();
    return bytes32;
  }

  function sleep(callback: (args: void) => void, ms: number) {
    return new Promise((callback) => setTimeout(callback, ms));
  }

  function buildInitCode(byteCode: string, toBase58: string): string {
    // convert base58 encoded address to raw address (hex identifier)
    // 41 is the tron network prefix, drop it, then padding 0 to 32 bytes
    const base58Hex = TronWeb.address.toHex(toBase58).replace(/^41/, '');
    const bytes32 = base58Hex.padStart(64, '0');

    return byteCode + bytes32;
  }

  /*
   * @creatorAddress: raw address (hex identifier)
   * @to: base58 address
   *
   * ref:
   * - https://gist.github.com/miguelmota/c9102d370a3c1891dbd23e821be82ae2
   * - https://docs.soliditylang.org/en/latest/control-structures.html#salted-contract-creations-create2
   * - https://developers.tron.network/docs/tvm#differences-from-evm
   */
  function buildCreate2Address(creatorAddress: string, saltHex: string, initCode: string): string {
    const initCodeHash = web3Utils.sha3(initCode) as string;

    if (creatorAddress.startsWith('0x')) {
      creatorAddress = creatorAddress.slice(2);
    }
    creatorAddress = creatorAddress.replace(/^41/, ''); // drop `41` prefix

    const parts = [
      '41',
      creatorAddress,
      saltHex,
      initCodeHash
    ];

    const bytes = parts.map(x => x?.replace(/^0x/, '')).join('');
    const hash = web3Utils.sha3(`0x${bytes}`);

    return '0x' + hash?.slice(-40).toLowerCase();
  }

  async function createAccountContract(initAddress: string, to: string, saltHex: BytesLike): Promise<string> {
    let accountAddress = initAddress;
    let intervalStoped = false;
    const txID = await contract.create(to, saltHex).send();

    const intervalId = setInterval(async () => {
      tronWeb.event.getEventsByTransactionID(txID)
        .then((event) => {
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

  describe("Create Account", function() {
    it("create one account with predictable address", async function() {
      const to = "TXuegbzruFz4HGbSoAGdWJFfFGpH7SXEZL"; // base58 encoded address
      const saltBytes = digest("user1");
      const saltHex = saltBytes.toString('hex');
      let exist = false;

      // calculate CREATE2 address
      const initCode = buildInitCode(accountArtifacts.bytecode, to);
      const predictedAccountAddress = buildCreate2Address(contract.address, saltHex, initCode);
      console.log("predictedAccountAddress: ", predictedAccountAddress);

      exist = await contract.accounts(predictedAccountAddress).call();
      expect(exist).to.equal(false);

      const accountAddress = await createAccountContract("", to, saltBytes);
      console.log("accountAddress: ", accountAddress);
      expect(accountAddress).to.equal(predictedAccountAddress);

      exist = await contract.accounts(predictedAccountAddress).call();
      expect(exist).to.equal(true);
    });

    it("create two account with different salt", async function() {
      const initAddress = "";
      const to = "TXuegbzruFz4HGbSoAGdWJFfFGpH7SXEZL";
      const accountAddress1 = await createAccountContract(initAddress, to, digest("user21"));
      const accountAddress2 = await createAccountContract(initAddress, to, digest("user22"));

      console.log("addr1: ", accountAddress1);
      console.log("addr2: ", accountAddress2);

      expect(accountAddress1).to.not.equal(initAddress);
      expect(accountAddress2).to.not.equal(initAddress);
      expect(accountAddress1).to.not.equal(accountAddress2);
    });

    it("create repeat account", async function() {
      const to = "TXuegbzruFz4HGbSoAGdWJFfFGpH7SXEZL";
      // salt is the same as the previous
      const saltBytes = digest("user1");
      const saltHex = saltBytes.toString('hex');

      // calculate CREATE2 address
      const initCode = buildInitCode(accountArtifacts.bytecode, to);
      const predictedAccountAddress = buildCreate2Address(contract.address, saltHex, initCode);
      const exist = await contract.accounts(predictedAccountAddress).call();

      // ensure address exists contract
      expect(exist).to.equal(true);

      const accountAddress = await createAccountContract("", to, saltBytes);

      // TODO: how to check?
      expect(accountAddress).to.equal("");
    });

    it("be rejected", async function() {
      const to = "TXuegbzruFz4HGbSoAGdWJFfFGpH7SXEZL";
      const tronWeb1 = new TronWeb(
        full_node,
        solidity_node,
        event_server,
        private_key1,
      );

      // TODO: how to check
      const contract1 = await tronWeb1.contract().at(contract.address);
      const saltBytes = digest("rejectedUser");
      const txID = await contract1.create(to, saltBytes).send();
      console.log("txID: ", txID);
    });

  });

  // TODO:
  describe("Check", function() {
    it("check the balance of TRX", async function() {
      const address = tronWeb.defaultAddress["base58"];
      const balance = await tronWeb.trx.getBalance(address);

      console.log("balance: ", balance);
    });

    it("check bandwidth & energy")
    it("check the balance of USDT", async function() {
      const to = "000";
      const saltBytes = digest("user1");
      const saltHex = saltBytes.toString('hex');
      let exist = false;

      // calculate CREATE2 address
      const initCode = buildInitCode(accountArtifacts.bytecode, to);
      const predictedAccountAddress = buildCreate2Address(contract.address, saltHex, initCode);
      console.log("predictedAccountAddress: ", predictedAccountAddress);
    });

  });

})