import React from 'react';
import { createUseStyles } from 'react-jss';
import PropTypes from 'prop-types';

const useStyles = createUseStyles({
    emptyView: {
        margin: ['55px', '40px'],
        textAlign: 'center',
        fontSize: '16px'
    }
});

const EmptyTableView = ({ emptyTableConfig }) => {
    const classes = useStyles();

    return (
        <div className={classes.emptyView}>
            {emptyTableConfig?.message}
        </div>
    );
};

EmptyTableView.defaultProps = {
    emptyTableConfig: {
        message: 'There are no records to display for this table.'
    }
};

EmptyTableView.propTypes = {
    emptyTableConfig: PropTypes.shape({
        message: PropTypes.string
    })
};

export default EmptyTableView;
