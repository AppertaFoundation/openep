package com.marand.thinkmed.medications.dto;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.dto.change.TherapyChangeDto;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Igor Horvat
 * @author Mitja Lapajne
 */

public class TherapyActionHistoryDto extends DataTransferObject implements JsonSerializable
{
  private DateTime actionPerformedTime;
  private DateTime actionTakesEffectTime;
  private String performer;
  private TherapyActionHistoryType therapyActionHistoryType;
  private TherapyChangeReasonDto changeReason;
  private List<TherapyChangeDto<?, ?>> changes = new ArrayList<>();

  public DateTime getActionPerformedTime()
  {
    return actionPerformedTime;
  }

  public void setActionPerformedTime(final DateTime actionPerformedTime)
  {
    this.actionPerformedTime = actionPerformedTime;
  }

  public DateTime getActionTakesEffectTime()
  {
    return actionTakesEffectTime;
  }

  public void setActionTakesEffectTime(final DateTime actionTakesEffectTime)
  {
    this.actionTakesEffectTime = actionTakesEffectTime;
  }

  public String getPerformer()
  {
    return performer;
  }

  public void setPerformer(final String performer)
  {
    this.performer = performer;
  }

  public TherapyActionHistoryType getTherapyActionHistoryType()
  {
    return therapyActionHistoryType;
  }

  public void setTherapyActionHistoryType(final TherapyActionHistoryType therapyActionHistoryType)
  {
    this.therapyActionHistoryType = therapyActionHistoryType;
  }

  public TherapyChangeReasonDto getChangeReason()
  {
    return changeReason;
  }

  public void setChangeReason(final TherapyChangeReasonDto changeReason)
  {
    this.changeReason = changeReason;
  }

  public List<TherapyChangeDto<?, ?>> getChanges()
  {
    return changes;
  }

  public void setChanges(final List<TherapyChangeDto<?, ?>> changes)
  {
    this.changes = changes;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("actionPerformedTime", actionPerformedTime)
        .append("actionTakesEffectTime", actionTakesEffectTime)
        .append("editor", performer)
        .append("therapyActionHistoryType", therapyActionHistoryType)
        .append("changeReason", changeReason)
        .append("changes", changes);
  }
}