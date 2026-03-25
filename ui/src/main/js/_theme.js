const colors = {
    green: {
        darkGreen: '#3B7D3C'
    },
    grey: {
        lighterGrey: '#E8E6E6',
        lightGrey: '#D9D9D9',
        default: '#808080',
        darkGrey: '#666666',
        darkerGrey: '#4a5565',
        blackout: '#222222'
    },
    purple: {
        lightPurple: '#dab2ff',
        default: '#9810fa',
        darkPurple: '#343E4C',
        darkerPurple: '#1e2939'
    },
    red: {
        lighterRed: '#EA7B73',
        lightRed: '#E03C31',
        default: '#D72C20'
    },
    white: {
        default: '#FFFFFF'
    },
    borderColor: '#0000001a',
    defaultAlertColor: '#2E3B4E',
    darkGreyAlertColor: '#646E81',
    defaultAlertColor: '#2E3B4E',
    defaultBackgroundColor: '#F7F7FA',
    defaultBorderColor: '#B1B3B3',
    statusFailure: '#E15241',
    statusPending: '#F0AD4E',
    statusSuccess: '#509D51',
    warning: '#E07C05',
    status: {
        error: {
            background: '#ffe2e2',
            border: '#ffc9c9',
            default: '#9f0712',
            text: '#E15241'
        },
        success: {
            background: '#dcfce7',
            border: '#b9f8cf',
            default: '#016630',
            text: '#3B7D3C'
        },
        warning: {
            background: '#ffedd4',
            border: '#ffd6a8',
            default: '#9f2d00',
            text: '#E07C05'
        }
    },
};

const defaultBorder = `solid 1px ${colors.borderColor}`;

const theme = {
    colors,
    defaultBorder
};

export default theme;
