import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

const useStyles = createUseStyles(theme => ({
    searchFilterContainer: {
        marginLeft: 'auto',
        marginRight: 0,
        display: 'flex',
        columnGap: '5px'
    },
    inputContainer: {
        position: 'relative',
        border: `solid 1px ${theme.colors.grey.lightGrey}`,
        padding: ['8px', '14px'],
        borderRadius: '6px',
        display: 'flex',
        alignItems: 'center'
    },
    inputStyle: {
        border: 'none',
        font: 'inherit',
        cursor: 'text',
        textOverflow: 'ellipsis',
        overflow: 'hidden',
        width: '125px',
        '&:focus': {
            outline: 0
        }
    },
    searchIcon: {
        color: theme.colors.grey.default,
        backgroundColor: 'transparent',
        border: 'none',
        paddingRight: '8px',
        margin: ['auto', 0],

        '&:hover': {
            color: 'oklch(37.3% 0.034 259.733)',
        },
    },
    clearInputIcon: {
        border: 'none',
        background: 'none',
        width: '16px',
        height: '16px',
        padding: 0,
        display: 'inline-flex',
        alignItems: 'center',
        justifyContent: 'center'
    },
    clearInputIconHidden: {
        visibility: 'hidden',
        pointerEvents: 'none'
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
}));

const SearchFilter = ({ searchBarPlaceholder, handleSearchChange, defaultSearchValue, isDisabled }) => {
    const classes = useStyles();
    const [searchValue, setSearchValue] = useState(null);

    useEffect(() => {
        setSearchValue(defaultSearchValue);
    }, [defaultSearchValue]);

    function handleChange(evt) {
        setSearchValue(evt.target.value)
    }

    // Search when user presses enter (ASCII value for Enter/Return is 13)
    function handleKeyDown(evt) {
        if (evt.keyCode === 13) {
            handleSearchChange(searchValue);
        }
    }

    function handleClearSearchField() {
        setSearchValue('');
        handleSearchChange('');
    }

    return (
        <div className={classes.searchFilterContainer}>
            <div className={classes.inputContainer}>
                <button className={classes.searchIcon} onClick={() => handleSearchChange(searchValue)} role="button" disabled={isDisabled}>
                    <FontAwesomeIcon icon="magnifying-glass" />
                </button>
                <input
                    className={classes.inputStyle}
                    placeholder={searchBarPlaceholder}
                    value={searchValue ?? ''}
                    onChange={handleChange}
                    onKeyDown={handleKeyDown}
                    disabled={isDisabled}
                />
                <button
                    className={`${classes.clearInputIcon} ${!searchValue ? classes.clearInputIconHidden : ''}`}
                    onClick={handleClearSearchField}
                    role="button"
                    disabled={isDisabled || !searchValue}
                    type="button"
                    aria-label="Clear search"
                >
                    <FontAwesomeIcon icon="times" />
                </button>
            </div>
        </div>
    );
};

SearchFilter.propTypes = {
    searchBarPlaceholder: PropTypes.string,
    handleSearchChange: PropTypes.func,
    defaultSearchValue: PropTypes.string,
    isDisabled: PropTypes.bool
};

export default SearchFilter;