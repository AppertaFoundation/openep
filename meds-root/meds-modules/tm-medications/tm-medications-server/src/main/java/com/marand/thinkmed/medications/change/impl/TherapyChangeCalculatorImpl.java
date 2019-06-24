package com.marand.thinkmed.medications.change.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.NonNull;

import com.google.common.base.Preconditions;
import com.marand.maf.core.JsonUtil;
import com.marand.maf.core.Opt;
import com.marand.maf.core.data.object.HourMinuteDto;
import com.marand.maf.core.time.DateTimeFormatters;
import com.marand.thinkmed.medications.business.impl.TherapyDisplayProvider;
import com.marand.thinkmed.medications.change.TherapyChangeCalculator;
import com.marand.thinkmed.medications.api.internal.dto.ComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.ConstantComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.ConstantTherapy;
import com.marand.thinkmed.medications.api.internal.dto.InfusionIngredientDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.api.internal.dto.OxygenTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.SimpleTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.VariableComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.VariableSimpleTherapyDto;
import com.marand.thinkmed.medications.dto.change.DoseToVariableDoseTherapyChangeDto;
import com.marand.thinkmed.medications.dto.change.RateToVariableRateTherapyChangeDto;
import com.marand.thinkmed.medications.dto.change.StringTherapyChangeDto;
import com.marand.thinkmed.medications.dto.change.StringsTherapyChangeDto;
import com.marand.thinkmed.medications.dto.change.TherapyChangeDto;
import com.marand.thinkmed.medications.dto.change.TherapyChangeType;
import com.marand.thinkmed.medications.dto.change.VariableDoseTherapyChangeDto;
import com.marand.thinkmed.medications.dto.change.VariableDoseToDoseTherapyChangeDto;
import com.marand.thinkmed.medications.dto.change.VariableRateTherapyChangeDto;
import com.marand.thinkmed.medications.dto.change.VariableRateToRateTherapyChangeDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.TimedComplexDoseElementDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.TimedSimpleDoseElementDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Mitja Lapajne
 */
@Component
public class TherapyChangeCalculatorImpl implements TherapyChangeCalculator
{
  private TherapyDisplayProvider therapyDisplayProvider;

  @Autowired
  public void setTherapyDisplayProvider(final TherapyDisplayProvider therapyDisplayProvider)
  {
    this.therapyDisplayProvider = therapyDisplayProvider;
  }

