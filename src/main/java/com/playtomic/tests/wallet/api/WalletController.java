package com.playtomic.tests.wallet.api;

import com.playtomic.tests.wallet.domain.Wallet;
import com.playtomic.tests.wallet.usecase.GetWalletUseCase;
import com.playtomic.tests.wallet.usecase.TopUpWalletUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
public class WalletController {

    private final GetWalletUseCase getWalletUseCase;
    private final TopUpWalletUseCase topUpWalletUseCase;
    private final Logger log = LoggerFactory.getLogger(WalletController.class);

    public WalletController(GetWalletUseCase getWalletUseCase,
                            TopUpWalletUseCase topUpWalletUseCase) {
        this.getWalletUseCase = getWalletUseCase;
        this.topUpWalletUseCase = topUpWalletUseCase;
    }

    @RequestMapping("/")
    public void log() {
        log.info("Logging from /");
    }

    @GetMapping("/{id}")
    public WalletResponse getWallet(@PathVariable Long id) {
        log.info("GET /{} called", id);
        Wallet wallet = getWalletUseCase.execute(id);
        log.info("Wallet {} retrieved with balance {}", id, wallet.getBalance());
        return new WalletResponse(wallet.getBalance());
    }

    @PostMapping("/topup")
    public WalletResponse topUp(@RequestBody TopUpRequest request) {
        log.info("POST /topup called with walletId={} amount={} card={}",
                request.getWalletId(),
                request.getAmount(),
                request.getCreditCardNumber());
        Wallet wallet = topUpWalletUseCase.execute(
                request.getWalletId(),
                request.getAmount(),
                request.getCreditCardNumber()
        );
        log.info("Wallet {} topped up successfully. New balance {}",
                request.getWalletId(),
                wallet.getBalance());
        return new WalletResponse(wallet.getBalance());
    }
}
