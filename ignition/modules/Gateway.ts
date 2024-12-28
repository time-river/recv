import { buildModule } from "@nomicfoundation/hardhat-ignition/modules";

const GatewayModule = buildModule("Gateway", (m) => {

  const gateway = m.contract("Lock", []);

  return { gateway };
});

export default GatewayModule;