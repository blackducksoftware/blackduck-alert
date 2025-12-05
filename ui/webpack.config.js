const { merge } = require('webpack-merge');
const commonConfig = require('./webpack.common.config.js');

module.exports = merge(commonConfig, {
    mode: 'production',
    plugins: []
});
