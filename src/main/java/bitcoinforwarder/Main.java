package bitcoinforwarder;

public class Main {

    public static void main(String [] args)
    {
        System.out.println("\n\nCoinForwarder Service Starting...\n");

        CoinForwarder coinForwarder;

        if(args.length > 1) {
            coinForwarder = new CoinForwarder(args[1]);
        }
        else {
            coinForwarder = new CoinForwarder();
        }

        coinForwarder.downloadBlockChainAndWait();
        coinForwarder.addCoinsRecieveListener();

        String sendToAddress = coinForwarder.getCurrentRecieveAddress();
        System.out.println("Send coins to: " + sendToAddress);
        System.out.println("Once coins are recieved they will be forwarded to: " + coinForwarder.forwardingAddress);
        System.out.println("Waiting for coins to arrive. Press Ctrl-C to quit.");

        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException ignored) {}
    }
}
