package com.marand.thinkmed.medications.api.internal.dto.dose;

import com.marand.maf.core.data.object.HourMinuteDto;
import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
public class TimedComplexDoseElementDto extends DataTransferObject implements JsonSerializable
{
  private ComplexDoseElementDto doseElement;
  private HourMinuteDto doseTime;

  private String intervalDisplay;
  private String speedDisplay;
  private String speedFormulaDisplay;

  public ComplexDoseElementDto getDoseElement()
  {
    return doseElement;
  }

  public void setDoseElement(final ComplexDoseElementDto doseElement)
  {
    this.doseElement = doseElement;
  }

  public HourMinuteDto getDoseTime()
  {
    return doseTime;
  }

  public void setDoseTime(final HourMinuteDto doseTime)
  {
    this.doseTime = doseTime;
  }

  public String getIntervalDisplay()
  {
    return intervalDisplay;
  }

  public void setIntervalDisplay(final String intervalDisplay)
  {
    this.intervalDisplay = intervalDisplay;
  }

  public String getSpeedDisplay()
  {
    return speedDisplay;
  }

  public void setSpeedDisplay(final String speedDisplay)
  {
    this.speedDisplay = speedDisplay;
  }

  public String getSpeedFormulaDisplay()
  {
    return speedFormulaDisplay;
  }

  public void setSpeedFormulaDisplay(final String speedFormulaDisplay)
  {
    this.speedFormulaDisplay = speedFormulaDisplay;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("doseElement", doseElement)
        .append("doseTime", doseTime)
        .append("intervalDisplay", intervalDisplay)
        .append("speedDisplay", speedDisplay)
        .append("speedFormulaDisplay", speedFormulaDisplay)
    ;
  }
}
