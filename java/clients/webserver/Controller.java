package com.template.webserver;


import net.corda.core.contracts.*;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.NodeInfo;
import net.corda.core.transactions.SignedTransaction;

import java.util.*;

import com.template.flows.*
import com.template.states.Balance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/") // The paths for HTTP requests are relative to this base path.
public class Controller {
    private final CordaRPCOps proxy;
    private final static Logger logger = LoggerFactory.getLogger(Controller.class);

    public Controller(NodeRPCConnection rpc) {
        this.proxy = rpc.proxy;
    }

    @GetMapping(value = "/flows", produces = TEXT_PLAIN_VALUE)
    private String flows() {
        return proxy.registeredFlows().toString();
    }

    @GetMapping(value = "/balances",produces = APPLICATION_JSON_VALUE)
    public List<StateAndRef<Balance>> getBalances() {
        // Filter by state type: Balance.
        return proxy.vaultQuery(Balance.class).getStates();
    }

    /**
     *
     * @param request - needs a initialCurrency parameter
     * @return Custom Response
     * @throws IllegalArgumentException
     */
    @PostMapping (value = "create-balance" , produces =  TEXT_PLAIN_VALUE , headers =  "Content-Type=application/x-www-form-urlencoded" )
    public ResponseEntity<String> issueBalance(HttpServletRequest request) throws IllegalArgumentException {
        // Get currency value from request
        String currency = request.getParameter("initialCurrency");
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

    /**
     *
     * @param request - needs a balanceId and amount parameter in HTTP request.
     * @return Customized Response
     * @throws IllegalArgumentException
     */
    @PostMapping (value = "deposit" , produces =  TEXT_PLAIN_VALUE , headers =  "Content-Type=application/x-www-form-urlencoded" )
    public ResponseEntity<String> deposit(HttpServletRequest request) throws IllegalArgumentException {
        // Get balanceId and amount value from request
        UniqueIdentifier balanceId = UUID.fromString(request.getParameter("balanceId"));
        double amount = Double.parseDouble(request.getParameter("amount"));

        try {
            //start flow using proxy RPC connection
            SignedTransaction result = proxy.startFlow(Deposit.DepositInitiator.class, balanceId, amount).getReturnValue().get();

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Transaction id "+ result.getId() +" committed to ledger.\n "
                            + "BalanceId is " + result.getTx().getOutput(0).getLinearId() + "\n"
                            + "Balance is " + result.getTx().getOutput(0).getBalance());
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    /**
     *
     * @param request - needs a balanceId and amount parameter in HTTP request.
     * @return Customized Response
     * @throws IllegalArgumentException
     */
    @PostMapping (value = "withdraw" , produces =  TEXT_PLAIN_VALUE , headers =  "Content-Type=application/x-www-form-urlencoded" )
    public ResponseEntity<String> withdraw(HttpServletRequest request) throws IllegalArgumentException {
        // Get balanceId and amount value from request
        UniqueIdentifier balanceId = UUID.fromString(request.getParameter("balanceId"));
        double amount = Double.parseDouble(request.getParameter("amount"));

        try {
            //start flow using proxy RPC connection
            SignedTransaction result = proxy.startFlow(Withdraw.WithdrawInitiator.class, balanceId, amount).getReturnValue().get();

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Transaction id "+ result.getId() +" committed to ledger.\n "
                            + "BalanceId is " + result.getTx().getOutput(0).getLinearId() + "\n"
                            + "Balance is " + result.getTx().getOutput(0).getBalance());
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
}