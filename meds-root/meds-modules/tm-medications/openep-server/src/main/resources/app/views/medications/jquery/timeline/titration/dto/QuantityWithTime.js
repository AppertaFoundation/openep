Class.define('app.views.medications.timeline.titration.dto.QuantityWithTime', 'tm.jquery.Object', {

  time: null,
  quantity: null,
  bolusQuantity: null,
  bolusUnit: null,
  comment: null,

  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);

    if (this.time && !tm.jquery.Utils.isDate(this.time))
    {
      this.time = new Date(this.time);
    }
  },

  /**
   * @returns {Date|null}
   */
  getTime: function()
  {
    return this.time;
  },

  /**
   * @returns {Number}
   */
  getUtcTime: function()
  {
    var time = new Date(this.time);
    return Date.UTC(time.getFullYear(), time.getMonth(), time.getDate(), time.getHours(), time.getMinutes());
  },

  /**
   * @returns {Number|null}
   */
  getQuantity: function()
  {
    return this.quantity;
  },

  /**
   * @param {Number} quantity
   */
  setQuantity: function(quantity)
  {
    this.quantity = quantity;
  },

  /**
   * @returns {Number|null}
   */
  getBolusQuantity: function()
  {
    return this.bolusQuantity;
  },

  /**
   * @returns {String|null}
   */
  getBolusUnit: function()
  {
    return this.bolusUnit;
  },

  /**
   * @returns {String|null}
   */
  getComment: function()
  {
    return this.comment;
  },

  /**
   * @returns {Boolean}
   */
  isBolusAdministration: function()
  {
    return !tm.jquery.Utils.isEmpty(this.getBolusQuantity());
  },

  /**
   * @returns {Boolean}
   */
  hasComment: function()
  {
    return !tm.jquery.Utils.isEmpty(this.getComment());
  }
});