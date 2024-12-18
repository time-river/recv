// SPDX-License-Identifier: GPL-3.0

pragma solidity >=0.8.2 <0.9.0;

import "./ITRC20.sol";

contract Account {
    address public owner;
    address payable public receiver;

    event FlushEvent(address, uint256);

    constructor(address payable _receiver) {
        owner = msg.sender;
        receiver = _receiver;
    }

    function flush() public {
        require(msg.sender == owner, "403");

        address current = address(this);
        uint256 balance = ITRC20(current).balanceOf(current);
        if (balance == 0) {
            return;
        }

        ITRC20(current).transfer(receiver, balance);
        emit FlushEvent(receiver, balance);
    }
}

contract Gateway {
    address public owner;
    mapping(address => bool) public accounts;

    event CreateEvent(address);

    constructor() {
        owner = msg.sender;
    }

    function create(address payable _to, bytes32 _salt) public {
        require(msg.sender == owner, "403");

        Account account = new Account{salt: _salt}(_to);
        accounts[address(account)] = true;

        emit CreateEvent(address(account));
    }
}
