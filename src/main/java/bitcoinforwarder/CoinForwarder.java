package bitcoinforwarder;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import org.bitcoinj.core.*;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.utils.BriefLogFormatter;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;

import java.io.File;
import java.util.concurrent.Executor;

public class CoinForwarder {

    private static String defaultForwardAddress = "2MshwmThGb7PgnZYgSGL9cCNY9ZyEopd99q";

    private String filePrefix = "forwarding-service-testnet";
    private String feePerKb = "0.00001";
    private NetworkParameters params = TestNet3Params.get();
    public Address forwardingAddress;

    private WalletAppKit kit;

    public CoinForwarder() {
        this(defaultForwardAddress);
    }

    public CoinForwarder(String addressToForwardTo) {
        this(addressToForwardTo, true);
    }

    public CoinForwarder(String addressToForwardTo, boolean slientLogger)
    {
        if(slientLogger) {
            BriefLogFormatter.initWithSilentBitcoinJ();
        }
        else {
            BriefLogFormatter.init();
        }

        this.forwardingAddress = Address.fromBase58(params, addressToForwardTo);

        kit = new WalletAppKit(params, new File("."), filePrefix) {
            @Override
            protected void onSetupCompleted() {
                // This is called in a background thread after startAndWait is called
                if (wallet().getKeyChainGroupSize() < 1)
                    wallet().importKey(new ECKey());

                System.out.println("Wallet Setup Complete. Waiting for blockchain download..");
            }
        };
    }

    public void downloadBlockChainAndWait() {
        // Download the block chain and wait until it's done.
        kit.startAsync();
        kit.awaitRunning();

        System.out.println("Blockchain downloaded, ready to recieve coins");
    }

    public void addCoinsRecieveListener() {

        kit.wallet().addCoinsReceivedEventListener(new WalletCoinsReceivedEventListener() {
            @Override
            public void onCoinsReceived(Wallet w, Transaction tx, Coin prevBalance, Coin newBalance) {
                Coin value = tx.getValueSentToMe(w);
                System.out.println("Received tx for " + value.toFriendlyString() + ": " + tx);
                System.out.println("Transaction will be forwarded after it confirms.");
                forwardCoinsAfterConfirmedBlocks(tx, 1);
            }
        });
    }

    private void forwardCoinsAfterConfirmedBlocks(Transaction tx, int blocks) {
        Futures.addCallback(tx.getConfidence().getDepthFuture(blocks), new FutureCallback<TransactionConfidence>() {
            @Override
            public void onSuccess(TransactionConfidence result) {
                Coin value = tx.getValueSentToMe(kit.wallet());
                System.out.println("Forwarding " + value.toFriendlyString() + " BTC");

                final Coin amountToSend = value.subtract(Transaction.REFERENCE_DEFAULT_MIN_TX_FEE);
                SendRequest req = SendRequest.to(forwardingAddress, amountToSend);
                req.feePerKb = Coin.parseCoin(feePerKb);

                Wallet.SendResult sendResult = null;

                try {
                    sendResult = kit.wallet().sendCoins(kit.peerGroup(), req);
                } catch (InsufficientMoneyException e) {
                    e.printStackTrace();
                }

                Transaction createdTx = sendResult.tx;
                System.out.println("createdTx: " + createdTx);

                final Wallet.SendResult finalSendResult = sendResult;
                sendResult.broadcastComplete.addListener(new Runnable() {
                    @Override
                    public void run() {
                        // The wallet has changed now, it'll get auto saved shortly or when the app shuts down.
                        System.out.println("Sent coins onwards! Transaction hash is " + finalSendResult.tx.getHashAsString());
                    }
                }, new Executor() {
                    @Override
                    public void execute(Runnable command) {

                    }
                });
            }
            @Override
            public void onFailure(Throwable t) {}
        });
    }

    public String getCurrentRecieveAddress() {
        return "" + kit.wallet().currentReceiveAddress();
    }
}
