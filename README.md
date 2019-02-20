## Bitcoin Forwarding Service

This is an implementation of bitcoinj's https://bitcoinj.github.io/ library to use a bitcoin forwarding service. This uses the wallet kit similar in their example but wraps it into a BitcoinForwarder object for easier readability and usability. THIS RUNS ON THE TESTNET ONLY. I purposely didn't put an option to run on mainnet because this is just for learning purposes.

## Motivation

This is just a way for me to dip my toes into using the bitcoinj library. This may evolve into a online service.

## To Use

Run Main.java with one command line argument: the forward address. If you do not provide an address it will use a default forward address.
