define(["jquery",
        "underscore",
        "lib/backbone",
        "datatables",
        "js-new/config/Configuration",
        "js-new/utils/utils"],
    function($,_,Backbone,DataTable,Configuration,utils) {
        
        /**
         * Initialize this view
         * @param options - map of
         *   {
         *     datamodel - metadata data model
         *     model - metadata model
         *   }
         */
        function init(options) {
            //Pull in parameters
            this.datamodel = options["datamodel"];
            this.model = options["model"];
            //Set inital product type
            this.type = (Configuration.DEFAULT_TYPE in this.datamodel.get("types") || Object.keys(this.datamodel.get("types")).length == 0)?Configuration.DEFAULT_TYPE:Object.keys(this.datamodel.get("types"))[0];
            this.datamodel.on("change:types",this.render,this);
            this.focused = null;
            this.ingesting = false;
            this._template = _.template($("script#template-elements-table").html());
            this._entryTemplate = _.template($("script#template-table-element").html());
            this.working = options["working-set"];
        };
        /**
         * Returns delimited list
         * @returns string of values separated by '|'
         */
        function getDelimitedList(list1,list2) {
            var tmp = _.zip(list1,list2);
            for (var i = 0; i < tmp.length; i++) {
                tmp[i] = tmp[i].join("|");
            }
            return tmp;
        }
        /**
         * Recursively merge two metadata objects
         * @param mer - original metadata object (merge into)
         * @param met - new metadat object (merge from)
         * @param ignoreFilled - ignore the field if a value is there
         */
        function recurseMerge(mer,met,ignoreFilled) {
            if (mer.name != met.name)
                return;
            //Lock if values don't match (if ignore filled, don't do anything and default to mer)
            if (!ignoreFilled && !_.isEqual(mer.values,met.values)) {
                mer.locked = true;
                mer.display = "** Inconsistent Values **";//getDelimitedList(mer.values,met.values);
            } else {
                delete mer.display;
            }
            //Merge the children (a metadata object technically has children
            for (var key in met.children) {
                if (!(key in mer.children))
                    mer.children[key] = {"name":met.children[key].name,"values":met.children[key].values,"children":{}};
                recurseMerge(mer.children[key],met.children[key],ignoreFilled);
            }
        };
        /**
         * Build an individual element entry
         * @param element - element to build
         * @param merged - merged metadata
         * @returns {name - name of element, html- html for this element's input}
         */
        function renderElementInput(element,merged) {
            var values = null;
            //Flag attributes
            var locked = "attachments" in element && "locked" in element.attachments || this.model.size() == 0;
            var required = "attachments" in element && "required" in element.attachments && this.model.size() != 0;
            var hidden = "attachments" in element && "hidden" in element.attachments && !required;

            var error = (element.elementName in merged.errors) ? merged.errors[element.elementName]:"";
            //Get values, (will create dropdown)
            if ("attachments" in element && "values" in element.attachments) {
                values = element.attachments.values.split(",");
            }
            //Grab in the current metadata
            var value = "";
            if (element.elementName in merged.children) {
                var current = merged.children[element.elementName];
                if ("display" in current) {
                    value = current["display"];
                } else if ("values" in current && current.values.length > 0) {
                    value = current.values[0];
                }
                //Lock it if multi-valued
                if ("locked" in current) {
                    locked = locked | current["locked"];
                }
            }
            //Grab the template and build it
            var obj = {
                "id":"input-"+element.elementName,
                "name":element.elementName,
                "displayName":("attachments" in element && "displayName" in element.attachments)?element.attachments.displayName:element.elementName,
                "description":("attachments" in element && "description" in element.attachments)?element.attachments.description:"",
                "locked":locked,
                "required":required,
                "hidden":hidden,
                "values":values,
                "value":(element.elementName == "ProductType")?this.type:value,
                "error":error
            };
            obj.html = this._entryTemplate(obj);     
            return obj
        }
        /**
         * Inspect the elements for equality and return true if "same"
         * @param obj1 - first object
         * @param obj2 - second object
         * @returns true if equal or false 
         */
        function equateObjects(obj1,obj2) {
            var fields = ["id","name","displayName","description","locked","required","hidden","values","value","error"];
            //Check undefined status
            if (typeof(obj1) != typeof(obj2) || typeof(obj1) === "undefined") {
                return false;
            }
            for (var i = 0; i < fields.length; i++) {
                if (!$.isArray(obj1[fields[i]]) && !$.isArray(obj2[fields[i]]) && obj1[fields[i]] != obj2[fields[i]]) {
                    return false;
                } else if ($.isArray(obj1[fields[i]]) != $.isArray(obj2[fields[i]])) {
                    return false;
                } else if ($.isArray(obj1[fields[i]]) && obj1[fields[i]].length != obj2[fields[i]].length) {
                    return false;
                } else if ($.isArray(obj1[fields[i]])) {
                    for (var j = 0; j < obj1[fields[i]].length; j++) {
                        if (obj1[fields[i]][j] != obj2[fields[i]][j]) {
                            return false;
                        }
                    }
                } 
            }
            return true;
        }
        /**
         * Render this view, based on the given data model
         * @param forceRefresh - force a refresh
         */
        function render(forceRefresh) {
            var self = this;
            var oldType = this.type;
            forceRefresh = (typeof(forceRefresh) === "undefined")?false:forceRefresh;
            //Set the type by first element
            if (this.model.size() > 0 && typeof(this.model.first().get("root")) !== "undefined" &&
                    "ProductType" in this.model.first().get("root").children && this.model.first().get("root").children["ProductType"].values.length > 0) {
                this.type =  this.model.first().get("root").children["ProductType"].values[0];
            }
            var completeRefresh = (oldType != this.type || forceRefresh);
            //Clear on blur for render updates
            $(this.$el).find("table:first").find("input,select").off("blur");
            var items = (this.type in this.datamodel.get("types")) ? this.datamodel.get("types")[this.type] : [];
            var inputHtmls = [];
            //Merge metadata together for display
            var merged = {"name":"root","values":[],"children":{},"errors":{}};
            //For each item in the collection, merge together
            this.model.each(
                function(elem) {
                    if (typeof(elem.get("root")) === "undefined")
                        return;
                    //Reset the model's type from selected type
                    if (!("ProductType" in elem.get("root").children) || elem.get("root").children["ProductType"].values.length == 0) {
                        elem.get("root").children["ProductType"] = {"name":"ProductType","values":[self.type],"children":{}}
                    }
                    recurseMerge(merged,utils.deep(elem.get("root")),false);
                    if (typeof(elem.validationError) === "undefined" || elem.validationError == null)
                        return;
                    //Update errors
                    for (var key in elem.validationError) {
                        merged.errors[key] = elem.validationError[key];
                    }

                });
            //Mask in working set metadata
            recurseMerge(merged,utils.deep(this.working.get("root")),true);
            
            //Build this element
            for (var i = 0; i < items.length; i++) {
                inputHtmls.push(this.renderElementInput(items[i],merged));
            }
            //Completely refresh or just update?
            if (completeRefresh) {
                this.$el.html(this._template({"$":$,"htmls":inputHtmls,"disabled":this.model.size() == 0,"ingesting":this.ingesting}));
                for (var i = 0; i < inputHtmls.length; i++) {
                    $("tr#"+inputHtmls[i].id).data(inputHtmls[i]);
                }
                //DataTables JS registering
                var table = $(this.$el).find("table:first");
                table.DataTable({"paging": false,"bFilter": false,"bSort": false});
                //Refocus event
                if (this.focused != null && this.focused != "") {
                    $("#"+this.focused).focus();
                }
                //Force update from current working set (this allows ghosting of metadata)
                for (var key in this.working.get("root").children) {
                    var childEntry = this.working.get("root").children[key];
                    var cntx = {
                        "name":childEntry.name,
                        "value":childEntry.values[0]
                    };
                    (this.dataEntry.bind(cntx))();
                }
                //Attach events to the controls bindings.
                $(this.$el).find("table:first").find("input,select").on("change",this.dataEntry);
            } else {
                $(this.$el).find("table:first").find("input,select").off("change");
                //Check elements for updates
                for (var i = 0; i < inputHtmls.length; i++) {
                    var cur = inputHtmls[i];
                    var obj = $("tr#"+cur.id).data();
                    if (!equateObjects(obj,cur)) {
                        $("tr#"+cur.id).html(cur.html);
                    }
                    $("tr#"+cur.id).data(cur);
                }
                $(this.$el).find("table:first").find("input,select").on("change",this.dataEntry);
            }
        };
        /**
         * A function to set the "on change" call-back for data inputs
         * @param func - function to call back (should come from controller)
         */
        function setOnEntryFunction(func) {
            this.dataEntry = func;
        }
        /**
         * Update the current product type from the controller
         * @param type - product type
         */
        function setProductType(type) {
            this.type = type;
        };
        //Return backbone view
        return Backbone.View.extend({
            initialize: init,
            render: render,
            setOnEntryFunction: setOnEntryFunction,
            setProductType: setProductType,
            //Private functions needing correct "this"
            renderElementInput: renderElementInput
            
        });
    });