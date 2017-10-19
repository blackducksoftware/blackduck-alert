 var React = require('react');
 var client = require('./client');

 var App = React.createClass({
    render: function() {
        return (
            <h1>Hello World</h1>
        )
    }
 });

 React.render(
     <App />,
     document.getElementById('react')
 );