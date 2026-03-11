package com.mosleemapp.app.utils;

import android.text.Html;
import android.text.Spanned;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TajweedHelper {
    
    private static final Map<String, String> tajweedColors = new HashMap<>();
    
    static {
        // Grey - Silent letters, Hamzatul Wasl, Lam Shamsiyyah
        tajweedColors.put("h", "#AAAAAA");
        tajweedColors.put("s", "#AAAAAA");
        tajweedColors.put("l", "#AAAAAA");
        
        // Light Blue - Normal/Permissible Prolongation
        tajweedColors.put("n", "#537FFF");
        tajweedColors.put("p", "#537FFF");
        
        // Dark Blue - Necessary Prolongation
        tajweedColors.put("m", "#000EAA");
        
        // Red - Qalaqah
        tajweedColors.put("q", "#DD0008");
        
        // Cyan - Iqlab
        tajweedColors.put("i", "#26BFFD");
        
        // Green - Ikhafa' Shafawi, Idgham Shafawi, Idgham with Ghunnah
        tajweedColors.put("c", "#169200");
        tajweedColors.put("w", "#169200");
        tajweedColors.put("a", "#169200");
        
        // Purple - Ikhafa', Idgham without Ghunnah
        tajweedColors.put("f", "#9400A8");
        tajweedColors.put("u", "#9400A8");
    }

    public static Spanned parseTajweed(String tajweedText) {
        if (tajweedText == null) return Html.fromHtml("", Html.FROM_HTML_MODE_LEGACY);

        String htmlText = tajweedText;
        boolean changed = true;

        // Actual API format: [tag:optional_id[visible_text]
        // e.g. [h:1[ٱ] or [l[ل] or [n[ـٰ]
        // Pattern: opening bracket, tag letter(s), optional :id, inner bracket with text 
        Pattern pattern = Pattern.compile("\\[([a-z])[^\\[]*\\[([^\\[\\]]+)\\]");

        while (changed) {
            Matcher m = pattern.matcher(htmlText);
            StringBuffer sb = new StringBuffer();
            changed = false;

            while (m.find()) {
                changed = true;
                String tag = m.group(1);
                String text = m.group(2);
                String color = tajweedColors.containsKey(tag) ? tajweedColors.get(tag) : "#000000";
                // Using Matcher.quoteReplacement to safely handle Arabic in replacement
                String replacement = "<font color='" + color + "'>" + text + "</font>";
                m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }
            m.appendTail(sb);
            htmlText = sb.toString();
        }

        return Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY);
    }
}
