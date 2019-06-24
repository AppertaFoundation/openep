package com.marand.thinkmed.medications.connector.data.object.ehr;

import java.util.Arrays;
import java.util.Objects;

import com.marand.thinkmed.medications.connector.data.object.AllergiesStatus;
import lombok.NonNull;
import org.openehr.jaxb.rm.DvCodedText;

/**
 * @author Nejc Korasa
 */
public enum EhrAllergiesStatus
{
  PRESENT("at0.11"),
  NO_KNOWN_ALLERGY("at0.9"),
  NO_INFORMATION("at0.10");

  private final String atCode;

  EhrAllergiesStatus(final String atCode)
  {
    this.atCode = atCode;
  }

  public String getAtCode()
  {
    return atCode;
  }

  public static EhrAllergiesStatus get(final @NonNull DvCodedText text)
  {
    final String atCode = text.getDefiningCode().getCodeString();

    return Arrays.stream(values())
        .filter(status -> status.getAtCode().equals(atCode))
        .filter(Objects::nonNull)
        .findFirst()
        .orElseThrow(() -> new UnsupportedOperationException("Can not map to EhrAllergiesStatus for atCode: " + atCode));
  }

  public AllergiesStatus map()
  {
    return AllergiesStatus.valueOf(name());
  }
}
