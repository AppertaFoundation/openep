package com.marand.thinkmed.medications.dto;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForMedicationsDto;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */

public class TherapyViewPatientDto extends DataTransferObject
{
  private PatientDataForMedicationsDto patientData;
  private RoundsIntervalDto roundsInterval;
  private AdministrationTimingDto administrationTiming;
  private List<String> customGroups = new ArrayList<>();
  private Double referenceWeight;
  private DateTime referenceWeightDate;
  private String lastLinkName;
  private boolean recentHospitalization;

  public PatientDataForMedicationsDto getPatientData()
  {
    return patientData;
  }

  public void setPatientData(final PatientDataForMedicationsDto patientData)
  {
    this.patientData = patientData;
  }

  public RoundsIntervalDto getRoundsInterval()
  {
    return roundsInterval;
  }

  public void setRoundsInterval(final RoundsIntervalDto roundsInterval)
  {
    this.roundsInterval = roundsInterval;
  }

  public AdministrationTimingDto getAdministrationTiming()
  {
    return administrationTiming;
  }

  public void setAdministrationTiming(final AdministrationTimingDto administrationTiming)
  {
    this.administrationTiming = administrationTiming;
  }

  public List<String> getCustomGroups()
  {
    return customGroups;
  }

  public void setCustomGroups(final List<String> customGroups)
  {
    this.customGroups = customGroups;
  }

  public Double getReferenceWeight()
  {
    return referenceWeight;
  }

  public void setReferenceWeight(final Double referenceWeight)
  {
    this.referenceWeight = referenceWeight;
  }

  public DateTime getReferenceWeightDate()
  {
    return referenceWeightDate;
  }

  public void setReferenceWeightDate(final DateTime referenceWeightDate)
  {
    this.referenceWeightDate = referenceWeightDate;
  }

  public String getLastLinkName()
  {
    return lastLinkName;
  }

  public void setLastLinkName(final String lastLinkName)
  {
    this.lastLinkName = lastLinkName;
  }

  public boolean isRecentHospitalization()
  {
    return recentHospitalization;
  }

  public void setRecentHospitalization(final boolean recentHospitalization)
  {
    this.recentHospitalization = recentHospitalization;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("patientData", patientData)
        .append("roundsInterval", roundsInterval)
        .append("administrationTiming", administrationTiming)
        .append("customGroups", customGroups)
        .append("referenceWeight", referenceWeight)
        .append("referenceWeightDate", referenceWeightDate)
        .append("lastLinkName", lastLinkName)
        .append("recentHospitalization", recentHospitalization)
    ;
  }
}
