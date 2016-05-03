/**
 * Configuration object for javascript application
 * @author starchmd
 */
_GLOBAL_CONFIG_VAR = null;
define(["jquery"],
    //TODO: Return configuration from server via REST call
    /**
     * Configuration object
     */
    function($) {
        /**
         * Configuration object acts like a map of configuration
         * keys to values, with the added function addConfig to allow adding keys.
         * 
         * Note: A Singleton configuration is created and returned to all.
         */
        function ConfigObject() {
            var _self = this;

            var path = $(location).attr("pathname");
            //Basic metadata
            _self.SETUP_CONFIG = {
                "hidden":[],
                "presets":{}
            },
            _self.METADATA_FILTERS = {
                "urn:oodt:ProductType":["RawData","Processed", "Supplementary", "Analysis", "Clinical"]
            };


            _self.METADATA_REST_SERVICE = "services/metadata";
            _self.DIRECTORY_REST_SERVICE = "services/directory";
            _self.EXTRACTOR_REST_SERVICE = "services/metadata/extractors";
            _self.INGEST_REST_SERVICE = "services/ingest";
            _self.VALIDATION_REST_SERVICE = "services/validation";
            //Dropzone requires full path
            _self.UPLOAD_REST_SERVICE = path.substr(0,path.lastIndexOf("/")+1)+"services/upload/file";
            _self.FILE_SYSTEM_REFRESH_INTERVAL = 1000;
            _self.DEFAULT_TYPE = "GenericFile";
            
            _self.addConfig = 
                /**
                 * Add configuration
                 * @param conf - anonymous object to copy into config
                 */
                function(conf) {
                    for (var key in conf) {
                        //Add key if not there
                        if (typeof(_self[key]) == "object" && typeof(conf[key]) == "object") {
                            for (var child in conf[key]) {
                                _self[key][child] = conf[key][child]; 
                            }  
                        } else if (typeof(_self[key]) != "object") {
                            _self[key] = conf[key];
                        }
                    }
                };
            _self.addPreset = 
                /**
                 * Adds a preset to the configuration
                 * @param key - key to assign preset to
                 * @param value - value of preset
                 */
                function(key,value) {
                    //If config is malformed reconstruct right keys
                    if (!("SETUP_CONFIG" in _self)) {
                        _self.SETUP_CONFIG = {"hidden":[],"presets":{}};
                    }
                    if (!("presets" in _self.SETUP_CONFIG)) {
                        _self.SETUP_CONFIG["presets"] = {}
                    }
                    _self.SETUP_CONFIG["presets"][key] = value;
                };
                
        }
        /**
         * Global configuration singleton
         */
        if (_GLOBAL_CONFIG_VAR == null) {
            var _GLOBAL_CONFIG_VAR = new ConfigObject();
        }
        return _GLOBAL_CONFIG_VAR;
    });
