import { HardhatUserConfig } from "hardhat/config";
import "@nomicfoundation/hardhat-toolbox";

const config: HardhatUserConfig = {
  networks: {
    tron_hasta: {
      url: "https://api.shasta.trongrid.io",
      accounts: [process.env.PRIVATE_KEY_SHASTA || ""]
    },
  },
  solidity: {
    version: "0.8.28",
    settings: {
      optimizer: {
        enabled: true,
        runs: 200
      }
    }
  }
};

export default config;
