package com.marand.thinkmed.medications.infusion.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.marand.ispek.ehr.common.EhrLinkType;
import com.marand.maf.core.Pair;
import com.marand.maf.core.time.Intervals;
import com.marand.thinkehr.session.EhrSessioned;
import com.marand.thinkmed.medications.administration.AdministrationProvider;
import com.marand.thinkmed.medications.api.internal.dto.ComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationResultEnum;
import com.marand.thinkmed.medications.dto.administration.AdministrationTaskDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationTypeEnum;
import com.marand.thinkmed.medications.dto.administration.InfusionBagAdministration;
import com.marand.thinkmed.medications.dto.administration.InfusionBagDto;
import com.marand.thinkmed.medications.dto.administration.InfusionBagTaskDto;
import com.marand.thinkmed.medications.dto.administration.InfusionSetChangeDto;
import com.marand.thinkmed.medications.dto.unit.KnownUnitType;
import com.marand.thinkmed.medications.ehr.model.InpatientPrescription;
import com.marand.thinkmed.medications.ehr.utils.PrescriptionsEhrUtils;
import com.marand.thinkmed.medications.infusion.AdministrationCandidateDo;
import com.marand.thinkmed.medications.infusion.InfusionBagHandler;
import com.marand.thinkmed.medications.infusion.InfusionBagTaskHandler;
import com.marand.thinkmed.medications.infusion.InfusionBagTaskProvider;
import com.marand.thinkmed.medications.task.MedicationsTasksProvider;
import com.marand.thinkmed.medications.therapy.util.TherapyIdUtils;
import com.marand.thinkmed.medications.units.converter.UnitsConverter;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Minutes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.marand.thinkmed.medications.dto.unit.KnownUnitType.ML;

/**
 * @author Nejc Korasa
 */
@Component
public class InfusionBagHandlerImpl implements InfusionBagHandler
{
  private MedicationsTasksProvider medicationsTasksProvider;
  private MedicationsBo medicationsBo;
  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private InfusionBagTaskProvider infusionBagTaskProvider;
  private InfusionBagTaskHandler infusionBagTaskHandler;
  private AdministrationProvider administrationProvider;
  private UnitsConverter unitsConverter;

  @Autowired
  public void setMedicationsTasksProvider(final MedicationsTasksProvider medicationsTasksProvider)
  {
    this.medicationsTasksProvider = medicationsTasksProvider;
  }

  @Autowired
  public void setMedicationsBo(final MedicationsBo medicationsBo)
  {
    this.medicationsBo = medicationsBo;
  }

  @Autowired
  public void setMedicationsOpenEhrDao(final MedicationsOpenEhrDao medicationsOpenEhrDao)
  {
    this.medicationsOpenEhrDao = medicationsOpenEhrDao;
  }

  @Autowired
  public void setInfusionBagTaskHandler(final InfusionBagTaskHandler infusionBagTaskHandler)
  {
    this.infusionBagTaskHandler = infusionBagTaskHandler;
  }

  @Autowired
  public void setInfusionBagTaskProvider(final InfusionBagTaskProvider infusionBagTaskProvider)
  {
    this.infusionBagTaskProvider = infusionBagTaskProvider;
  }

  @Autowired
  public void setAdministrationProvider(final AdministrationProvider administrationProvider)
  {
    this.administrationProvider = administrationProvider;
  }

  @Autowired
  public void setUnitsConverter(final UnitsConverter unitsConverter)
  {
    this.unitsConverter = unitsConverter;
  }

  @Override
  @EhrSessioned
  public void recalculateInfusionBagChange(
      final @NonNull String patientId,
      final @NonNull String currentTherapyId,
      final AdministrationDto modifiedAdministrationDto,
      final String deletedAdministrationId,
      final @NonNull DateTime actionTimestamp)
  {
    final InpatientPrescription prescription = getInpatientPrescription(patientId, currentTherapyId);
    final TherapyDto currentTherapy = medicationsBo.convertMedicationOrderToTherapyDto(prescription, prescription.getMedicationOrder());

    if (currentTherapy instanceof ComplexTherapyDto && ((ComplexTherapyDto)currentTherapy).isContinuousInfusion())
    {
      final List<InpatientPrescription> prescriptions = new ArrayList<>();
      final List<String> therapyIds = new ArrayList<>();
      loadInstructionsAndTherapyIds(patientId, PrescriptionsEhrUtils.getOriginalTherapyId(prescription), prescriptions, therapyIds);

      final List<AdministrationDto> givenAdministrations = getGivenAdministrations(patientId, prescriptions);
      handleRecalculateForContinuousInfusion(
          currentTherapy,
          currentTherapyId,
          therapyIds,
          patientId,
          givenAdministrations,
          modifiedAdministrationDto,
          deletedAdministrationId);
    }
  }

