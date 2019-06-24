Class.define('app.views.medications.common.TherapyGroupPanel', 'tm.jquery.Panel', {
  cls: "therapy-group-panel",
  groupId: null,
  groupTitle: null,
  view: null,

  contentData: null,
  displayProvider: null,

  dynamicContent: false,

  /*** privates */
  _preventMultipliedDataOnExpand: false,
  
  Constructor: function (config)
  {
    this.callSuper(config);

    var self = this;

    this.showHeader = true;
    this.showFooter = false;
    this.collapsible = true;

    this.contentData = this.getConfigValue("contentData", []);
    this.groupTitle = this.getConfigValue("groupTitle", "");
    this.displayProvider = this.getConfigValue("displayProvider", new app.views.medications.common.therapy.TherapyContainerDisplayProvider({
      view: this.getView(),
      getStatusIcon: function (dto)
      {
        var status = tm.jquery.Utils.isEmpty(dto.changeType) ? dto.therapyStatus : dto.changeType;
        var enums = app.views.medications.TherapyEnums;
        if (status == enums.therapyStatusEnum.ABORTED)
        {
          return "icon_aborted";
        }
        else if (status == enums.therapyStatusEnum.CANCELLED)
        {
          return "icon_cancelled";
        }
        else if (status == enums.therapyStatusEnum.SUSPENDED)
        {
          return "icon_suspended";
        }
        return null;
      },
      getPharmacistReviewIcon: function (status)
      {
        return null;
      }
    }));

    this._initHeaderContainer();
    this._initContentContainer();

    if (!this.isDynamicContent())
    {
      this._processContentData();
    }
    else
    {
      this.on(tm.jquery.ComponentEvent.EVENT_TYPE_BEFORE_EXPAND, function()
      {
        if (!self._preventMultipliedDataOnExpand)
        {
          self._preventMultipliedDataOnExpand = true;
          self._processContentData(true);
        }
      });
      this.on(tm.jquery.ComponentEvent.EVENT_TYPE_AFTER_COLLAPSE, function ()
      {
        self._preventMultipliedDataOnExpand = false;
        var contentContainer = self.getContent();
        contentContainer.removeAll();
      });
    }
  },

  _initHeaderContainer: function ()
  {
    var header = this.getHeader();
    header.setCls("TextDataBold");
    header.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "center", 0));

    var headerValueContainer = new tm.jquery.Container({
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      cursor: "pointer",
      cls: "TextDataBold",
      html: this.getGroupTitle()
    });

    header.add(headerValueContainer);
    this.bindToggleEvent([headerValueContainer]);
  },

  _initContentContainer: function ()
  {
    var contentContainer = this.getContent();
    contentContainer.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0));
  },

  _processContentData: function (repaint)
  {
    var contentContainer = this.getContent();
    var content = this.getContentData();

    if (tm.jquery.Utils.isArray(content))
    {
      for (var i = 0; i < content.length; i++)
      {
        var elementContainer = this.buildElementContainer(content[i]);
        contentContainer.add(elementContainer);
      }
    }

    if (repaint === true && this.isRendered()) contentContainer.repaint();
  },

  addElement: function(elementData) {
    var contentContainer = this.getContent();
    var elementContainer = this.buildElementContainer(elementData);
    contentContainer.add(elementContainer);
    this.getContentData().push(elementData);

    if (contentContainer.isRendered()) {
      elementContainer.doRender();
      jQuery(contentContainer.getDom()).prepend(elementContainer.getDom());
    }

    return elementContainer;
  },

  buildElementContainer: function (elementData)
  {
    var therapyContainer = new app.views.medications.common.therapy.TherapyContainer({
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      view: this.getView(),
      data: elementData,
      displayProvider: this.getDisplayProvider(),
      showIconTooltip: false,
      /* this is ugly but for some reason contentContainer's getParent() returns a different object ... */
      groupPanel: this,
      getGroupPanel: function() {
        return this.groupPanel;
      }
    });
    this.attachElementToolbar(therapyContainer);
    return therapyContainer;
  },

  attachElementToolbar: function(elementContainer)
  {

  },

  refresh: function (){
    if (this.isDynamicContent() && this.isCollapsed()) return;

    var contentContainer = this.getContent();
    contentContainer.removeAll();
    this._processContentData();

    if (contentContainer.isRendered()) contentContainer.repaint();
  },

  getView: function ()
  {
    return this.view;
  },

  getDisplayProvider: function()
  {
    return this.displayProvider
  },

  getGroupId: function()
  {
    return this.groupId;
  },

  getGroupTitle: function ()
  {
    return this.groupTitle;
  },

  getContentData: function ()
  {
    return this.contentData;
  },

  setContentData: function(content)
  {
    this.contentData = tm.jquery.Utils.isEmpty(content) ? [] : content;
  },

  isDynamicContent: function()
  {
    return this.dynamicContent === true;
  },

  remove: function(container) {
    var contentComponents = this.getContent().getComponents();
    var index = contentComponents.indexOf(container);

    if (index > -1)
    {
      this.getContentData().splice(index, 1);
      this.getContent().remove(container);
    }
  },

  removeByIndex: function(index) {
    var contentData = this.getContentData();
    var contentComponents = this.getContent().getComponents();
    if (index >= 0 && index < contentData.length) {
      contentData.splice(index, 1);

      var elementContainer = contentComponents[index];
      this.getContent().remove(elementContainer);
    }
  },

  removeByData: function(elementData) {
    var index = this.getContentData().indexOf(elementData);
    this.removeByIndex(index);
  },

  getElementContainerByData: function(elementData) {
    var index = this.getContentData().indexOf(elementData);
    var contentComponents = this.getContent().getComponents();
    if (index > -1 && index < contentComponents.length) {
      return contentComponents[index];
    }
    return null;
  }
});