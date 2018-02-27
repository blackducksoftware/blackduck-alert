const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const srcDir = path.resolve(__dirname, 'src');
const jsDir = path.resolve(srcDir, 'main', 'js');

const buildDir = path.resolve(__dirname, 'build', 'resources', 'main', 'static');

module.exports = {
    entry: path.resolve(jsDir, 'Index'),
    devtool: 'sourcemaps',
    output: {
        path: buildDir,
        filename: 'js/bundle.js',
        publicPath: '/'
    },
    module: {
        rules: [
            {
                test: /\.js$/,
                exclude: /(node_modules)/,
                loader: 'babel-loader'
            }, {
                test: /\.(jpg|png|svg)$/,
                use: [{
                    loader: 'file-loader',
                    options: {
                        name: '[path][name].[ext]'
                    }
                }]
            }, {
                test: /\.scss$/,
                exclude: /(node_modules)/,
                use: [{
                    loader: "style-loader" // creates style nodes from JS strings
                }, {
                    loader: "css-loader" // translates CSS into CommonJS
                }, {
                    loader: "sass-loader" // compiles Sass to CSS
                }]
            }, {
                test: /\.css$/,
                exclude: /node_modules/,
                use: [{
                    loader: 'style-loader'
                }, {
                    loader: 'css-loader',
                    options: {
                        modules: true,
                        importLoaders: 1,
                        localIdentName: '[name]__[local]___[hash:base64:5]'
                    }
                }]
            }, {
                test: /\.css$/,
                include: /node_modules/,
                use: [{
                    loader: 'style-loader'
                }, {
                    loader: 'css-loader',
                    options: {
                        modules: true,
                        importLoaders: 1,
                        localIdentName: '[local]'
                    }
                }]
            }, {
                test: /\.woff(2)?(\?v=[0-9]\.[0-9]\.[0-9])?$/,
                loader: 'url-loader?limit=10000&mimetype=application/font-woff'
            }, {
                test: /\.(ttf|eot|svg)(\?v=[0-9]\.[0-9]\.[0-9])?$/,
                loader: 'file-loader'
            }
        ]
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
            target: "http://kkelley-mac.local:8080",
            secure: false,
            cookieDomainRewrite: {
                "*": ""
            }
        }]
    }
};
