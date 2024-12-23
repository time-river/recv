import { buildModule } from '@nomicfoundation/hardhat-ignition/modules';

export default buildModule("Gateway", (m) => {
    const token = m.contract("Gateway", []);

    return { token };
});