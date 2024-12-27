// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.28;

import { Ownable } from '@openzeppelin/contracts/access/Ownable.sol';
import { Wallet } from './Wallet.sol';

contract Gateway is Ownable {
    mapping (address => bool) public wallets;

    event CreateWallet(address);

    constructor() Ownable(msg.sender) {}

    function createWallet(bytes32 salt) external onlyOwner {
        Wallet wallet = new Wallet{salt: salt}();
        wallets[address(wallet)] = true;
        emit CreateWallet(address(wallet));
    }
}