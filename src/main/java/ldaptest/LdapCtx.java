package ldaptest;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.util.Hashtable;

public class LdapCtx {

    public static void main(String[] args) {


        Hashtable<String, Object> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap://172.16.23.36:389");
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, "cn=bobpingadmin,dc=bobping,dc=cn");
        env.put(Context.SECURITY_CREDENTIALS, "123456");
        env.put("java.naming.ldap.factory.socket", "ldapConn.CustomSocketFactory4Mex");
        env.put("com.sun.jndi.ldap.connect.timeout", "2000");
        env.put("com.sun.jndi.ldap.read.timeout", "5000");

        try {
            LdapContext ctx = new InitialLdapContext(env, null);
            System.out.println("Successfully connected to LDAP server");

            // Perform LDAP operations here, such as searching for entries
            SearchControls searchControls = new SearchControls();
            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration<SearchResult> results = ctx.search("dc=bobping,dc=cn", "(objectclass=*)", searchControls);
            while (results.hasMore()) {
                SearchResult result = results.next();
                Attributes attrs = result.getAttributes();
                System.out.println(attrs);
            }

            // Close the LDAP connection

            Hashtable<?, ?> environment = ctx.getEnvironment();
            for (Object key : environment.keySet()) {
                System.out.println(key + ": " + environment.get(key));
            }

            ctx.close();
        } catch (NamingException e) {
            System.err.println("Failed to connect to LDAP server: " + e.getMessage());
        }
    }
}