  @Override
  public List<TherapyChangeDto<?, ?>> calculateTherapyChanges(
      final @NonNull TherapyDto therapy,
      final @NonNull TherapyDto changedTherapy,
      final boolean includeTherapyStartChange,
      final @NonNull Locale locale)
  {
    final List<TherapyChangeDto<?, ?>> changes = new ArrayList<>();

    if (therapy instanceof SimpleTherapyDto)
    {
      changes.addAll(getSimpleTherapyChanges(therapy, changedTherapy));
    }
    else if (therapy instanceof ComplexTherapyDto)
    {
      changes.addAll(getComplexTherapyChanges((ComplexTherapyDto)therapy, changedTherapy));
    }
    else if (therapy instanceof OxygenTherapyDto)
    {
      changes.addAll(getOxygenTherapyChanges(therapy, changedTherapy));
    }
    else
    {
      throw new UnsupportedOperationException("Therapy type " + therapy.getMedicationOrderFormType() + " not supported");
    }

    getDosingFrequencyChange(therapy, changedTherapy).forEach(changes::add);

    getStringTherapyChange(
        Opt.resolve(() -> therapy.getReleaseDetails().getDisplay()),
        Opt.resolve(() -> changedTherapy.getReleaseDetails().getDisplay()),
        TherapyChangeType.RELEASE_DETAILS)
        .forEach(changes::add);

    getStringTherapyChange(
        Opt.of(therapy.getComment()),
        Opt.of(changedTherapy.getComment()),
        TherapyChangeType.COMMENT)
        .forEach(changes::add);

    getStringTherapyChange(
        Opt.resolve(() -> therapy.getClinicalIndication().getName()),
        Opt.resolve(() -> changedTherapy.getClinicalIndication().getName()),
        TherapyChangeType.INDICATION)
        .forEach(changes::add);

    getStringsTherapyChange(
        therapy.getRoutes().stream().map(MedicationRouteDto::getName).collect(Collectors.toList()),
        changedTherapy.getRoutes().stream().map(MedicationRouteDto::getName).collect(Collectors.toList()),
        TherapyChangeType.ROUTE)
        .forEach(changes::add);

    getStringTherapyChange(
        Opt.of(therapy.getApplicationPreconditionDisplay()),
        Opt.of(changedTherapy.getApplicationPreconditionDisplay()),
        TherapyChangeType.ADDITIONAL_CONDITIONS)
        .forEach(changes::add);

    getStringTherapyChange(
        Opt.of(therapy.getWhenNeededDisplay()),
        Opt.of(changedTherapy.getWhenNeededDisplay()),
        TherapyChangeType.WHEN_NEEDED)
        .forEach(changes::add);

    getStringTherapyChange(
        Opt.resolve(() -> therapy.getMaxDailyFrequency().toString()),
        Opt.resolve(() -> changedTherapy.getMaxDailyFrequency().toString()),
        TherapyChangeType.MAX_DOSES)
        .forEach(changes::add);

    getStringTherapyChange(
        Opt.of(therapy.getStartCriterionDisplay()),
        Opt.of(changedTherapy.getStartCriterionDisplay()),
        TherapyChangeType.DOCTOR_ORDERS)
        .forEach(changes::add);

    if (includeTherapyStartChange)
    {
      getStringTherapyChange(
          Opt.of(therapy.getStart()).map(e -> DateTimeFormatters.shortDateTime(locale).print(e)),
          Opt.of(changedTherapy.getStart()).map(e -> DateTimeFormatters.shortDateTime(locale).print(e)),
          TherapyChangeType.START)
          .forEach(changes::add);
    }

    getStringTherapyChange(
        Opt.of(therapy.getEnd()).map(e -> DateTimeFormatters.shortDateTime(locale).print(e)),
        Opt.of(changedTherapy.getEnd()).map(e -> DateTimeFormatters.shortDateTime(locale).print(e)),
        TherapyChangeType.END)
        .forEach(changes::add);

    return changes;
  }

  @Override
  public boolean hasTherapyChanged(
      final @NonNull TherapyChangeType.TherapyChangeGroup group,
      final @NonNull TherapyDto therapy,
      final @NonNull TherapyDto changedTherapy,
      final boolean includeTherapyStartChange,
      final @NonNull Locale locale)
  {
    if (therapy.getFormattedTherapyDisplay() == null)
    {
      therapyDisplayProvider.fillDisplayValues(therapy, true, locale);
    }
    if (changedTherapy.getFormattedTherapyDisplay() == null)
    {
      therapyDisplayProvider.fillDisplayValues(changedTherapy, true, locale);
    }

    return calculateTherapyChanges(therapy, changedTherapy, includeTherapyStartChange, locale)
        .stream()
        .anyMatch(c -> group.getChanges().contains(c.getType()));
  }

  private Opt<StringTherapyChangeDto> getDosingFrequencyChange(final TherapyDto therapy, final TherapyDto changedTherapy)
  {
    final boolean dosingFrequencyChange =
        !Objects.equals(therapy.getFrequencyDisplay(), changedTherapy.getFrequencyDisplay());
    final boolean dosingDaysFrequencyChange =
        !Objects.equals(therapy.getDaysFrequencyDisplay(), changedTherapy.getDaysFrequencyDisplay());
    final boolean daysOfWeekFrequencyChange =
        !Objects.equals(therapy.getDaysOfWeekDisplay(), changedTherapy.getDaysOfWeekDisplay());

    if (dosingFrequencyChange || dosingDaysFrequencyChange || daysOfWeekFrequencyChange)
    {
      final StringTherapyChangeDto change = new StringTherapyChangeDto(TherapyChangeType.DOSE_INTERVAL);
      String oldValue = therapy.getFrequencyDisplay();
      if (therapy.getDaysFrequencyDisplay() != null)
      {
        oldValue += " - " + therapy.getDaysFrequencyDisplay();
      }
      if (therapy.getDaysOfWeekDisplay() != null)
      {
        oldValue += " - " + therapy.getDaysOfWeekDisplay();
      }
      change.setOldValue(oldValue);

      String newValue = changedTherapy.getFrequencyDisplay();
      if (changedTherapy.getDaysFrequencyDisplay() != null)
      {
        newValue += " - " + changedTherapy.getDaysFrequencyDisplay();
      }
      if (changedTherapy.getDaysOfWeekDisplay() != null)
      {
        newValue += " - " + changedTherapy.getDaysOfWeekDisplay();
      }
      change.setNewValue(newValue);
      return Opt.of(change);
    }
    return Opt.none();
  }

