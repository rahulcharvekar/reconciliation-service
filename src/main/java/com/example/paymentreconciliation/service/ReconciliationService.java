package com.example.paymentreconciliation.service;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import com.example.paymentreconciliation.utilities.logger.LoggerFactoryProvider;

@Service
public class ReconciliationService {

    private static final Logger log = LoggerFactoryProvider.getLogger(ReconciliationService.class);

    public String reconcilePayments() {
        log.info("Starting payment reconciliation process");
        String result = "Reconciliation completed";
        log.info("Finished payment reconciliation process");
        return result;
    }
}
