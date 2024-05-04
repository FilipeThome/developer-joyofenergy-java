package uk.tw.energy.domain;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.List;

/**
 * @param unitRate unit price per kWh
 */
public record PricePlan(
    String planName,
    String energySupplier,
    BigDecimal unitRate,
    List<PeakTimeMultiplier> peakTimeMultipliers) {

  public static class PeakTimeMultiplier {

    DayOfWeek dayOfWeek;
    BigDecimal multiplier;

    public PeakTimeMultiplier(DayOfWeek dayOfWeek, BigDecimal multiplier) {
      this.dayOfWeek = dayOfWeek;
      this.multiplier = multiplier;
    }

    public DayOfWeek getDayOfWeek() {
      return dayOfWeek;
    }

    public BigDecimal getMultiplier() {
      return multiplier;
    }
  }
}