  private List<TherapyChangeDto<?, ?>> getVariableComplexTherapyChanges(
      final VariableComplexTherapyDto therapy,
      final TherapyDto changedTherapy)
  {
    final List<TherapyChangeDto<?, ?>> changes = new ArrayList<>();

    if (changedTherapy instanceof VariableComplexTherapyDto)
    {
      final List<TimedComplexDoseElementDto> therapyDoseElements = therapy.getTimedDoseElements();
      final List<TimedComplexDoseElementDto> changedTherapyDoseElements =
          ((VariableComplexTherapyDto)changedTherapy).getTimedDoseElements();

      if (!JsonUtil.toJson(therapyDoseElements).equals(JsonUtil.toJson(changedTherapyDoseElements)))
      {
        final VariableRateTherapyChangeDto change = new VariableRateTherapyChangeDto();
        change.setOldValue(therapy.getTimedDoseElements());
        change.setNewValue(((VariableComplexTherapyDto)changedTherapy).getTimedDoseElements());
        changes.add(change);
      }
    }
    else if (changedTherapy instanceof ConstantComplexTherapyDto)
    {
      final VariableRateToRateTherapyChangeDto change = new VariableRateToRateTherapyChangeDto();
      change.setOldValue(therapy.getTimedDoseElements());
      change.setNewValue(((ConstantComplexTherapyDto)changedTherapy).getSpeedDisplay());
      changes.add(change);
    }
    return changes;
  }

  private List<TherapyChangeDto<?, ?>> getComplexTherapyChanges(
      final ComplexTherapyDto therapy,
      final TherapyDto changedTherapy)
  {
    final List<TherapyChangeDto<?, ?>> changes = new ArrayList<>();

    if (therapy instanceof ConstantComplexTherapyDto)
    {
      changes.addAll(getConstantComplexTherapyChanges((ConstantComplexTherapyDto)therapy, changedTherapy));
    }
    else if (therapy instanceof VariableComplexTherapyDto)
    {
      changes.addAll(getVariableComplexTherapyChanges((VariableComplexTherapyDto)therapy, changedTherapy));
    }

    if (changedTherapy instanceof ComplexTherapyDto)
    {
      final ComplexTherapyDto changedComplexTherapy = (ComplexTherapyDto)changedTherapy;

      changes.addAll(getMixtureChanges(therapy, changedComplexTherapy));
      getStringTherapyChange(
          Opt.of(therapy.getAdditionalInstruction()),
          Opt.of(changedComplexTherapy.getAdditionalInstruction()),
          TherapyChangeType.ADDITIONAL_INSTRUCTION)
          .forEach(changes::add);
    }

    return changes;
  }

  private List<TherapyChangeDto<?, ?>> getConstantComplexTherapyChanges(
      final ConstantComplexTherapyDto therapy,
      final TherapyDto changedTherapy)
  {
    final List<TherapyChangeDto<?, ?>> changes = new ArrayList<>();

    if (changedTherapy instanceof ConstantComplexTherapyDto)
    {
      final ConstantComplexTherapyDto changedComplexTherapy = (ConstantComplexTherapyDto)changedTherapy;

      getStringTherapyChange(
          Opt.of(therapy.getSpeedDisplay()),
          Opt.of(changedComplexTherapy.getSpeedDisplay()),
          TherapyChangeType.RATE)
          .forEach(changes::add);

      getStringTherapyChange(
          Opt.of(therapy.getDurationDisplay()),
          Opt.of(changedComplexTherapy.getDurationDisplay()),
          TherapyChangeType.INFUSION_DURATION)
          .forEach(changes::add);

      getDoseTimeChange(therapy, changedComplexTherapy).map(changes::add);
    }
    else if (changedTherapy instanceof VariableComplexTherapyDto)
    {
      final RateToVariableRateTherapyChangeDto change = new RateToVariableRateTherapyChangeDto();
      change.setOldValue(therapy.getSpeedDisplay());
      change.setNewValue(((VariableComplexTherapyDto)changedTherapy).getTimedDoseElements());
      changes.add(change);
    }
    else
    {
      throw new IllegalArgumentException("Change not supported");
    }
    return changes;
  }

