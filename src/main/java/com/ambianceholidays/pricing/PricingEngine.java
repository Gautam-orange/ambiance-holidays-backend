package com.ambianceholidays.pricing;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Central pricing calculation. All math in integer cents.
 * Formula (SOW §20.7.4):
 *   subtotal    = sum(lineTotal)
 *   markup      = subtotal * markupRate
 *   commission  = subtotal * commissionRate  (display/reporting only, not added to total)
 *   vat         = subtotal * vatRate         (VAT on subtotal only, NOT on subtotal+markup)
 *   total       = subtotal + markup + vat
 */
@Component
public class PricingEngine {

    public PricingResult calculate(int subtotalCents, BigDecimal markupRate,
            BigDecimal commissionRate, BigDecimal vatRate) {
        int markupCents     = percent(subtotalCents, markupRate);
        int commissionCents = percent(subtotalCents, commissionRate);
        int vatCents        = percent(subtotalCents, vatRate);  // VAT on subtotal only
        int totalCents      = subtotalCents + markupCents + vatCents;
        return new PricingResult(subtotalCents, markupCents, commissionCents, subtotalCents, vatCents, totalCents);
    }

    public int cancellationFee(int totalCents, long hoursUntilService) {
        if (hoursUntilService > 24) return 0;
        if (hoursUntilService > 12) return percent(totalCents, new BigDecimal("50"));
        if (hoursUntilService > 2)  return percent(totalCents, new BigDecimal("75"));
        return totalCents;
    }

    private static int percent(int cents, BigDecimal rate) {
        if (rate == null || rate.compareTo(BigDecimal.ZERO) == 0) return 0;
        return rate.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                   .multiply(BigDecimal.valueOf(cents))
                   .setScale(0, RoundingMode.HALF_UP)
                   .intValue();
    }
}
