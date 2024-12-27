// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.28;

import { Ownable } from '@openzeppelin/contracts/access/Ownable.sol';
import { Wallet } from './Wallet.sol';

contract Gateway is Ownable {
    event CreateWallet(address);

    constructor() Ownable() {}

    function createWallet(address to, bytes32 salt) external onlyOwner {
        Wallet wallet = new Wallet{salt: salt}(to);

        emit CreateWallet(address(wallet));
    }
}