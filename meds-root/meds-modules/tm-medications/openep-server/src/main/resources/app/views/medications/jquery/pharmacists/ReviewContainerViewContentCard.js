Class.define('app.views.medications.pharmacists.ReviewContainerViewContentCard', 'tm.jquery.Container', {
  cls: "view-card",
  flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
  reviewContainer: null,

  therapiesListContainer: null,

  medicationSupplyTypeComponent: null,
  referBackComponent: null,
  reminderComponent: null,
  overallRecommendationComponent: null,

  drugRelatedProblemDescContainer: null,
  pharmacokineticIissueDescContainer: null,
  patientRelatedDescContainer: null,

  desiredTextFieldHeight: null,
  showSupply: null,

  /* constructor */
  Constructor: function (config)
  {
    this.callSuper(config);

    this.initialGuiBuild = this.getConfigValue('initialGuiBuild', true);

    this.desiredTextFieldHeight = this.getConfigValue('desiredTextFieldHeight', {
      overallRecommendation: null
    });

    if (this.initialGuiBuild)
    {
      this._buildGUI();
      this._populateReviewDataContainers();
    }
  },

  ///
  /// private methods
  ///
  _buildGUI: function ()
  {
    var view = this.getReviewContainer().getView();
    var data = this.getReviewContainer().getReviewData();

    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0));

    var therapiesListContainer = new tm.jquery.Container({
      cls: "therapy-list-container",
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0)
    });
    this.add(therapiesListContainer);

    if (!tm.jquery.Utils.isEmpty(data))
    {
      var basicDataRow = new tm.jquery.Container({
        cls: "basic-data-row",
        layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 0)
      });
      basicDataRow.getLayout().setFlexFlow(tm.jquery.flexbox.FlexFlow.create("row", "wrap"));

      if (this.showSupply === true && !tm.jquery.Utils.isEmpty(data.getMedicationSupplyType()))
      {
        var supplyText = view.getDictionary("MedicationSupplyTypeEnum." + data.getMedicationSupplyType());

        if (!tm.jquery.Utils.isEmpty(data.getDaysSupply()))
        {
          supplyText += ", " + data.getDaysSupply()
              + " " + (data.getDaysSupply() === 1 ? view.getDictionary("day") : view.getDictionary("days"));
        }
        var supplyContainer = new app.views.medications.common.VerticallyTitledComponent({
          titleText: view.getDictionary("supply") + " (" + view.getDictionary("in.days") + ")",
          contentComponent: new tm.jquery.Component({
            cls: "TextData",
            html: supplyText,
            flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
          })
        });
        this.medicationSupplyTypeComponent = supplyContainer.getContentComponent();

        basicDataRow.add(supplyContainer);
      }

      if (!tm.jquery.Utils.isEmpty(data.getReferBackToPrescriber()))
      {
        var referBackContainer = new app.views.medications.common.VerticallyTitledComponent({
          titleText: view.getDictionary("refer.back.to.prescriber"),
          contentComponent: new tm.jquery.Component({
            cls: "TextData",
            html: this.getTextValueForBoolean(data.getReferBackToPrescriber()),
            flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
          })
        });
        this.referBackComponent = referBackContainer.getContentComponent();
        basicDataRow.add(referBackContainer);
      }

      if (basicDataRow.getComponents().length > 0)
      {
        this.add(basicDataRow);
      }
    }

    if (!tm.jquery.Utils.isEmpty(this.getReviewContainer().getReviewData().getDrugRelatedProblem()))
    {
      var drugRelatedProblemDescContainer = new app.views.medications.pharmacists.ProblemDescriptionViewContainer({
        titleText: view.getDictionary("pharmacotherapeutic.drug.related"),
        problemDescription: this.getReviewContainer().getReviewData().getDrugRelatedProblem(),
        reviewContainerContentCard: this,
        hidden: tm.jquery.Utils.isEmpty(this.getReviewContainer().getReviewData().getDrugRelatedProblem())
      });
      this.add(drugRelatedProblemDescContainer);
    }

    if (!tm.jquery.Utils.isEmpty(this.getReviewContainer().getReviewData().getPharmacokineticIssue()))
    {
      var pharmacokineticIissueDescContainer = new app.views.medications.pharmacists.ProblemDescriptionViewContainer({
        titleText: view.getDictionary("pharmacokinetic.consultation"),
        problemDescription: this.getReviewContainer().getReviewData().getPharmacokineticIssue(),
        reviewContainerContentCard: this,
        hidden: tm.jquery.Utils.isEmpty(this.getReviewContainer().getReviewData().getPharmacokineticIssue())
      });
      this.add(pharmacokineticIissueDescContainer);

    }

    if (!tm.jquery.Utils.isEmpty(this.getReviewContainer().getReviewData().getPatientRelatedProblem()))
    {
      var patientRelatedDescContainer = new app.views.medications.pharmacists.ProblemDescriptionViewContainer({
        titleText: view.getDictionary("patient.related"),
        problemDescription: this.getReviewContainer().getReviewData().getPatientRelatedProblem(),
        reviewContainerContentCard: this,
        hidden: tm.jquery.Utils.isEmpty(this.getReviewContainer().getReviewData().getPatientRelatedProblem())
      });
      this.add(patientRelatedDescContainer);
    }

    if (!tm.jquery.Utils.isEmpty(data) && !tm.jquery.Utils.isEmpty(data.getOverallRecommendation()))
    {
      var overallRecommendationContainer = new app.views.medications.common.VerticallyTitledComponent({
        titleText: view.getDictionary("overall.recommendation"),
        contentComponent: new tm.jquery.Component({
          cls: "TextData",
          style: "white-space: pre-wrap;",
          html: tm.jquery.Utils.isEmpty(data) ? null : tm.jquery.Utils.escapeHtml(data.getOverallRecommendation())
        }),
        hidden: !tm.jquery.Utils.isEmpty(data) && tm.jquery.Utils.isEmpty(data.getOverallRecommendation())
      });
      this.add(overallRecommendationContainer);
      this.overallRecommendationComponent = overallRecommendationContainer.getContentComponent();
    }

    this.therapiesListContainer = therapiesListContainer;
  },

  _populateReviewDataContainers: function()
  {
    this._populateTherapiesListContainer();
  },

  _populateTherapiesListContainer: function ()
  {
    var self = this;
    var view = this.getReviewContainer().getView();
    var data = this.getReviewContainer().getReviewData();
    var relatedTherapies = tm.jquery.Utils.isEmpty(data) ? [] : data.getRelatedTherapies();
    var therapiesListContainer = this.getTherapiesListContainer();

    relatedTherapies.forEach(function (therapyData)
    {
      var therapyContainer = self.buildTherapyContainer(view, therapyData);
      therapiesListContainer.add(therapyContainer);
    });
  },

  buildTherapyContainer: function (view, data)
  {
    return app.views.medications.pharmacists.TherapyContainer.forViewReviewContentCard({
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      view: view,
      data: data
    });
  },

  getTextValueForBoolean: function (value)
  {
    var view = this.getReviewContainer().getView();

    if (tm.jquery.Utils.isEmpty(value)) return null;

    return value === true ? view.getDictionary("yes") : view.getDictionary("no");
  },

  /* public methods*/
  getReviewContainer: function ()
  {
    return this.reviewContainer;
  },

  getTherapiesListContainer: function ()
  {
    return this.therapiesListContainer;
  },

  getReferBackComponent: function ()
  {
    return this.referBackComponent;
  },

  getMedicationSupplyTypeComponent: function ()
  {
    return this.medicationSupplyTypeComponent;
  },

  getDrugRelatedProblemDescContainer: function ()
  {
    return this.drugRelatedProblemDescContainer;
  },

  getPharmacokineticIissueDescContainer: function ()
  {
    return this.pharmacokineticIissueDescContainer;
  },

  getPatientRelatedDescContainer: function ()
  {
    return this.patientRelatedDescContainer;
  },

  getOverallRecommendationComponent: function ()
  {
    return this.overallRecommendationComponent;
  },

  refresh: function ()
  {
    if (this.isRendered())
    {
      this.removeAll();

      this.overallRecommendationComponent = null;
      this.referBackComponent = null;

      this._buildGUI();
      this._populateReviewDataContainers();
      this.repaint();
    }
  }
});