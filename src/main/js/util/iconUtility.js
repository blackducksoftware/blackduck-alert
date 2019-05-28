import { library } from '@fortawesome/fontawesome-svg-core';
import { fas } from '@fortawesome/free-solid-svg-icons';
import { fab } from '@fortawesome/free-brands-svg-icons';

export function loadIconData() {
    library.add(fas, fab);
}

// Because Font Awesome now has different icon sets we need to create an array that includes the icon set.
// the default set is fas the solid icons.  Brands are in the fab set.
// if the parameter to this function contains a '/' we construct an array from the string splitting on '/' to denote the icon set.
// if the parameter to this function does not contain a '/' then we use the default icon set.
// Format:
// <FA_SET>/<ICON_NAME> = ['<FA_SET>', '<ICON_NAME>']
// <ICON_NAME> = ['fas', '<ICON_NAME>]'
export function createIconPath(iconPathData) {
    let path = null;
    if (iconPathData) {
        path = ['fas'];
        if (iconPathData.includes('/')) {
            path = iconPathData.split('/');
        } else {
            path.push(iconPathData);
        }
    }
    return path;
}
