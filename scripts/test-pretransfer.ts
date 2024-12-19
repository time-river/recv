import { Contract, TronWeb } from "tronweb";
import * as web3Utils from 'web3-utils';
import crypto from 'crypto';

import artifacts from '../artifacts/contracts/usdt.sol/Gateway.json';
import accountArtifacts from '../artifacts/contracts/usdt.sol/Account.json';
import { BytesLike } from "tronweb/lib/esm/types";
import { EventResponse } from "tronweb/lib/esm/lib/event";

const priv_key = "72748f3ff6a1cb8306a4a3994283a3f7f640fae465d98f12a4c98d2d619e2697";
const port = process.env.TRON_PORT || 9090;
const full_node = "http://127.0.0.1:" + port;
const solidity_node = full_node;
const event_server = full_node;

const _address = TronWeb.address.fromPrivateKey(priv_key) as string;
console.log("wallet address: ", TronWeb.address.toHex(_address));
const tronWeb = new TronWeb(
  full_node,
  solidity_node,
  event_server,
  priv_key
);

function digest(ctx: string): Buffer {
  const bytes32 = crypto.createHash('sha256').update(ctx).digest();
  return bytes32;
}

function buildInitCode(byteCode: string, toBase58: string): string {
  // convert base58 encoded address to raw address (hex identifier)
  // 41 is the tron network prefix, drop it, then padding 0 to 32 bytes
  const base58Hex = TronWeb.address.toHex(toBase58).replace(/^41/, '');
  const bytes32 = base58Hex.padStart(64, '0');

  return byteCode + bytes32;
}

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

function sleep(callback: (args: void) => void, ms: number) {
  return new Promise((callback) => setTimeout(callback, ms));
}

async function depoly() {
  const contract = await tronWeb.contract().new({
    abi: artifacts.abi,
    bytecode: artifacts.bytecode
  });

  return contract;
}

async function createAccountContract(contract: Contract, to: string, saltHex: BytesLike): Promise<string> {
  const owner = await contract.owner().call();
  console.log("owner: ", owner);

  let accountAddress = "";
  let intervalStoped = false;
  const txID = await contract.create(to, saltHex).send();

  console.log("txID: ", txID);

  const intervalId = setInterval(async () => {
    const event = await tronWeb.event.getEventsByTransactionID(txID);
    if (event.data && event.data.length != 0) {
      const data = event.data[0];
      accountAddress = data.result['0'];

      clearInterval(intervalId);
      intervalStoped = true;
    }
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

async function getBalance(address: string): Promise<number> {
  const tron = new TronWeb(
    full_node,
    solidity_node,
    event_server,
  );
  const balance = await tron.trx.getBalance(address);

  return balance / 1000000 ;
}

async function contractState(address: string) {
  const addr = TronWeb.address.toHex(address);
  console.log("addr: ", address, addr);
  const tron = new TronWeb(
    full_node,
    solidity_node,
    event_server,
  );

  const result = await tron.trx.getContract(addr);
  console.log(">>> ", result)
  const exist = result.abi && result.byteCode;
  console.log(addr, " exist: ", exist);
}

function transfer() {

}

async function main() {
  const address = await TronWeb.address.fromPrivateKey(priv_key) as string;
  console.log("wallet balance: ", await getBalance(address))
  depoly().then(async (contract: Contract) => {
    contractState(contract.address)
    console.log("contract address: ", contract.address);
    const owner = await contract.owner().call();
    console.log("owner: ", owner);

    console.log("self balance: ", await getBalance(owner));

    const to = 'TWMqkkHMbcTQFh2LVMuNW4bc7uGNFmuwPg';
    const saltBytes = digest("user1");
    const saltHex = saltBytes.toString('hex');

    const initCode = buildInitCode(accountArtifacts.bytecode, to);
    let address = buildCreate2Address(contract.address as string, saltHex, initCode);
    console.log("predict address: ", address, await contract.accounts(address).call());
    contractState(address);

    address = await createAccountContract(contract, to, saltBytes);
    console.log("true address: ", address, await contract.accounts(address).call());
    contractState(address);

    //console.log("self balance: ", await getBalance(owner));
  });
}

main().then(() => {});