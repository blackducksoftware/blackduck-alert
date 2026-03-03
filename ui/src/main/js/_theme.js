const colors = {
    blue: {
        darkerBlue: '#0000001a'
    },
    green: {
        darkGreen: '#3B7D3C'
    },
    grey: {
        lighterGrey: '#E8E6E6',
        lightGrey: '#D9D9D9',
        default: '#808080',
        darkGrey: '#666666',
        darkerGrey: '#4a5565'
    },
    purple: {
        lightPurple: '#dab2ff',
        default: '#9810fa'
    },
    red: {
        lighterRed: '#EA7B73',
        lightRed: '#E03C31',
        default: '#D72C20'
    },
    white: {
        default: '#FFFFFF'
    },
    defaultAlertColor: '#2E3B4E',
    darkGreyAlertColor: '#646E81',
    statusFailure: '#E15241',
    statusPending: '#F0AD4E',
    statusSuccess: '#509D51',
    warning: '#E07C05'
};

const defaultBorder = `solid 1px ${colors.blue.darkerBlue}`;

const theme = {
    colors,
    defaultBorder
};

export default theme;
