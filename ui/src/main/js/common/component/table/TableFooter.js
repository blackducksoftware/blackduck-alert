import React from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import Pagination from 'common/component/navigation/Pagination';
import DropdownField from 'common/component/input/DropdownField';

const useStyles = createUseStyles((theme) => ({
    tableFooter: {
        position: 'relative',
        display: 'flex',
        alignItems: 'center',
        width: '100%',
        padding: ['14px', '20px']
    },
    paginationContainer: {
        position: 'absolute',
        left: '50%',
        transform: 'translateX(-50%)'
    },
    rowCountSelector: {
        marginLeft: 'auto',
        color: theme.colors.grey.default,
        fontSize: '14px',
        paddingRight: '4px',
        '& > select': {
            border: `solid 1px ${theme.colors.grey.lightGrey}`,
            borderRadius: '6px',

            '&:hover': {
                cursor: 'pointer',
                border: `solid 1px ${theme.colors.grey.default}`
            }
        }
    },
    countSelectorText: {
        margin: 0,
        paddingRight: '8px',
        display: 'inline-block'
    }
}));

const TableFooter = ({ data, onPage, onPageSize, pageSize }) => {
    const classes = useStyles();

    // Default table row amounts are 10, 25, 30, and 50 - these were collected from legacy code
    const onPageOptions = [{ label: '10', value: 10 }, { label: '25', value: 25 }, { label: '30', value: 30 }, { label: '50', value: 50 }];

    return (
        <div className={classes.tableFooter}>
            {data?.totalPages > 1 && (
                <div className={classes.paginationContainer}>
                    <Pagination data={data} onPage={onPage} />
                </div>
            )}
            <div className={classes.rowCountSelector}>
                <p className={classes.countSelectorText}>Rows per page:</p>
                <DropdownField options={onPageOptions} onChange={onPageSize} selectedValue={pageSize} id="row-count-selector" />
            </div>
        </div>
    );
};

TableFooter.propTypes = {
    data: PropTypes.object,
    onPage: PropTypes.func,
    onPageSize: PropTypes.func,
    pageSize: PropTypes.oneOfType([PropTypes.string, PropTypes.number])
};

export default TableFooter;
