import viem, { formatEther, formatUnits, parseAbi, parseUnits } from "viem";
import { createPublicClient, createWalletClient, http } from "viem";
import { sepolia } from "viem/chains";
import { getBalance } from "viem/_types/actions/public/getBalance";

const publicClient = createPublicClient({
    chain: sepolia,
    transport: http(),
});

const walletAddress = "0x03034947Fb6f497246e53Bd977ff1C2Cb47f743b";
const usdtContractAddress = "0x7169d38820dfd117c3fa1f22a697dba58d90ba06";
const emptyContractAddress = "0xF0bb84D0C5Fc76ea6e7DDA092423C7b2284e4191";

async function addressType(address: `0x${string}`, type: string) {
    const code = await publicClient.getCode({address});

    if (code) {
        console.log("contract address: ", address, " type: ", type);
    } else {
        console.log("wallet address: ", address, " type: ", type);
    }
}

async function block() {
    const nr = await publicClient.getBlockNumber();
    console.log("block number: ", nr);
}

async function ethBalance(address: `0x${string}`) {
    const val = await publicClient.getBalance({address});
    console.log("eth balance: ", val, typeof(val));
    const eth = formatEther(val);
    console.log("eth: ", eth);
}

async function erc20Balance(address: `0x${string}`) {
    const val = await publicClient.readContract({
        address: usdtContractAddress,
        abi: parseAbi([
            "function balanceOf(address owner) view returns (uint256)"
        ]),
        functionName: "balanceOf",
        args: [address],
    });
    
    console.log("USDT balance:", val);
    const usdt = formatUnits(val, 6);
    console.log("usdt: ", usdt);
}

async function main() {
    console.log(">>>>>>>>>>>> block test >>>>>>>>>>>")
    await block();

    console.log(">>>>>>>>>>>> address type >>>>>>>>>>>");
    await addressType(walletAddress, "wallet");
    await addressType(usdtContractAddress, "usdt contract");
    await addressType(emptyContractAddress, "empty contract");

    console.log(">>>>>>>>>>>> wallet balance >>>>>>>>>>>>>>>>");
    await ethBalance(walletAddress);
    await erc20Balance(walletAddress);

    console.log(">>>>>>>>>>>>> usdt contract balance >>>>>>>>>>");
    await ethBalance(usdtContractAddress);
    await erc20Balance(usdtContractAddress);

    console.log(">>>>>>>>>>>>> empty contract balance >>>>>>>>>>");
    await ethBalance(emptyContractAddress);
    await erc20Balance(emptyContractAddress);
}

main().then(() => {});
// watch wallet event: poll?


// transfer to wallet
