import { library } from '@fortawesome/fontawesome-svg-core';

import {
    faPlus,
    faCog
} from '@fortawesome/free-solid-svg-icons';

export default function registerIcons() {
    library.add(
        faPlus,
        faCog
    );
}
