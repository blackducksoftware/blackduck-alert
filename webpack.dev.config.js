const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const srcDir = path.resolve(__dirname, 'src');
const jsDir = path.resolve(srcDir, 'main', 'js');

const buildDir = path.resolve(__dirname, 'build', 'resources', 'main', 'static');

module.exports = {
    entry: ['babel-polyfill', 'whatwg-fetch', path.resolve(jsDir, 'Index')],
    devtool: 'sourcemaps',
    output: {
        path: buildDir,
        filename: 'js/bundle.js',
        publicPath: '/alert/'
    },
    module: {
        rules: [{
            test: /\.js$/,
            exclude: /(node_modules)/,
            use: ['babel-loader']
        }, {
            test: /\.(jpg|png|svg)$/,
            loader: 'file-loader',
            options: {
                name: '[path][name].[ext]'
            }
        }, {
            test: /\.scss$/,
            use: ['style-loader', 'css-loader', 'sass-loader']
        }, {
            test: /\.css$/,
            include: /(node_modules)/,
            use: ['style-loader', 'css-loader']
        }, {
            test: /\.woff(2)?(\?v=[0-9]\.[0-9]\.[0-9])?$/,
            loader: 'url-loader?limit=10000&mimetype=application/font-woff'
        }, {
            test: /\.(ttf|eot|svg)(\?v=[0-9]\.[0-9]\.[0-9])?$/,
            loader: 'file-loader'
        }]
    },
    plugins: [new HtmlWebpackPlugin({
        template: 'src/main/js/templates/index.html'
    })],
    devServer: {
        hot: true,
        port: 9000,
        compress: true,
        historyApiFallback: true,
        disableHostCheck: true,
        proxy: [{
            context: ['/api'],
            target: 'http://localhost.local:8080',
            secure: false,
            cookieDomainRewrite: {
                '*': ''
            }
        }]
    }
};
