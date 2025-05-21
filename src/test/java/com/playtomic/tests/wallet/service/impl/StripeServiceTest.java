package com.playtomic.tests.wallet.service.impl;

import com.playtomic.tests.wallet.service.StripeAmountTooSmallException;
import com.playtomic.tests.wallet.service.StripeRestTemplateResponseErrorHandler;
import com.playtomic.tests.wallet.service.StripeService;
import com.playtomic.tests.wallet.service.StripeServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

public class StripeServiceTest {

    private StripeService stripeService;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        // 1) Creating RestTemplate for tests
        RestTemplate testTemplate = new RestTemplateBuilder()
                .errorHandler(new StripeRestTemplateResponseErrorHandler())
                .build();

        // 2) Creating mockServer from RestTemplate
        this.mockServer = MockRestServiceServer.createServer(testTemplate);

        // 3) Instantiating original StripeService
        URI chargeUri = URI.create("http://how-would-you-test-me.localhost/charge");
        URI refundUri = URI.create("http://how-would-you-test-me.localhost/refund");
        this.stripeService = new StripeService(chargeUri, refundUri, new RestTemplateBuilder());

        // 4) Injecting test RestTemplate in stripeService
        ReflectionTestUtils.setField(stripeService, "restTemplate", testTemplate);
    }

    @Test
    void test_exception() {
        assertThrows(StripeAmountTooSmallException.class,
                () -> stripeService.charge("4242 4242 4242 4242", new BigDecimal("5")));
    }

    @Test
    void test_ok() throws StripeServiceException {
        mockServer.expect(requestTo("http://how-would-you-test-me.localhost/charge"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"id\":\"pay_123\",\"amount\":15}",
                        MediaType.APPLICATION_JSON));

        stripeService.charge("4242 4242 4242 4242", new BigDecimal("15"));

        mockServer.verify();
    }
}
