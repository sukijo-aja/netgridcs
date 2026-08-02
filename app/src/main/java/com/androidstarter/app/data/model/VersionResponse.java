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
        @SerializedName("products")
        private ModuleInfo products;

        @SerializedName("hadist")
        private ModuleInfo hadist;

        @SerializedName("khutbah")
        private ModuleInfo khutbah;

        public ModuleInfo getProducts() { return products; }
        public ModuleInfo getHadist() { return hadist; }
        public ModuleInfo getKhutbah() { return khutbah; }
    }

    public static class ModuleInfo {
        @SerializedName("last_updated")
        private long lastUpdated;

        public long getLastUpdated() { return lastUpdated; }
    }
}
