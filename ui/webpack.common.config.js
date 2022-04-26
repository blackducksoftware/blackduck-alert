const path = require('path');
const ExtractTextPlugin = require('extract-text-webpack-plugin');
const HtmlWebpackPlugin = require('html-webpack-plugin');

const srcDir = path.resolve(__dirname, 'src');
const jsDir = path.resolve(srcDir, 'main', 'js');

const buildDir = path.resolve(__dirname, 'build', 'resources', 'main', 'static');

module.exports = {
    resolve: {
        modules: [path.resolve(__dirname, 'src', 'main', 'js'), 'node_modules'],
        extensions: ['.js']
    },
    entry: ['@babel/polyfill', 'whatwg-fetch', path.resolve(jsDir, 'Index')],
    output: {
        path: buildDir,
        filename: 'js/bundle.js',
        publicPath: '/alert/'
    },
    module: {
        rules: [{
            test: /\.js$/,
            exclude: /(node_modules)/,
            loader: 'babel-loader'
        }, {
            test: /\.(jpg|png|svg)$/,
            exclude: /(node_modules)/,
            loader: 'file-loader',
            options: {
                name: '[path][name].[ext]'
            }
        }, {
            test: /\.scss$/,
            loader: ExtractTextPlugin.extract('css-loader!sass-loader')
        }, {
            test: /\.css$/,
            include: /(node_modules)/,
            loader: ExtractTextPlugin.extract('css-loader')
        }, {
            test: /\.woff(2)?(\?v=[0-9]\.[0-9]\.[0-9])?$/,
            loader: 'url-loader?limit=10000&mimetype=application/font-woff&name=fonts/[name].[ext]'
        }, {
            test: /\.(ttf|eot|svg)(\?v=[0-9]\.[0-9]\.[0-9])?$/,
            loader: 'url-loader?limit=10000&name=fonts/[name].[ext]'
        }]
    },
    plugins: [
        new HtmlWebpackPlugin({
            favicon: 'src/main/resources/favicon.ico',
            template: 'src/main/js/index.html',
            xhtml: true
        }),
        new ExtractTextPlugin('css/style.css', {
            allChunks: true
        })
    ]
};
