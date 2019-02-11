import React from 'react';

import '../../../css/logos.scss';

const Logo = () => (
    <div className="productLogo">
        <span className="synopsysLogoSpan">
        <img
            className="synopsysHeaderLogo"
            src="https://www.synopsys.com/content/dam/synopsys/company/about/legal/synopsys-logos/whitelogo/synopsys_wht.png"
            alt="Synopsys"
        />
            <span className="headerStandardSize">&nbsp;|&nbsp;ALERT</span>&nbsp;
        </span>
    </div>
);

export default Logo;
