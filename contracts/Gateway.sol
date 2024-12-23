// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.28;

import { IERC20 } from '@openzeppelin/contracts/token/ERC20/IERC20.sol';
//import "hardhat/console.sol";

contract Account {
    address payable public _receiver;

    constructor(address payable receiver) {
        _receiver = receiver;
    }

    function ethBalance() public view returns (uint256) {
        return address(this).balance;
    }

    function usdtBalance() public view returns (uint256) {
        return IERC20(address(this)).balanceOf(address(this));
    }

    function flushEth() external payable {
        _receiver.transfer(ethBalance());
    }

    function flushUsdt() external {
        IERC20 token = IERC20(address(this));
        token.transfer(_receiver, usdtBalance());
    }
}

contract Gateway {
    address public owner;
    mapping (address => bool) public accounts;

    event Create(address, address);
    event Log(bytes32);

    constructor() {
        owner = msg.sender;
    }

    function initCodeHash(address to) public returns (bytes memory) {
        bytes memory bytecode = abi.encodePacked(type(Account).creationCode, abi.encode(to));
        emit Log(to);
        return bytecode;
    }

    function create(address payable to, bytes32 salt) external returns(address) {
        require(msg.sender == owner, "403");

        initCodeHash(to);

        Account account = new Account{salt: salt}(to);
        accounts[address(account)] = true;
        //emit Create(address(account), address(this));

        return address(account);
    }
}