package com.blackducksoftware.integration.alert.common.descriptor.config;

public class UIComponent {
    private String label;
    private String fontAwesomeIcon;
    private String reactComponentName;

    public UIComponent(final String label, final String fontAwesomeIcon, final String reactComponentName) {
        this.label = label;
        this.fontAwesomeIcon = fontAwesomeIcon;
        this.reactComponentName = reactComponentName;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    public String getFontAwesomeIcon() {
        return fontAwesomeIcon;
    }

    public void setFontAwesomeIcon(final String fontAwesomeIcon) {
        this.fontAwesomeIcon = fontAwesomeIcon;
    }

    public String getReactComponentName() {
        return reactComponentName;
    }

    public void setReactComponentName(final String reactComponentName) {
        this.reactComponentName = reactComponentName;
    }

}
