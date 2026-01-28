package com.mosleemapp.app.data.models;

public class DuaItem {
    private String title;
    private String arabic;
    private String latin;
    private String translation;

    public DuaItem(String title, String arabic, String latin, String translation) {
        this.title = title;
        this.arabic = arabic;
        this.latin = latin;
        this.translation = translation;
    }

    public String getTitle() {
        return title;
    }

    public String getArabic() {
        return arabic;
    }

    public String getLatin() {
        return latin;
    }

    public String getTranslation() {
        return translation;
    }
}
