const path = require('path');
const merge = require('webpack-merge');
const commonConfig = require('./webpack.common.config.js');

const srcDir = path.resolve(__dirname, 'src');
const jsDir = path.resolve(srcDir, 'main', 'js');

module.exports = merge.smart(commonConfig, {
    mode: 'development',
    devtool: 'source-map',
    output: {
        publicPath: '/'
    },
    devServer: {
        static: [
            { directory: jsDir, publicPath: '/alert/' },
            { directory: path.resolve(srcDir, 'css'), publicPath: '/alert/' },
            { directory: path.resolve(srcDir, 'img'), publicPath: '/alert/' }
        ],
        https: true,
        hot: true,
        port: 9000,
        compress: true,
        historyApiFallback: true,
        allowedHosts: 'all',
        proxy: [{
            context: ['/alert/api/**', '/alert/saml2/**', 'alert/swagger-ui/**'],
            target: 'https://localhost:8443',
            secure: false,
            cookieDomainRewrite: {
                '*': ''
            },
            logLevel: 'debug'
        }]
    }
});
