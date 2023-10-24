import React from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import Pagination from 'common/component/navigation/Pagination';
import DropdownField from 'common/component/input/DropdownField';

const useStyles = createUseStyles({
    tableFooter: {
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'baseline',
        paddingBottom: '50px'
    },
    rowCountSelector: {
        marginLeft: 'auto',
        marginRight: 0
    }
});

const TableFooter = ({ data, onPage, onPageSize, showPageSize, pageSize }) => {
    const classes = useStyles();

    // Default table row amounts are 10, 25, 30, and 50 - these were collected from legacy code
    const onPageOptions = [{ label: '10 Rows', value: 10 }, { label: '25 Rows', value: 25 }, { label: '30 Rows', value: 30 }, { label: '50 Rows', value: 50 }];

    return (
        <div className={classes.tableFooter}>
            {data?.totalPages > 1 && (
                <div>
                    <Pagination data={data} onPage={onPage} />
                </div>
            )}
            {/* Until all endpoints are converted to HATEOAS, we need to use showPageSize prop to determine which tables should have this capability */}
            {(showPageSize) && (
                <div className={classes.rowCountSelector}>
                    <DropdownField options={onPageOptions} onChange={onPageSize} selectedValue={pageSize} />
                </div>
            )}
        </div>
    );
};

TableFooter.defaultProps = {
    showPageSize: false
};

TableFooter.propTypes = {
    data: PropTypes.object,
    onPage: PropTypes.func,
    onPageSize: PropTypes.func,
    showPageSize: PropTypes.bool,
    pageSize: PropTypes.string
};

export default TableFooter;
