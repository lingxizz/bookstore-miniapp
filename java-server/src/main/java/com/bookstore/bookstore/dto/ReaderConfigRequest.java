package com.bookstore.bookstore.dto;

public class ReaderConfigRequest {
    private Integer fontSize;
    private Integer lineHeight;
    private String theme;
    private Integer brightness;
    private Integer paragraphSpacing;
    private String fontFamily;
    private String pagingMode;

    public Integer getFontSize() { return fontSize; }
    public void setFontSize(Integer fontSize) { this.fontSize = fontSize; }
    public Integer getLineHeight() { return lineHeight; }
    public void setLineHeight(Integer lineHeight) { this.lineHeight = lineHeight; }
    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
    public Integer getBrightness() { return brightness; }
    public void setBrightness(Integer brightness) { this.brightness = brightness; }
    public Integer getParagraphSpacing() { return paragraphSpacing; }
    public void setParagraphSpacing(Integer paragraphSpacing) { this.paragraphSpacing = paragraphSpacing; }
    public String getFontFamily() { return fontFamily; }
    public void setFontFamily(String fontFamily) { this.fontFamily = fontFamily; }
    public String getPagingMode() { return pagingMode; }
    public void setPagingMode(String pagingMode) { this.pagingMode = pagingMode; }
}