  private List<TherapyChangeDto<?, ?>> getMixtureChanges(
      final ComplexTherapyDto therapy,
      final ComplexTherapyDto changedTherapy)
  {
    final List<TherapyChangeDto<?, ?>> changes = new ArrayList<>();

    int index = 0;
    for (final InfusionIngredientDto ingredient : therapy.getIngredientsList())
    {
      final Opt<String> changedTherapyMedication =
          changedTherapy.getIngredientsList().size() > index ?
          Opt.of(changedTherapy.getIngredientsList().get(index).getMedication().getDisplayName()) :
          Opt.none();

      getStringTherapyChange(
          Opt.of(ingredient.getMedication().getDisplayName()),
          changedTherapyMedication,
          TherapyChangeType.MEDICATION)
          .forEach(changes::add);

      final Opt<String> changedTherapyDose =
          changedTherapy.getIngredientsList().size() > index ?
          Opt.of(changedTherapy.getIngredientsList().get(index).getQuantityDisplay()) :
          Opt.none();

      getStringTherapyChange(
          Opt.of(ingredient.getQuantityDisplay()),
          changedTherapyDose,
          TherapyChangeType.DOSE)
          .forEach(changes::add);
      index++;
    }

    if (therapy.getVolumeSumDisplay() != null)
    {
      getStringTherapyChange(
          Opt.of(therapy.getVolumeSumDisplay()),
          Opt.of(changedTherapy.getVolumeSumDisplay()),
          TherapyChangeType.VOLUME_SUM)
          .forEach(changes::add);
    }

    return changes;
  }

  private List<TherapyChangeDto<?, ?>> getSimpleTherapyChanges(final TherapyDto therapy, final TherapyDto changedTherapy)
  {
    Preconditions.checkArgument(
        changedTherapy instanceof SimpleTherapyDto,
        "Therapy" + therapy.getCompositionUid() + "not saved correctly");

    final SimpleTherapyDto simpleTherapy = (SimpleTherapyDto)therapy;
    final SimpleTherapyDto changedSimpleTherapy = (SimpleTherapyDto)changedTherapy;

    final List<TherapyChangeDto<?, ?>> changes = new ArrayList<>();

    getStringTherapyChange(
        Opt.of(simpleTherapy.getMedication().getDisplayName()),
        Opt.of(changedSimpleTherapy.getMedication().getDisplayName()),
        TherapyChangeType.MEDICATION)
        .forEach(changes::add);

    if (therapy instanceof ConstantSimpleTherapyDto)
    {
      if (changedTherapy instanceof ConstantSimpleTherapyDto)
      {
        getStringTherapyChange(
            Opt.of(simpleTherapy.getQuantityDisplay()),
            Opt.of(changedSimpleTherapy.getQuantityDisplay()),
            TherapyChangeType.DOSE)
            .forEach(changes::add);

        getDoseTimeChange(
            (ConstantSimpleTherapyDto)simpleTherapy,
            (ConstantSimpleTherapyDto)changedSimpleTherapy)
            .map(changes::add);
      }
      else if (changedTherapy instanceof VariableSimpleTherapyDto)
      {
        final DoseToVariableDoseTherapyChangeDto change = new DoseToVariableDoseTherapyChangeDto();
        change.setOldValue(simpleTherapy.getQuantityDisplay());
        change.setNewValue(((VariableSimpleTherapyDto)changedTherapy).getTimedDoseElements());
        changes.add(change);
      }
      else
      {
        throw new IllegalArgumentException("Change not supported");
      }
    }
    else if (therapy instanceof VariableSimpleTherapyDto)
    {
      if (changedTherapy instanceof ConstantSimpleTherapyDto)
      {
        final VariableDoseToDoseTherapyChangeDto change = new VariableDoseToDoseTherapyChangeDto();
        change.setOldValue(((VariableSimpleTherapyDto)therapy).getTimedDoseElements());
        change.setNewValue(((ConstantSimpleTherapyDto)changedTherapy).getQuantityDisplay());
        changes.add(change);
      }
      else if (changedTherapy instanceof VariableSimpleTherapyDto)
      {
        final List<TimedSimpleDoseElementDto> therapyDoseElements =
            ((VariableSimpleTherapyDto)simpleTherapy).getTimedDoseElements();
        final List<TimedSimpleDoseElementDto> changedTherapyDoseElements =
            ((VariableSimpleTherapyDto)changedSimpleTherapy).getTimedDoseElements();

        if (!JsonUtil.toJson(therapyDoseElements).equals(JsonUtil.toJson(changedTherapyDoseElements)))
        {
          final VariableDoseTherapyChangeDto change = new VariableDoseTherapyChangeDto();
          change.setOldValue(therapyDoseElements);
          change.setNewValue(changedTherapyDoseElements);
          changes.add(change);
        }
      }
    }
    else
    {
      throw new IllegalArgumentException("Change not supported");
    }
    return changes;
  }

