import React from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import PaginationButton from '../button/PaginationButton';
import IconButton from '../button/IconButton';

function getDisplayPages(currentPage, totalPages) {
    let links = [];
    const starterPages = [0, 1, 2];
    const tailEndPages = [totalPages - 5, totalPages - 4, totalPages - 3, totalPages - 2, totalPages - 1, totalPages];

    // If the number of pages is 0 or 1, do not show pagination
    if (totalPages <= 1) {
        return links;
    }

    // If the number of pages is less than 5, show pagination of 5
    if (totalPages <= 5) {
        for (let i = 0; i < totalPages; i++) {
            links.push(i);
        }
        return links;
    }

    // If the current page is 1st, 2nd, or 3rd page show a pagination of up to 5
    if (starterPages.includes(currentPage)) {
        for(let i = 0; i < 5; i++){
            links.push(i);
        }
        return links;
    }

    // If the current page is any one of the last 5 available pages, return the last 5 available pages
    // This is more for consistency, so that we are always showing 5 pages
    if (tailEndPages.includes(currentPage)) {
        const startIndex = tailEndPages.length - 1;
        for(let i = totalPages - startIndex; i < totalPages; i++){
            links.push(i);
        }
        return links;
    }

    // If the current page is nth page show n-2 n-1 n n+1 n+2
    if (currentPage >= 3 && currentPage <= totalPages - 2) {
        for(let i = currentPage - 2; i < currentPage + 3; i++) {
            links.push(i);
        }
        return links;
    }

    return links;
}

const Pagination = ({ data, onPage }) => {
    const { currentPage, totalPages } = data;

    const links = getDisplayPages(currentPage, totalPages);

    const handlePageClick = (selectedPage) => {
        onPage(selectedPage);
    }
    
    const handleFirstPageClick = () => {
        onPage(0)
    }

    const handlePreviousPageclick = () => {
        if (currentPage !== 0) {
            onPage(currentPage - 1);
        }
    }

    const handleNextPageclick = () => {
        if (currentPage !== totalPages - 1) {
            onPage(currentPage + 1);
        }
    }

    const handleEndPageClick = () => {
        onPage(totalPages - 1);
    }

    return (
        <>
            <IconButton icon="angle-double-left" onClick={handleFirstPageClick} />
            <IconButton icon="angle-left" onClick={handlePreviousPageclick} />

            { links.map((page) => {
                const pageNumber = page + 1;
                return (
                    <PaginationButton pageNumber={pageNumber} isActive={page === currentPage} onClick={() => handlePageClick(page)} />
                )
            }) }

            <IconButton icon="angle-right" onClick={handleNextPageclick} />
            <IconButton icon="angle-double-right" onClick={handleEndPageClick} />
        </>
    );
};

Pagination.defaultProps = {
    data: {
        currentPage: 0,
        totalPages: 1
    }
};

Pagination.propTypes = {
    data: PropTypes.shape({
        currentPage: PropTypes.string,
        totalPages: PropTypes.string
    }),
    onPage: PropTypes.func,
};

export default Pagination;
