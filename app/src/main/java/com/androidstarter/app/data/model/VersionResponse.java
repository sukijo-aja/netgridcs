package com.androidstarter.app.data.model;

import com.google.gson.annotations.SerializedName;

public class VersionResponse {
    @SerializedName("global_last_updated")
    private long globalLastUpdated;

    @SerializedName("modules")
    private Modules modules;

    public long getGlobalLastUpdated() { return globalLastUpdated; }
    public Modules getModules() { return modules; }

    public static class Modules {
        // Add your generic modules here if needed in the future
        // e.g. @SerializedName("module_name") private ModuleInfo moduleName;
    }

    public static class ModuleInfo {
        @SerializedName("last_updated")
        private long lastUpdated;

        public long getLastUpdated() { return lastUpdated; }
    }
}
