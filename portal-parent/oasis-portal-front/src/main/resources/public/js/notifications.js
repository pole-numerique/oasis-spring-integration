/** @jsx React.DOM */

/** Custom translation */
function t(key) {
    if (typeof _i18n != 'undefined') {
        var v = _i18n[key];
        if (v != null) return v;
    }
    return key;
}



var NotificationTable = React.createClass({displayName: 'NotificationTable',
    loadNotifications: function() {
        $.ajax({
            url: this.props.url,
            datatype: 'json',
            success: function(data) {
                this.setState({n:data});
            }.bind(this),
            error: function(xhr, status, err) {
                console.error(this.props.url, status, err.toString());
            }.bind(this)
        });


    },
    getInitialState: function() {
        return {n: []};
    },
    componentDidMount: function() {
        this.loadNotifications();
        setInterval(this.loadNotifications, this.props.pollInterval);
    },
    sortBy: function(criterion) {
        var component = this;
        return function() {
            var n = component.state.n.sort(function (a, b) {
                return a[criterion].localeCompare(b[criterion]);
            });
            component.setState({n: n});
        };
    },
    removeNotif: function(id) {
        this.setState({n:this.state.n.filter(function(n) {return n.id != id;})});

        $.ajax({
            url: this.props.url + "/" + id,
            method: 'delete',
            datatype: 'json',
            success: function(data) {
                // nothing much to say is there?
            }.bind(this),
            error: function(xhr, status, err) {
                console.error(this.props.url, status, err.toString());
            }.bind(this)
        });
    },
    render: function () {
        var callback = this.removeNotif;
        if (this.state.n.length == 0) {
            return (
                React.DOM.div({className: "standard-form"}, 
                t('no-notification')
                )
                );
        } else {
            var notificationNodes = this.state.n.map(function (notif) {
                return (
                    Notification({key: notif.id, notif: notif, onRemoveNotif: callback})
                    );
            });
            return (
                React.DOM.div({className: "standard-form"}, 
                    React.DOM.div({className: "row form-table-header"}, 
                        React.DOM.div({className: "col-sm-2", onClick: this.sortBy('date')}, t('date')), 
                        React.DOM.div({className: "col-sm-2", onClick: this.sortBy('appName')}, t('app')), 
                        React.DOM.div({className: "col-sm-6", onClick: this.sortBy('formattedText')}, t('message'))
                    ), 
                notificationNodes
                )
                );
        }
    }
});

var Notification = React.createClass({
    displayName: "Notification",
    removeNotif: function(e) {
        this.props.onRemoveNotif(this.props.notif.id);
    },
    render: function() {
        return (
            React.DOM.div({className: "row form-table-row"}, 
                React.DOM.div({className: "col-sm-2"}, this.props.notif.dateText), 
                React.DOM.div({className: "col-sm-2"}, this.props.notif.appName), 
                React.DOM.div({className: "col-sm-6", dangerouslySetInnerHTML: {__html: this.props.notif.formattedText}}), 
                React.DOM.div({className: "col-sm-2"}, 
                    React.DOM.a({href: this.props.notif.url, target: "_new", className: "btn btn-primary"}, t('manage')), 
                    React.DOM.a({href: "#", className: "btn btn-primary", onClick: this.removeNotif}, t('archive'))
                )
            )
            );
    }
});


React.renderComponent(
    NotificationTable({url: notificationService, pollInterval: 2000})
    , document.getElementById("notifications"));
