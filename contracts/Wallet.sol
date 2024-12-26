// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.28;

import { Ownable } from '@openzeppelin/contracts/access/Ownable.sol';
import { IERC20 } from '@openzeppelin/contracts/token/ERC20/IERC20.sol';

Contract Wallet is Ownable {

    construct() Ownable() {}

    receive() external payable {
        revert("501 Not Implemented");
    }

    fallback() external payable {
        revert("501 Not Implemented");
    }

    function withdraw(address payable to, uint256 amount, address token) external onlyOwner {
        address payable ERC20Token = IERC20(token);

        ERC20Token.transfer(to, amount);
    }
}