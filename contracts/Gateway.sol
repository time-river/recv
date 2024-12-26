// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.28;

import { Ownable } from '@openzeppelin/contracts/access/Ownable.sol';
import { Wallet } from './Wallet.sol';

contract Gateway is Ownable {
    mapping (address => bool) public accounts;

    event CreateWallet(address);

    constructor() Ownable() {}

    function createWallet(bytes32 salt) external onlyOwner {
        Wallet wallet = new Wallet{salt: salt}();

        emit CreateWallet(address(wallet));
    }
}