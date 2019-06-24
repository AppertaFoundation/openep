Class.define('app.views.medications.common.ChangeReasonDataEntryContainer', 'app.views.common.containers.AppDataEntryContainer', {
  padding: 10,
  cls: 'change-reason-dialog',

  /* public members */
  defaultHeight: 230,
  defaultWidth: 460,
  view: null,
  startProcessOnEnter: true,

  titleIcon: null,
  titleText: null,
  selection: null,
  changeReasonTypeKey: null, /* app.views.medications.TherapyEnums.*/

  _form: null,
  _actionReasonField: null,
  _actionCommentField: null,

  Constructor: function (config)
  {
    this.callSuper(config);
    this._buildGUI();
    this._configureForm();
  },

  _buildGUI: function ()
  {
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0));

    var view = this.getView();

    var titleContainer = new app.views.medications.common.IconDescriptionContainer({
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 0),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      view: this.view,
      iconImage: this.getTitleIcon(),
      description: this.getTitleText(),
    });

    var actionReasonOptions = this.getSelectBoxValues(this.getChangeReasonTypeKey()).map(function (item)
    {
      return tm.jquery.SelectBox.createOption(item, null);
    });

    var selections = [];
    if (this.selection)
    {
      selections.push(this.selection);
    }

    var actionReasonField = new tm.jquery.SelectBox({
      cls: "action-reason-field",
      dropdownHeight: 5,
      dropdownWidth: "stretch",
      options: actionReasonOptions,
      selections: selections,
      multiple: false,
      allowSingleDeselect: true,
      placeholder: view.getDictionary("select"),
      defaultTextProvider: this._selectBoxTextProvider,
      defaultValueCompareToFunction: this._selectBoxValueCompare,
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      appendTo: function ()
      {
        return view.getAppFactory().getDefaultRenderToElement();
      }
    });
    actionReasonField.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function (component)
    {
      var options = component.getOptions();

      if (options.length === 1)
      {
        if (component.getSelections().length === 0)
        {
          component.setSelections(options[0]);
        }
        component.setEnabled(false);
      }
    });

    var actionCommentField = new tm.jquery.TextField({
      cls: "action-comment-field",
      placeholder: view.getDictionary("additional.reason")
    });

    this.add(titleContainer);
    this.add(actionReasonField);
    this.add(actionCommentField);

    this._actionReasonField = actionReasonField;
    this._actionCommentField = actionCommentField;
  },

  _configureForm: function ()
  {
    var form = new tm.jquery.Form({
      view: this.getView(),
      showTooltips: false,
      requiredFieldValidatorErrorMessage: this.getView().getDictionary("field.value.is.required")
    });

    // reason input //
    form.addFormField(new tm.jquery.FormField({
      name: "actionReasonSelectBox", label: null, component: this.getActionReasonField(), required: true,
      validation: {
        type: "local"
      },
      componentValueImplementationFn: function (component)
      {
        return component.hasSelections() ? component.getSelections() : null;
      },
      getComponentValidationMarkElement: function (component)
      {
        return component.getButtonElement();
      }
    }));

    this._form = form;
  },

  _selectBoxTextProvider: function (selectBox, index, option)
  {
    var item = option.getValue();
    return tm.jquery.Utils.isEmpty(item) ? null : item.name;
  },

  _selectBoxValueCompare: function (value1, value2)
  {
    return (tm.jquery.Utils.isEmpty(value1) ? null : value1.code)
        === (tm.jquery.Utils.isEmpty(value2) ? null : value2.code);
  },

  getSelectBoxValues: function (key)
  {
    var view = this.getView();
    var reasonMap = view.getChangeReasonTypeHolder().getMap();

    if (key && !tm.jquery.Utils.isEmpty(reasonMap))
    {
      return reasonMap.hasOwnProperty(key) && tm.jquery.Utils.isArray(reasonMap[key]) ?
          reasonMap[key] : [];
    }

    return [];
  },

  getChangeReasonTypeKey: function ()
  {
    return this.changeReasonTypeKey;
  },

  getView: function ()
  {
    return this.view;
  },

  getForm: function ()
  {
    return this._form;
  },

  getTitleIcon: function ()
  {
    return this.titleIcon;
  },

  getTitleText: function ()
  {
    return this.titleText;
  },

  getActionReasonField: function ()
  {
    return this._actionReasonField;
  },

  getActionCommentField: function ()
  {
    return this._actionCommentField;
  },

  processResultData: function (resultDataCallback)
  {
    var self = this;
    var form = this.getForm();
    var failResultData = new app.views.common.AppResultData({success: false, value: null});

    form.setOnValidationSuccess(function ()
    {
      var successResultData = new app.views.common.AppResultData({
        success: true,
        value: new app.views.medications.common.dto.TherapyChangeReason({
          changeReason: self.getActionReasonField().getSelections()[0],
          comment: self.getActionCommentField().getValue()
        })
      });
      resultDataCallback(successResultData);

    });
    form.setOnValidationError(function ()
    {
      resultDataCallback(failResultData);
    });

    form.submit();
  }
});