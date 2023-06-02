import React from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

const useStyles = createUseStyles({
    inputContainer: {
        display: 'flex',
        height: '25px',
        marginBottom: '5px'
    },
    inputStyle: {
        margin: [0, '3px', 0, 'auto'],
        border: 'solid .5px',
        padding: ['2px', '4px'],
        font: 'inherit',
        cursor: 'text',
        '&:focus': {
            outline: 0
        }
    },
    searchIconContainer: {
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: '#e7e7f0',
        border: ['solid', '1px', '#ddd'],
        cursor: 'pointer',
        borderRadius: '50%',
        width: '30px',
        height: '30px'
    }
});

const SearchFilter = ({ searchBarPlaceholder, handleSearchChange, search }) => {
    const classes = useStyles();

    return (
        <>
            <input
                className={classes.inputStyle}
                onChange={handleSearchChange}
                placeholder={searchBarPlaceholder}
                value={search}
            />
            <span className={classes.searchIconContainer}>
                <FontAwesomeIcon icon="search" />
            </span>
        </>
    );
};

SearchFilter.propTypes = {
    searchBarPlaceholder: PropTypes.string,
    handleSearchChange: PropTypes.func,
    search: PropTypes.string
};

export default SearchFilter;
