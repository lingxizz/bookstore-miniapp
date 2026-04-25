package com.bookstore.bookstore.dto;

public class ReaderConfigRequest {
    private Integer fontSize;
    private Integer lineHeight;
    private String theme;
    private Integer brightness;

    public Integer getFontSize() { return fontSize; }
    public void setFontSize(Integer fontSize) { this.fontSize = fontSize; }
    public Integer getLineHeight() { return lineHeight; }
    public void setLineHeight(Integer lineHeight) { this.lineHeight = lineHeight; }
    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
    public Integer getBrightness() { return brightness; }
    public void setBrightness(Integer brightness) { this.brightness = brightness; }
}
