/**
 * API Response commonly returns format: "2026-04-08 10:31 (UTC)"
 * This utility will convert that format into a more readable format based on the user's 
 * locale.
 * 
 * @param {string} dateString - The date string in the format "YYYY-MM-DD HH:mm (UTC)"
 * @returns {string} - The formatted date string based on the user's locale
 */
export function formatDate(dateString) {
    console.log(dateString)
    if (!dateString) {
        return null;
    }

    const [datePart, timePart] = dateString.replace(' (UTC)', '').split(' ');

    const date = new Date(`${datePart}T${timePart}:00Z`);

    return date.toLocaleString();
}