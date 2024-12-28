import { encodeAbiParameters, encodePacked, getContractAddress, keccak256, parseAbiParameters } from "viem";

import artifacts from "../artifacts/contracts/Wallet.sol/Wallet.json";

export function predictWalletAddress(
  from: `0x${string}`, arg: `0x${string}`, salt: `0x${string}`
): `0x${string}` {
  const byteCode = artifacts.bytecode as `0x${string}`;
  const argsBytes = encodeAbiParameters(
    parseAbiParameters("address owner"),
    [arg]
  );
  const initCode = encodePacked(
    ["bytes", "bytes"],
    [byteCode, argsBytes]
  );
  const predict = getContractAddress({
    bytecodeHash: keccak256(initCode),
    from: from,
    opcode: "CREATE2",
    salt: salt,
  });

  return predict;
}