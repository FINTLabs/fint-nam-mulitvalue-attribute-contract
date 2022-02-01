# Multivalue SAML Attribute Contract
## SUMMARY
This class is meant for automatical synchronization of user store in service provider, when user logs in with Remote login from Identity Provider. Our class also supports multivalue attributes
and synchronizes them correctly into LDAP base.

## BUILD
Before you build your project you have to set-up your environment. You have to add next AM .jar files into your project folder "netiq":
 - jcc.jar
 - jgroups-all.jar
 - jsso.jar
 - NAMCommon.jar
 - nidp.jar
 - nxpe.jar
 
Files are going to be found on server with Access Manager installed.
Default location:
 - Linux: /opt/novell/nam/idp/webapps/nidp/WEB-INF/lib/
 - Windows: C:\Program Files (x86)\Novell\Tomcat\webapps\nidp\WEB-INF\lib
 
## DEPLOYEMENT PROCESS
After the project is built you have to add .jar file into tomcat library.
Default location:
 - Linux: /opt/novell/nam/idp/webapps/nidp/WEB-INF/lib/
 - Windows: C:\Program Files (x86)\Novell\Tomcat\webapps\nidp\WEB-INF\lib
  
Now you should restart your Access Manager with command *rcnovell-idp restart* or even better restart whole machine.
  
## IMPLEMENTATION
First after deployement you have to create new class in Access Manager with your custom jar specified. . It is the best if you follow [documentation](https://www.netiq.com/documentation/access-manager-45/admin/data/authclasseslist.html) for this procedure.
Class definition:
 - Java Class: other
 - Java Class Path: si.genlan.nam.idp.UpdateUserStoreBySamlResponseContract
 
You have to define next properties in your class:
| Property Name | Property Value | Description |
| ------------- | -------------- | ----------- |
| ldapURL | ldaps://11.22.33.45:636;ldap://10.11.12.13:389 | URL From Access Manager Identity Servers. Since we can have more than all we should define all of them and separate them with ";" |
| ldapProtocol | ssl;noEnc; | Protocol for class to connect to LDAP base. "ssl" is used for SSL and StartTLS connection, for everything else "noEnc" is used. It has to be in same order as URL and every URL should have this property |
| ldapUsername | username1;username2; | Service account username for LDAP server specified in ldapURL. Users have to be in same order as URL and separated with ";". Each URL should also have username |
| ldapPassword | password1;password2; | Service account password for LDAP server specified in ldapURL. Passwords have to be in same order as URL and separated with ";". Each URL should also have password |
| trace | true/false | Debug log on/off |
 
After class has been created you also have to create new Method with help from [documentation.](https://www.netiq.com/documentation/access-manager-45/admin/data/configureauthmethod.html)
After the method you should also [create new Contract](https://www.netiq.com/documentation/access-manager-45/admin/data/localcontract.html). In this contract you also have to define previously created Method as **Post Authentication Method**.

# Trusted Providers
For this contract to work properly, you have to add your Identity Servers (If you have them installed on other server) and Identity Provider in your trusted roots store.
Documentation of security and certificates is found [here](https://www.netiq.com/documentation/access-manager-45/admin/data/b1iv0ad8.html) and the section on how to define trusted roots and trust stores is defined [here](https://www.netiq.com/documentation/access-manager-45/admin/data/trustedroots.html).



