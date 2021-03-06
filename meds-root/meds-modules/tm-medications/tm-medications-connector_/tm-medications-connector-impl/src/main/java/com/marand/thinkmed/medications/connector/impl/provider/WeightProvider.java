package com.marand.thinkmed.medications.connector.impl.provider;

import lombok.NonNull;

/**
 * @author Mitja Lapajne
 */
public interface WeightProvider
{
  Double getPatientWeight(@NonNull String patientsId);
}
