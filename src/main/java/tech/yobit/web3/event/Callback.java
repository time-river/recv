package tech.yobit.web3.event;

public interface Callback {
    void resolve(Object[] args);

    void reject(Object[] args);
}
