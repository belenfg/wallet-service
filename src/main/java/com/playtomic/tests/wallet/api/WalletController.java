package com.playtomic.tests.wallet.api;

import com.playtomic.tests.wallet.domain.*;
import com.playtomic.tests.wallet.usecase.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;

@RestController
public class WalletController {

    private final GetWalletUseCase getWalletUseCase;
    private final TopUpWalletUseCase topUpWalletUseCase;
    private Logger log = LoggerFactory.getLogger(WalletController.class);

    public WalletController(GetWalletUseCase getWalletUseCase, TopUpWalletUseCase topUpWalletUseCase) {
        this.getWalletUseCase = getWalletUseCase;
        this.topUpWalletUseCase = topUpWalletUseCase;
    }

    @RequestMapping("/")
    public void log() {
        log.info("Logging from /");
    }

    @GetMapping("/{id}")
    public ResponseEntity<WalletResponse> getWallet(@PathVariable Long id) {
        log.info("GET /{} called", id);
        try {
            Wallet wallet = getWalletUseCase.execute(id);
            log.info("Wallet {} retrieved with balance {}", id, wallet.getBalance());
            return ResponseEntity.ok(new WalletResponse(wallet.getBalance()));
        } catch (IllegalArgumentException e) {
            log.warn("Wallet {} not found", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/topup")
    public ResponseEntity<?> topUp(@RequestBody TopUpRequest request) {
        log.info("POST /topup called with walletId={} amount={}", request.getWalletId(), request.getAmount());
        try {
            Wallet wallet = topUpWalletUseCase.execute(request.getWalletId(), request.getAmount());
            log.info("Wallet {} topped up successfully. New balance {}", request.getWalletId(), wallet.getBalance());
            return ResponseEntity.ok(new WalletResponse(wallet.getBalance()));
        } catch (IllegalArgumentException e) {
            log.error("Error topping up wallet {}: {}", request.getWalletId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }
}
