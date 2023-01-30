package com.template;

import net.corda.client.rpc.CordaRPCClient;
import net.corda.client.rpc.CordaRPCConnection;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.NodeInfo;
import net.corda.core.utilities.NetworkHostAndPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static net.corda.core.utilities.NetworkHostAndPort.parse;

/**
 * Connects to a Corda node via RPC and performs RPC operations on the node.
 *
 * The RPC connection is configured using command line arguments.
 */
public class Client {
    private static final String RPC_USERNAME = "user1";
    private static final String RPC_PASSWORD = "test";

    public static void main(String[] args) {
        if (args.length != 2) throw new IllegalArgumentException("Usage: RpcClient <node address> <counterpartyName>");
        System.out.println(args[0]);
        System.out.println(args[1]);

        final String rpcAddressString = args[0];
        final String currency = args[1];

        final RPCClient rpcClient = new RPCClient(rpcAddressString);
        rpcClient.issue_balance(currency);
        rpcClient.closeRpcConnection();

    }
    static class RPCClient {
        public static Logger logger = LoggerFactory.getLogger(RPCClient.class);

        private CordaRPCConnection rpcConnection;

        /** Sets a [CordaRPCConnection] to the node listening on [rpcPortString]. */
        protected RPCClient(String rpcAddressString) {
            final NetworkHostAndPort nodeAddress = NetworkHostAndPort.parse(rpcAddressString);
            final CordaRPCClient client = new CordaRPCClient(nodeAddress);
            rpcConnection = client.start(RPC_USERNAME, RPC_PASSWORD);
        }

        protected void closeRpcConnection() {
            rpcConnection.close();
        }

        private void issue_balance(String currency) {
            try {
                //start flow using proxy RPC connection
                SignedTransaction result = proxy.startFlow(IssueBalance.IssueBalanceInitiator.class, currency).getReturnValue().get();

                return ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body("Transaction id "+ result.getId() +" committed to ledger.\n "
                                + "BalanceId is " + result.getTx().getOutput(0).getLinearId() + "\n"
                                + "Balance currency is " + result.getTx().getOutput(0).getCurrency());
            } catch (Exception e) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(e.getMessage());
            }
        }
    }
}