  private Opt<StringsTherapyChangeDto> getDoseTimeChange(final ConstantTherapy therapy, final ConstantTherapy changedTherapy)
  {
    return getStringsTherapyChange(
        therapy.getDoseTimes().stream().map(HourMinuteDto::prettyPrint).collect(Collectors.toList()),
        changedTherapy.getDoseTimes().stream().map(HourMinuteDto::prettyPrint).collect(Collectors.toList()),
        TherapyChangeType.DOSE_TIMES);
  }

  private Collection<TherapyChangeDto<?, ?>> getOxygenTherapyChanges(final TherapyDto therapy, final TherapyDto changedTherapy)
  {
    Preconditions.checkArgument(
        changedTherapy instanceof OxygenTherapyDto,
        "Therapy" + therapy.getCompositionUid() + "not saved correctly");

    final OxygenTherapyDto therapy1 = (OxygenTherapyDto)therapy;
    final OxygenTherapyDto therapy2 = (OxygenTherapyDto)changedTherapy;

    final List<TherapyChangeDto<?, ?>> changes = new ArrayList<>();

    getStringTherapyChange(
        Opt.of(therapy1.getMedication().getDisplayName()),
        Opt.of(therapy2.getMedication().getDisplayName()),
        TherapyChangeType.MEDICATION)
        .forEach(changes::add);

    getStringTherapyChange(
        Opt.of(therapy1.getSpeedDisplay()),
        Opt.of(therapy2.getSpeedDisplay()),
        TherapyChangeType.DOSE)
        .forEach(changes::add);

    getStringTherapyChange(
        Opt.of(therapy1.getStartingDeviceDisplay()),
        Opt.of(therapy2.getStartingDeviceDisplay()),
        TherapyChangeType.DEVICE)
        .forEach(changes::add);

    getStringTherapyChange(
        Opt.of(therapy1.getSaturationDisplay()),
        Opt.of(therapy2.getSaturationDisplay()),
        TherapyChangeType.SATURATION)
        .forEach(changes::add);

    return changes;
  }

  private Opt<StringTherapyChangeDto> getStringTherapyChange(
      final Opt<String> value,
      final Opt<String> changedValue,
      final TherapyChangeType type)
  {
    if (!value.equals(changedValue))
    {
      final StringTherapyChangeDto change = new StringTherapyChangeDto(type);
      change.setOldValue(value.orElse(null));
      change.setNewValue(changedValue.orElse(null));
      return Opt.of(change);
    }
    return Opt.none();
  }

  private Opt<StringsTherapyChangeDto> getStringsTherapyChange(
      final List<String> valuesList,
      final List<String> changedValuesList,
      final TherapyChangeType type)
  {
    if (valuesList.size() != changedValuesList.size() || !valuesList.containsAll(changedValuesList))
    {
      final StringsTherapyChangeDto change = new StringsTherapyChangeDto(type);
      change.setOldValue(valuesList);
      change.setNewValue(changedValuesList);
      return Opt.of(change);
    }
    return Opt.none();
  }
}
