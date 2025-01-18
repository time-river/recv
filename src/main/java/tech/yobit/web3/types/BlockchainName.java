package tech.yobit.web3.types;

public enum BlockchainName {
    ETHEREUM(1),
    BNB_SMART_CHAIN(56),
    SOLANA(101),
    POLYGON(137),
    POLYGON_AMOY(80_002),
    SEPOLIA(11_155_111);

    private final long mId;

    BlockchainName(long id) {
        mId = id;
    }

    // TODO: check int/long overflow
    public static BlockchainName from(long id) throws IllegalArgumentException {
        int index = (int) (id);

        return switch (index) {
            case 1 -> ETHEREUM;
            case 137 -> POLYGON;
            case 56 -> BNB_SMART_CHAIN;
            case 101 -> SOLANA;
            case 80_002 -> POLYGON_AMOY;
            case 11_155_111 -> SEPOLIA;
            default -> throw new IllegalArgumentException("Unknown blockchain id: " + id);
        };
    }

    public static boolean isEVMCompatible(long id) {
        return BlockchainName.from(id).isEVMCompatible();
    }

    public static boolean isSupportedEIP1559(long id) {
        return BlockchainName.from(id).isSupportedEIP1559();
    }

    public long getId() {
        return mId;
    }

    @Override
    public String toString() {
        return switch (this) {
            case ETHEREUM -> "Ethereum Mainnet";
            case POLYGON -> "Polygon Mainnet";
            case BNB_SMART_CHAIN -> "BNB Smart Chain";
            case SOLANA -> "Solana";
            case POLYGON_AMOY -> "Polygon Amoy";
            case SEPOLIA -> "Sepolia";
        };
    }

    public boolean isEVMCompatible() {
        return this == ETHEREUM || this == POLYGON || this == BNB_SMART_CHAIN
                || this == POLYGON_AMOY || this == SEPOLIA;
    }

    public boolean isSupportedEIP1559() {
        return this == ETHEREUM || this == POLYGON || this == SEPOLIA;
    }
}
