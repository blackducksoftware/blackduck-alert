export function createIconPath(iconPathData) {
    let path = null;
    if (iconPathData) {
        path = ['fas'];
        if (iconPathData.includes(',')) {
            path = iconPathData.split(',');
        } else {
            path.push(iconPathData);
        }
    }
    return path;
}
