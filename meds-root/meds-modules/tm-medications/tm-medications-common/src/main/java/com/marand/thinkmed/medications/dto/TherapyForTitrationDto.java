package com.marand.thinkmed.medications.dto;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.connector.data.object.QuantityWithTimeDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
public class TherapyForTitrationDto extends DataTransferObject implements JsonSerializable
{
  private TherapyDto therapy;
  private String doseUnit;
  private List<QuantityWithTimeDto> administrations = new ArrayList<>();
  private Double infusionFormulaAtIntervalStart;

  public TherapyDto getTherapy()
  {
    return therapy;
  }

  public void setTherapy(final TherapyDto therapy)
  {
    this.therapy = therapy;
  }

  public String getDoseUnit()
  {
    return doseUnit;
  }

  public void setDoseUnit(final String doseUnit)
  {
    this.doseUnit = doseUnit;
  }

  public List<QuantityWithTimeDto> getAdministrations()
  {
    return administrations;
  }

  public void setAdministrations(final List<QuantityWithTimeDto> administrations)
  {
    this.administrations = administrations;
  }

  public Double getInfusionFormulaAtIntervalStart()
  {
    return infusionFormulaAtIntervalStart;
  }

  public void setInfusionFormulaAtIntervalStart(final Double infusionFormulaAtIntervalStart)
  {
    this.infusionFormulaAtIntervalStart = infusionFormulaAtIntervalStart;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("therapy", therapy)
        .append("doseUnit", doseUnit)
        .append("infusionFormulaAtIntervalStart", infusionFormulaAtIntervalStart)
        .append("administrations", administrations)
    ;
  }
}
