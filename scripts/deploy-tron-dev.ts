import { TronWeb } from 'tronweb';

import artifacts from '../artifacts/contracts/usdt.sol/Gateway.json';

if (!process.env.TRON_PRIVATE_KEY_DEV) {
  throw new Error('TRON_PRIVATE_KEY_DEV environment variable is not set');
}

const priv_key = process.env.TRON_PRIVATE_KEY_DEV; // 32 bytes
const port = process.env.TRON_PORT || 9090;
const full_node = "http://127.0.0.1:" + port;
const solidity_node = full_node;
const event_server = full_node;

function main() {
  const tronWeb = new TronWeb(
      full_node,
      solidity_node,
      event_server,
      priv_key,
  );

  tronWeb.contract().new({
    abi: artifacts.abi,
    bytecode: artifacts.bytecode
  }).then((contract) => {
    console.log('contract', contract);
    console.log(contract.address);
  })
};

main();