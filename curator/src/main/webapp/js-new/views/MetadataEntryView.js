define(["jquery",
        "underscore",
        "lib/backbone",
        "datatables",
        "js-new/utils/utils"],
    function($,_,Backbone,DataTable,utils) {
        
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
            this.type = ("GenericFile" in this.datamodel.get("types") || Object.keys(this.datamodel.get("types")).length == 0)?"GenericFile":Object.keys(this.datamodel.get("types"))[0];
            this.datamodel.on("change:types",this.render,this);
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
         */
        function recurseMerge(mer,met) {
            if (mer.name != met.name)
                return;
            //Lock if values don't match
            if (!_.isEqual(mer.values,met.values)) {
                mer.locked = true;
                mer.display = getDelimitedList(mer.values,met.values);
            } else {
                delete mer.display;
            }
            //Merge the children (a metadata object technically has children
            for (var key in met.children) {
                if (!(key in mer.children))
                    mer.children[key] = {"name":met.children[key].name,"values":met.children[key].values,"children":{}};
                recurseMerge(mer.children[key],met.children[key]);
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
            var required = "attachments" in element && "required" in element.attachments;
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
            var tmp = _.template($("script#template-table-element").html());
            return {"name": element.elementName,
                    "html": tmp({"name":element.elementName,
                                 "locked":locked,
                                 "required":required,
                                 "hidden":hidden,
                                 "values":values,
                                 "value":(element.elementName == "ProductType")?this.type:value,
                                 "error":error}),
                    "hidden":hidden};
        }
        /**
         * Render the extractors 
         * @returns extractors drop-down html
         */
        function renderExtractors() {
            return "";
        }
        /**
         * Render this view, based on the given data model
         */
        function render() {
            var self = this;
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
                    recurseMerge(merged,utils.deep(elem.get("root")));
                    if (typeof(elem.validationError) === "undefined" || elem.validationError == null)
                        return;
                    //Update errors
                    for (var key in elem.validationError) {
                        merged.errors[key] = elem.validationError[key];
                    }

                });
            //Build this element
            for (var i = 0; i < items.length; i++) {
                inputHtmls.push(this.renderElementInput(items[i],merged));
            }
            var tmp = _.template($("script#template-elements-table").html());
            this.$el.html(tmp({"htmls":inputHtmls,"disabled":this.model.size() == 0}));
            //DataTables JS registering
            var table = $(this.$el).find("table:first");
            table.DataTable({"paging": false});
            //Attach events to the controls bindings.
            $(this.$el).find("table:first").find("input,select").on("change",this.dataEntry);
            $(this.$el).find("button#ingest").on("click",this.ingestClick);
            $(this.$el).find("button#clear-metadata").on("click",this.metaClear);
        };
        /**
         * A function to set the "on change" call-back for data inputs
         * @param func - function to call back (should come from controller)
         */
        function setOnEntryFunction(func) {
            this.dataEntry = func;
        }
        /**
         * A function to set the "on click" for ingest button
         * @param func - function to call back (should come from controller)
         */
        function setIngestClick(func) {
            this.ingestClick = func;
        };
        /**
         * A function to set the "on click" for metadata clear button
         * @param func - function to call back (should come from controller)
         */
        function setMetadataClear(func) {
            this.metaClear = func;
        };
        /**
         * Update the current product type from the controller
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
            setIngestClick: setIngestClick,
            setMetadataClear: setMetadataClear,
            //Private functions needing correct "this"
            renderElementInput: renderElementInput
            
        });
    });