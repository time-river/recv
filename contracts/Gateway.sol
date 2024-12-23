// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.28;

import { IERC20 } from '@openzeppelin/contracts/token/ERC20/IERC20.sol';
//import "hardhat/console.sol";

contract Account {
    address payable public _receiver;

    event Deposit(address, uint256);
    event Receved(uint256);

    constructor(address payable receiver) {
        _receiver = receiver;
        emit Receved(address(this).balance);
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

    receive() external payable {
        emit Deposit(msg.sender, msg.value);
    }

    fallback() external payable {
        emit Deposit(msg.sender, msg.value);
    }
}

contract Gateway {
    address public owner;
    mapping (address => bool) public accounts;

    event Create(address);
    event LogBytes(bytes);
    event Logbytes32(bytes32);

    constructor() {
        owner = msg.sender;
    }

    function initCodeHash(address to) public pure returns (bytes32) {
        bytes memory bytecode = abi.encodePacked(type(Account).creationCode, abi.encode(to));
//        emit Log(keccak256(bytecode));
        return keccak256(bytecode);
    }

    function getAddress(bytes32 salt, bytes32 codeHash) public {
        bytes memory byteCode = abi.encodePacked(
            bytes1(0xff),
            address(this),
            salt,
            codeHash
        );

        emit LogBytes(byteCode);
        //emit Logbytes32(keccak256(byteCode));
    }

    function create(address payable to, bytes32 salt) external {
        require(msg.sender == owner, "403");

        //bytes32 hash = initCodeHash(to);
        //emit Log(hash);

        //getAddress(salt, hash);

        Account account = new Account{salt: salt}(to);
        accounts[address(account)] = true;

        emit Create(address(account));
    }
}