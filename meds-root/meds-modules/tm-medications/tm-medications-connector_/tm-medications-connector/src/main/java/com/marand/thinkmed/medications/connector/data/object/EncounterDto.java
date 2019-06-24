package com.marand.thinkmed.medications.connector.data.object;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.externals.data.object.ExternalIdentityDto;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */

public class EncounterDto extends ExternalIdentityDto implements JsonSerializable
{
  private String patientId;
  private DateTime start;
  private DateTime end;
  private EncounterType type;
  private EncounterStatus status;
  private NamedExternalDto ward;
  private String location;
  private String doctor;

  public EncounterDto(final String id)
  {
    super(id);
  }

  public void setPatientId(final String patientId)
  {
    this.patientId = patientId;
  }

  public DateTime getStart()
  {
    return start;
  }

  public void setStart(final DateTime start)
  {
    this.start = start;
  }

  public DateTime getEnd()
  {
    return end;
  }

  public void setEnd(final DateTime end)
  {
    this.end = end;
  }

  public EncounterType getType()
  {
    return type;
  }

  public void setType(final EncounterType type)
  {
    this.type = type;
  }

  public EncounterStatus getStatus()
  {
    return status;
  }

  public void setStatus(final EncounterStatus status)
  {
    this.status = status;
  }

  public NamedExternalDto getWard()
  {
    return ward;
  }

  public void setWard(final NamedExternalDto ward)
  {
    this.ward = ward;
  }

  public String getLocation()
  {
    return location;
  }

  public void setLocation(final String location)
  {
    this.location = location;
  }

  public String getDoctor()
  {
    return doctor;
  }

  public void setDoctor(final String doctor)
  {
    this.doctor = doctor;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("patientId", patientId)
        .append("start", start)
        .append("end", end)
        .append("type", type)
        .append("status", status)
        .append("ward", ward)
        .append("location", location)
        .append("doctor", doctor);
  }
}
