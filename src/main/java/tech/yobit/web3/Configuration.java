package tech.yobit.web3;

// TODO
public class Configuration {
    public String privateKey;
    public String defaultUrl;
    public Coin[] coins;
    public Blockchain[] blockchains;

    static public class Coin {
        public String name;
        public String fullName;
        public int decimal;
        public Blockchain[] blockchains;

        static public class Blockchain {
            public int id;
            public String contractAddress;
        }
    }

    static public class Blockchain {
        public String name;
        public int id;
        public String url;
        public String gatewayAddress;
    }

    public static Configuration parse() {
        return new Configuration();
    }
}
