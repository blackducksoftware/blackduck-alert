import React from 'react';

class EmailConfiguration extends React.Component {
    render() {
        return (
            <div>
                <h1>Email Configuration</h1>
                <label for="emailTemplateDirectory">Email Template Directory</label>
                <input type="text" name="emailTemplateDirectory"></input><br /> 
                
                <label for="emailTemplateLogoImage">Email Template Logo</label>
                <input type="text" name="emailTemplateLogoImage"></input><br /> 
                
                <label for="emailSubjectLine">Email Subject Line</label>
                <input type="text" name="emailSubjectLine"></input><br /> 
                
                <h2>Mail Smtp Configuration</h2>
                <label for="mailSmtpHost">Mail Smtp Host</label>
                <input type="text" name="mailSmtpHost"></input><br />
                
                <label for="mailSmtpUser">Mail Smtp User</label>
                <input type="text" name="mailSmtpUser"></input><br />
                
                <label for="mailSmtpPassword">Mail Smtp Password</label>
                <input type="password" name="mailSmtpPassword"></input><br />
                
                <label for="mailSmtpPort">Mail Smtp Port</label>
                <input type="number" name="mailSmtpPort"></input><br />
                
                <label for="mailSmtpConnectionTimeout">Mail Smtp Connection Timeout</label>
                <input type="number" name="mailSmtpConnectionTimeout"></input><br />
                
                <label for="mailSmtpTimeout">Mail Smtp Timeout</label>
                <input type="number" name="mailSmtpTimeout"></input><br />
                
                <label for="mailSmtpFrom">Mail Smtp From</label>
                <input type="text" name="mailSmtpFrom"></input><br />
                
                <label for="mailSmtpLocalhost">Mail Smtp Localhost</label>
                <input type="text" name="mailSmtpLocalhost"></input><br />
                
                <label for="mailSmtpEhlo">Mail Smtp Ehlo</label>
                <input type="text" name="mailSmtpEhlo"></input><br />
                
                <label for="mailSmtpAuth">Mail Smtp Auth</label>
                <input type="text" name="mailSmtpAuth"></input><br />
                
                <label for="mailSmtpDnsNotify">Mail Smtp Dns Notify</label>
                <input type="text" name="mailSmtpDnsNotify"></input><br />
                
                <label for="mailSmtpDnsRet">Mail Smtp Dns Ret</label>
                <input type="text" name="mailSmtpDnsRet"></input><br /> 
                
                <label for="mailSmtpAllow8bitmime">Mail Smtp Allow 8-bit Mime</label>
                <input type="checkbox" name="mailSmtpAllow8bitmime"></input><br /> 
                
                <label for="mailSmtpSendPartial">Mail Smtp Send Partial</label>
                <input type="checkbox" name="mailSmtpSendPartial"></input><br /> 
            </div>
        )
    }
}

export default EmailConfiguration;
