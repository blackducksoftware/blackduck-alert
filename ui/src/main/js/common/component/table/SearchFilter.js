import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

const useStyles = createUseStyles({
    searchFilterContainer: {
        marginLeft: 'auto',
        marginRight: 0,
        display: 'flex',
        columnGap: '5px'
    },
    inputContainer: {
        position: 'relative'
    },
    inputStyle: {
        border: 'solid .5px',
        padding: ['4px', '20px', '4px', '10px'],
        font: 'inherit',
        cursor: 'text',
        textOverflow: 'ellipsis',
        overflow: 'hidden',
        '&:focus': {
            outline: 0
        }
    },
    clearInputIcon: {
        display: 'inline-block',
        border: 'none',
        background: 'none',
        position: 'absolute',
        right: '5px',
        top: '5px',
        '&:hover': {
            cursor: 'default'
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
                <input
                    className={classes.inputStyle}
                    placeholder={searchBarPlaceholder}
                    value={searchValue ?? ''}
                    onChange={handleChange}
                    onKeyDown={handleKeyDown}
                    disabled={isDisabled}
                />
                { searchValue && (
                    <button className={classes.clearInputIcon} onClick={handleClearSearchField} role="button" disabled={isDisabled} >
                        <FontAwesomeIcon icon="times" size='sm' />
                    </button>
                ) }
            </div>
                       
            <button className={classes.searchIconContainer} onClick={() => handleSearchChange(searchValue)} role="button" >
                <FontAwesomeIcon icon="search" />
            </button>
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