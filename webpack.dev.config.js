const path = require('path');
const merge = require('webpack-merge');
const commonConfig = require("./webpack.common.config.js");

const srcDir = path.resolve(__dirname, 'src');
const jsDir = path.resolve(srcDir, 'main', 'js');

module.exports = merge.smart(commonConfig, {
    mode: 'development',
    devtool: 'sourcemaps',
    devServer: {
        contentBase: [jsDir, path.resolve(srcDir, 'css'), path.resolve(srcDir, 'img')],
        contentBasePublicPath: '/alert/',
        https: true,
        hot: true,
        port: 9000,
        compress: true,
        historyApiFallback: true,
        disableHostCheck: true,
        proxy: [{
            context: ['/alert/api/**'],
            target: 'https://localhost:8443',
            secure: false,
            changeOrigin: true,
            cookieDomainRewrite: {
                '*': ''
            },
            logLevel: 'debug'
        }]
    }
});