  private List<InpatientPrescription> getTherapiesWithOriginLink(final String patientId, final String originalTherapyId)
  {
    final Pair<String, String> parsedTherapyId = TherapyIdUtils.parseTherapyId(originalTherapyId);
    final String compositionUid = parsedTherapyId.getFirst();
    return medicationsOpenEhrDao.getLinkedPrescriptions(patientId, compositionUid, EhrLinkType.ORIGIN);
  }

  @Override
  public Double getRemainingInfusionBagQuantity(
      final @NonNull String patientId,
      final @NonNull String therapyId,
      final @NonNull DateTime when)
  {
    final InpatientPrescription prescription = getInpatientPrescription(patientId, therapyId);
    final TherapyDto therapy = medicationsBo.convertMedicationOrderToTherapyDto(prescription, prescription.getMedicationOrder());

    if (therapy instanceof ComplexTherapyDto && ((ComplexTherapyDto)therapy).isContinuousInfusion())
    {
      final List<InpatientPrescription> prescriptions = new ArrayList<>();
      final List<String> therapyIds = new ArrayList<>();
      loadInstructionsAndTherapyIds(
          patientId,
          PrescriptionsEhrUtils.getOriginalTherapyId(prescription),
          prescriptions,
          therapyIds);

      final List<AdministrationDto> givenAdministrations = getGivenAdministrations(patientId, prescriptions);
      final Pair<DateTime, InfusionBagDto> lastBag = findLastInfusionBagChange(givenAdministrations);

      return getRemainingBagQuantityForContinuousInfusion(therapyIds, patientId, givenAdministrations, lastBag, when);
    }

    return null;
  }

  /**
   * Loads instructions with "ORIGIN" link to originalTherapyId, including original instruction
   * and fills instructions and therapyIds lists.
   *
   * @see EhrLinkType
   */
  private void loadInstructionsAndTherapyIds(
      final String patientId,
      final String originalTherapyId,
      final List<InpatientPrescription> prescriptions,
      final List<String> therapyIds)
  {
    prescriptions.addAll(getTherapiesWithOriginLink(patientId, originalTherapyId));
    therapyIds.addAll(prescriptions.stream().map(TherapyIdUtils::createTherapyId).collect(Collectors.toList()));

    if (!therapyIds.contains(originalTherapyId))
    {
      final String compositionUid = TherapyIdUtils.extractCompositionUid(originalTherapyId);
      prescriptions.add(medicationsOpenEhrDao.loadInpatientPrescription(patientId, compositionUid));
      therapyIds.add(originalTherapyId);
    }
  }

  private Pair<DateTime, InfusionBagDto> findLastInfusionBagChange(final List<AdministrationDto> givenAdministrations)
  {
    final Optional<AdministrationDto> lastBagAdministration = givenAdministrations
        .stream()
        .filter(a -> a instanceof InfusionBagAdministration && ((InfusionBagAdministration)a).getInfusionBag() != null)
        .max(Comparator.comparing(AdministrationDto::getAdministrationTime));

    return lastBagAdministration
        .map(a -> Pair.of(a.getAdministrationTime(), ((InfusionBagAdministration)a).getInfusionBag())).orElse(null);
  }

  private void handleRecalculateForContinuousInfusion(
      final TherapyDto currentTherapy,
      final String currentTherapyId,
      final List<String> therapyIds,
      final String patientId,
      final List<AdministrationDto> givenAdministrations,
      final AdministrationDto modifiedAdministrationDto,
      final String deletedAdministrationId)
  {
    final List<AdministrationTaskDto> activeTasks = medicationsTasksProvider.findAdministrationTasks(
        patientId,
        therapyIds,
        null,
        false);

    final List<AdministrationTaskDto> filteredTasks = filterActiveTasks(activeTasks, modifiedAdministrationDto);
    final List<AdministrationDto> filteredAdministrations = filterGivenAdministrations(
        givenAdministrations,
        modifiedAdministrationDto,
        deletedAdministrationId);

    final Pair<DateTime, InfusionBagDto> lastInfusionBag = findLastInfusionBagChange(filteredAdministrations);
    if (lastInfusionBag != null)
    {
      final DateTime plannedInfusionBagChange = calculatePlannedInfusionBagChangeForContinuousInfusion(
          currentTherapy.getEnd(),
          filteredTasks,
          filteredAdministrations,
          lastInfusionBag.getSecond(),
          lastInfusionBag.getFirst());

      updateInfusionBagChange(currentTherapyId, therapyIds, patientId, plannedInfusionBagChange);
    }
    else
    {
      infusionBagTaskHandler.deleteInfusionBagTasks(patientId, therapyIds, null);
    }
  }

