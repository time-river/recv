// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.28;

import { Ownable } from '@openzeppelin/contracts/access/Ownable.sol';
import { IERC20 } from '@openzeppelin/contracts/token/ERC20/IERC20.sol';
import { SafeERC20 } from '@openzeppelin/contracts/token/ERC20/utils/SafeERC20.sol';

contract Wallet is Ownable {
    using SafeERC20 for IERC20;

    constructor(address owner) Ownable(owner) {}

    receive() external payable {
        revert("501 Not Implemented");
    }

    fallback() external payable {
        revert("501 Not Implemented");
    }

    function withdraw(address to, uint256 amount, address token) external onlyOwner {
        IERC20 ERC20Token = IERC20(token);
        ERC20Token.safeTransfer(to, amount);
    }
}