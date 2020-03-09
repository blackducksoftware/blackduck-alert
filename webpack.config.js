const merge = require('webpack-merge');
const commonConfig = require("./webpack.common.config.js");
const MinifyPlugin = require('babel-minify-webpack-plugin');

module.exports = merge.smart(commonConfig, {
    mode: 'production',
    plugins: [
        new MinifyPlugin()
    ]
});