  private List<AdministrationDto> filterGivenAdministrations(
      final List<AdministrationDto> givenAdministrations,
      final AdministrationDto modifiedAdministrationDto,
      final String deletedAdministrationId)
  {
    List<AdministrationDto> filteredAdministrations = new ArrayList<>(givenAdministrations);
    if (modifiedAdministrationDto != null)
    {
      final String administrationUidNoVersion = TherapyIdUtils.getCompositionUidWithoutVersion(
          modifiedAdministrationDto.getAdministrationId());

      if (administrationUidNoVersion != null)
      {
        filteredAdministrations = givenAdministrations
            .stream()
            .filter(administrationDto -> !administrationUidNoVersion.equals(
                TherapyIdUtils.getCompositionUidWithoutVersion(administrationDto.getAdministrationId())))
            .collect(Collectors.toList());

        filteredAdministrations.add(modifiedAdministrationDto);
      }
      else
      {
        filteredAdministrations.add(modifiedAdministrationDto);
      }
    }
    if (deletedAdministrationId != null)
    {
      final String deletedAdministrationUidWithoutVersion =
          TherapyIdUtils.getCompositionUidWithoutVersion(deletedAdministrationId);

      filteredAdministrations = filteredAdministrations
          .stream()
          .filter(administrationDto -> !deletedAdministrationUidWithoutVersion.equals(
              TherapyIdUtils.getCompositionUidWithoutVersion(administrationDto.getAdministrationId())))
          .collect(Collectors.toList());
    }
    return filteredAdministrations;
  }

  private List<AdministrationTaskDto> filterActiveTasks(
      final List<AdministrationTaskDto> tasks,
      final AdministrationDto modifiedAdministrationDto)
  {
    final Optional<String> taskId = Optional.ofNullable(modifiedAdministrationDto).map(AdministrationDto::getTaskId);
    if (taskId.isPresent() && TherapyIdUtils.getCompositionUidWithoutVersion(modifiedAdministrationDto.getAdministrationId()) == null)
    {
      return tasks
          .stream()
          .filter(task -> !task.getTaskId().equals(taskId.get()))
          .collect(Collectors.toList());
    }
    return tasks;
  }

  /**
   * Updates infusion bag change tasks for therapy, where therapyIds are all therapy ids with same original therapy id link.
   */
  private void updateInfusionBagChange(
      final String currentTherapyId,
      final List<String> therapyIds,
      final String patientId,
      final DateTime plannedInfusionBagChange)
  {
    final List<InfusionBagTaskDto> bagTasks = infusionBagTaskProvider.findInfusionBagTasks(patientId, therapyIds, null);

    if (bagTasks.isEmpty() || !bagTasks.get(0).getPlannedAdministrationTime().equals(plannedInfusionBagChange))
    {
      infusionBagTaskHandler.deleteInfusionBagTasks(patientId, therapyIds, "New infusion bag change task made");
      if (plannedInfusionBagChange != null)
      {
        infusionBagTaskHandler.createInfusionBagTask(patientId, currentTherapyId, plannedInfusionBagChange);
      }
    }
  }

