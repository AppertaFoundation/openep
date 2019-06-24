Class.define('app.views.medications.common.TherapyMedicationDataLoader', 'tm.jquery.Object', {
  view: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @param {app.views.medications.common.dto.Therapy|OxygenTherapy} therapy
   * @returns {tm.jquery.Promise}
   * @private
   */
  load: function(therapy)
  {
    var self = this;
    var deferred = tm.jquery.Deferred.create();
    var ingredientIds = therapy.getAllIngredientIds();

    if (therapy.isOrderTypeComplex())
    {
      if (ingredientIds.length > 0)
      {
        this.getView().getRestApi().loadMedicationDataForMultipleIds(ingredientIds).then(
            function onSuccess(medicationsData)
            {
              if (therapy.hasUniversalIngredient())
              {
                var extendedMedicationData =
                    self._extendAndSortMedicationDataWithUniversalMedications(therapy, medicationsData);
                deferred.resolve(extendedMedicationData);
              }
              else
              {
                deferred.resolve(medicationsData.sort(compareByIngredientOrder))
              }
            });
      }
      else
      {
        deferred.resolve([app.views.medications.MedicationUtils.getMedicationDataFromComplexTherapy(self.getView(), therapy)]);
      }
    }
    else
    {
      if (!therapy.getMedication().isMedicationUniversal())
      {
        this.getView().getRestApi().loadMedicationData(therapy.getMedication().getId()).then(
            function onSuccess(medicationData)
            {
              deferred.resolve(medicationData);
            });
      }
      else
      {
        deferred.resolve(app.views.medications.MedicationUtils.getMedicationDataFromSimpleTherapy(therapy));
      }
    }

    return deferred.promise();

    function compareByIngredientOrder(a, b)
    {
      return ingredientIds.indexOf(a.getMedication().getId()) - ingredientIds.indexOf(b.getMedication().getId());
    }
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * Check if medication on therapy is still active
   * @param {app.views.medications.common.dto.MedicationData|Array<app.views.medications.common.dto.MedicationData>} medicationData
   * @param {app.views.medications.common.dto.Therapy|app.views.medications.common.dto.OxygenTherapy} therapy
   * @returns {boolean}
   */
  isMedicationNoLongerAvailable: function(therapy, medicationData)
  {
    if (therapy.isOrderTypeComplex())
    {
      return !medicationData ||
          (tm.jquery.Utils.isArray(medicationData) && medicationData.length < 1) ||
          (tm.jquery.Utils.isArray(medicationData) && therapy.getIngredientsList() &&
          medicationData[0].getMedication().getId() !== therapy.getIngredientsList()[0].medication.getId())
    }
    else if (therapy.isOrderTypeSimple() || therapy.isOrderTypeOxygen())
    {
      return !medicationData || medicationData.getMedication().getId() !== therapy.getMedication().getId();
    }
    return false;
  },

  /**
   * Creates {@link MedicationData} objects for universal medications and sorts medicationData array to keep
   * order of medications from therapy
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {app.views.medications.common.dto.MedicationData|Array<app.views.medications.common.dto.MedicationData>} medicationData
   * @returns {Array<app.views.medications.common.dto.MedicationData>}
   * @private
   */
  _extendAndSortMedicationDataWithUniversalMedications: function(therapy, medicationData)
  {
    var self = this;
    if (tm.jquery.Utils.isArray(medicationData))
    {
      var extendedMedicationData = [];
      therapy.getIngredientsList().forEach(function(ingredient, i)
      {
        if (ingredient.medication.isMedicationUniversal())
        {
          var universalMedicationData =
              app.views.medications.MedicationUtils.getMedicationDataFromComplexTherapy(self.getView(), therapy, i);
          extendedMedicationData.push(universalMedicationData);
        }
        else
        {
          var ingredientMedicationData =
              app.views.medications.MedicationUtils.findInArray(medicationData, function(currentElement)
              {
                return currentElement.getMedication().getId() === ingredient.medication.getId();
              });
          extendedMedicationData.push(ingredientMedicationData);
        }
      });
    }
    return extendedMedicationData;
  }
});