  public DateTime calculatePlannedInfusionBagChangeForContinuousInfusion(
      final DateTime therapyEnd,
      final List<AdministrationTaskDto> notCompletedAdjustInfusionTasks,
      final List<AdministrationDto> givenAdministrations,
      final InfusionBagDto lastInfusionBagDto,
      final DateTime lastInfusionBagChangeTime)
  {
    final List<AdministrationCandidateDo> administrationCandidates = extractRelevantAdministrationCandidates(
        notCompletedAdjustInfusionTasks,
        givenAdministrations,
        Intervals.infiniteFrom(lastInfusionBagChangeTime));

    Double availableQuantity = lastInfusionBagDto.getQuantity();
    DateTime plannedInfusionBagTime = null;
    for (final AdministrationCandidateDo candidate : administrationCandidates)
    {
      final TherapyDoseDto therapyDoseDto = candidate.getTherapyDose();

      final AdministrationTypeEnum administrationType = candidate.getAdministrationType();
      if (administrationType == AdministrationTypeEnum.BOLUS
          && candidate.getAdministrationTime().isAfter(lastInfusionBagChangeTime))
      {
        availableQuantity -= getAdministeredQuantity(therapyDoseDto, ML);

        if (availableQuantity <= 0)
        {
          plannedInfusionBagTime = candidate.getAdministrationTime();
          break;
        }
      }
      else if (administrationType == AdministrationTypeEnum.STOP)
      {
        return null;
      }
      else
      {
        final Double rate = therapyDoseDto.getNumerator();

        final Integer minutesDuration = calculateRateDurationInterval(
            candidate,
            lastInfusionBagChangeTime,
            therapyEnd,
            administrationCandidates);

        final DateTime from = candidate.getAdministrationTime().isBefore(lastInfusionBagChangeTime)
                              ? lastInfusionBagChangeTime
                              : candidate.getAdministrationTime();

        if (minutesDuration != null)
        {
          final Double administrationQuantity = rate * minutesDuration / 60;
          availableQuantity -= administrationQuantity;

          if (availableQuantity <= 0)
          {
            final double minutesTillEmptyBag = (administrationQuantity + availableQuantity) / rate * 60;
            //noinspection NumericCastThatLosesPrecision
            plannedInfusionBagTime = from.plusMinutes((int)minutesTillEmptyBag);
            break;
          }
        }
        else
        {
          final double minutesTillEmptyBag = availableQuantity / rate * 60;
          //noinspection NumericCastThatLosesPrecision
          plannedInfusionBagTime = from.plusMinutes((int)minutesTillEmptyBag);
          break;
        }
      }
    }

    if (plannedInfusionBagTime == null)
    {
      final Optional<AdministrationCandidateDo> lastCandidate = administrationCandidates
          .stream()
          .max(Comparator.comparing(AdministrationCandidateDo::getAdministrationTime));

      if (lastCandidate.isPresent())
      {
        final Double rate = lastCandidate.get().getTherapyDose().getNumerator();
        final double minutesTillEmptyBag = availableQuantity / rate * 60;

        //noinspection NumericCastThatLosesPrecision
        plannedInfusionBagTime =
            lastCandidate.get().getAdministrationTime().plusMinutes((int)minutesTillEmptyBag);
      }
    }

    return plannedInfusionBagTime;
  }

  private double getAdministeredQuantity(final TherapyDoseDto therapyDose, final KnownUnitType knownUnit)
  {
    if (therapyDose.getDenominatorUnit() != null && unitsConverter.isConvertible(therapyDose.getDenominatorUnit(), knownUnit))
    {
      return unitsConverter.convert(therapyDose.getDenominator(), therapyDose.getDenominatorUnit(), knownUnit);
    }
    return unitsConverter.convert(therapyDose.getNumerator(), therapyDose.getNumeratorUnit(), knownUnit);
  }

  Double getRemainingBagQuantityForContinuousInfusion(
      final List<String> therapyIds,
      final String patientId,
      final List<AdministrationDto> givenAdministrations,
      final Pair<DateTime, InfusionBagDto> lastInfusionBag,
      final DateTime when)
  {
    if (lastInfusionBag != null && !lastInfusionBag.getFirst().isAfter(when))
    {
      final List<AdministrationTaskDto> notCompletedAdjustInfusionTasks = extractAdjustInfusionTasks(
          medicationsTasksProvider.findAdministrationTasks(patientId, therapyIds, null, false));

      final DateTime lastInfusionBagTime = lastInfusionBag.getFirst();

      final List<AdministrationCandidateDo> candidates = extractRelevantAdministrationCandidates(
          notCompletedAdjustInfusionTasks,
          givenAdministrations,
          new Interval(lastInfusionBagTime, when));

      Double availableQuantity = lastInfusionBag.getSecond().getQuantity();
      for (final AdministrationCandidateDo candidate : candidates)
      {
        final TherapyDoseDto therapyDoseDto = candidate.getTherapyDose();
        if (candidate.getAdministrationType() == AdministrationTypeEnum.BOLUS
            && candidate.getAdministrationTime().isAfter(lastInfusionBagTime))
        {
          availableQuantity -= getAdministeredQuantity(therapyDoseDto, ML);
        }
        else
        {
          final Integer minutesDuration = calculateRateDurationInterval(candidate, lastInfusionBagTime, when, candidates);

          if (minutesDuration != null)
          {
            availableQuantity -= therapyDoseDto.getNumerator() * minutesDuration / 60;
          }
        }
      }
      return availableQuantity;
    }
    return null;
  }

  private Integer calculateRateDurationInterval(
      final AdministrationCandidateDo administrationCandidate,
      final DateTime from,
      final DateTime to,
      final List<AdministrationCandidateDo> administrationCandidates)
  {
    final DateTime administrationTime = administrationCandidate.getAdministrationTime();
    if (to != null && administrationTime.isAfter(to))
    {
      return null;
    }

    final Optional<DateTime> nextAdministrationTime = administrationCandidates
        .stream()
        .filter(administration -> administration.getAdministrationTime().isAfter(administrationTime))
        .filter(administration -> to == null || administration.getAdministrationTime().isBefore(to))
        .filter(a -> a.getAdministrationType() == AdministrationTypeEnum.STOP
            || a.getAdministrationType() == AdministrationTypeEnum.ADJUST_INFUSION
            || a.getAdministrationType() == AdministrationTypeEnum.START)
        .map(AdministrationCandidateDo::getAdministrationTime)
        .filter(Objects::nonNull)
        .findFirst();

    final DateTime calculateFrom = administrationTime.isBefore(from) ? from : administrationTime;
    final DateTime calculateUntil = nextAdministrationTime.orElse(to);
    return calculateUntil != null ? Minutes.minutesIn(new Interval(calculateFrom, calculateUntil)).getMinutes() : null;
  }

  /**
   * Extracts AdministrationDtos and AdministrationTasks in interval with last AdministrationDto before interval start time.
   * Sorted by time ascending.
   */
  private List<AdministrationCandidateDo> extractRelevantAdministrationCandidates(
      final List<AdministrationTaskDto> notCompletedAdjustInfusionTasks,
      final List<AdministrationDto> givenAdministrations,
      final Interval interval)
  {
    final List<AdministrationCandidateDo> administrationCandidates = new ArrayList<>();

    administrationCandidates
        .addAll(givenAdministrations
                    .stream()
                    .filter(administrationDto -> AdministrationTypeEnum.MEDICATION_ADMINISTRATION.contains(administrationDto.getAdministrationType()))
                    .filter(administrationDto -> interval.contains(administrationDto.getAdministrationTime()))
                    .map(AdministrationCandidateDo::new)
                    .collect(Collectors.toList()));

    administrationCandidates
        .addAll(notCompletedAdjustInfusionTasks
                    .stream()
                    .filter(administrationTaskDto -> interval.contains(administrationTaskDto.getPlannedAdministrationTime()))
                    .map(AdministrationCandidateDo::new)
                    .collect(Collectors.toList()));

    //noinspection SimplifyOptionalCallChains
    final AdministrationCandidateDo lastBeforeBagChangeAdministrationCandidate = givenAdministrations
        .stream()
        .filter(this::isStartOrAdjustInfusion)
        .filter(administrationDto -> administrationDto.getAdministrationTime().isBefore(interval.getStart()))
        .max(Comparator.comparing(AdministrationDto::getAdministrationTime))
        .map(AdministrationCandidateDo::new)
        .orElse(null);

    if (lastBeforeBagChangeAdministrationCandidate != null)
    {
      administrationCandidates.add(lastBeforeBagChangeAdministrationCandidate);
    }

    return administrationCandidates
        .stream()
        .sorted(Comparator.comparing(AdministrationCandidateDo::getAdministrationTime))
        .collect(Collectors.toList());
  }

  private List<AdministrationDto> getGivenAdministrations(
      final String patientId,
      final List<InpatientPrescription> prescriptions)
  {
    return administrationProvider.getPrescriptionsAdministrations(patientId, prescriptions, null, true)
        .stream()
        .filter(this::isGiven)
        .collect(Collectors.toList());
  }

  private boolean isGiven(final AdministrationDto administration)
  {
    if (administration instanceof InfusionSetChangeDto)
    {
      return true;
    }
    return AdministrationResultEnum.ADMINISTERED.contains(administration.getAdministrationResult());
  }

  private InpatientPrescription getInpatientPrescription(
      final String patientId,
      final String therapyId)
  {
    final Pair<String, String> stringPair = TherapyIdUtils.parseTherapyId(therapyId);
    return medicationsOpenEhrDao.loadInpatientPrescription(patientId, stringPair.getFirst());
  }

  private List<AdministrationTaskDto> extractAdjustInfusionTasks(final List<AdministrationTaskDto> administrationTasks)
  {
    return administrationTasks
        .stream()
        .filter(administrationTaskDto -> administrationTaskDto.getAdministrationTypeEnum() == AdministrationTypeEnum.ADJUST_INFUSION)
        .collect(Collectors.toList());
  }

  private boolean isStartOrAdjustInfusion(final AdministrationDto administrationDto)
  {
    final AdministrationTypeEnum administrationTypeEnum = administrationDto.getAdministrationType();
    return administrationTypeEnum == AdministrationTypeEnum.START || administrationTypeEnum == AdministrationTypeEnum.ADJUST_INFUSION;
  }
}
